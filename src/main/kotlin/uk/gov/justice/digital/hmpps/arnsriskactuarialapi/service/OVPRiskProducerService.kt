package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ovp.OVPObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ovp.OVPRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.calculateOVPPercentageOneYear
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.calculateOVPPercentageTwoYears
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getAgeAtStartOfFollowup
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getAlcoholMisuseWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getAnyPreviousSanctionsWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getCurrentAccommodationOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getCurrentAccommodationWeightedOVP
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getCurrentPsychiatricTreatmentOrPendingWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getEmploymentStatusOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getEmploymentStatusWeightedOVP
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getGenderWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getImpactOfOffendingOnOthersWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOVPBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOffenderAgeGroupOVP
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getTotalNumberOfNonViolentSanctionsWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getTotalNumberOfViolentSanctionsWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.ovpInitialValidation

@Service
class OVPRiskProducerService : RiskScoreProducer {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val errors = ovpInitialValidation(request)

    if (errors.isNotEmpty()) {
      return context.apply { OVP = OVPObject(null, null, null, errors) }
    }

    val validRequest = OVPRequestValidated(
      request.totalNumberOfSanctions!!.toInt(),
      request.totalNumberOfViolentSanctions!!.toInt(),
      request.dateAtStartOfFollowup!!,
      request.dateOfBirth!!,
      request.gender!!,
      request.impactOfOffendingOnOthers!!,
      request.currentAccommodation!!,
      request.employmentStatus!!,
      request.alcoholIsCurrentUseAProblem!!,
      request.alcoholExcessive6Months!!,
      request.currentPsychiatricTreatmentOrPending!!,
      request.temperControl!!,
      request.proCriminalAttitudes!!,
    )
    return context.apply { OVP = getOVPObject(validRequest) }
  }

  private fun getOVPObject(
    request: OVPRequestValidated,
  ): OVPObject = runCatching {
    val alcoholMisuseWeighted = getAlcoholMisuseWeighted(request)

    val staticItems = listOf(
      getAnyPreviousSanctionsWeighted(request),
      getTotalNumberOfViolentSanctionsWeighted(request.totalNumberOfViolentSanctions),
      getTotalNumberOfNonViolentSanctionsWeighted(
        request.totalNumberOfSanctions - request.totalNumberOfViolentSanctions,
      ),
      getOffenderAgeGroupOVP(getAgeAtStartOfFollowup(request)),
      getGenderWeighted(request),
    ).sum()

    val dynamicItems = listOf(
      getImpactOfOffendingOnOthersWeighted(request),
      getCurrentAccommodationWeightedOVP(getCurrentAccommodationOffendersScore(request)),
      getEmploymentStatusWeightedOVP(getEmploymentStatusOffendersScore(request)),
      alcoholMisuseWeighted,
      getCurrentPsychiatricTreatmentOrPendingWeighted(request),
      request.temperControl.ordinal * 3,
      request.proCriminalAttitudes.ordinal * 3,
    ).sum()

    val totalOVPScore = staticItems + dynamicItems

    val oneYear = calculateOVPPercentageOneYear(totalOVPScore)
    val twoYear = calculateOVPPercentageTwoYears(totalOVPScore)
    val band = getOVPBand(twoYear)

    return OVPObject(
      provenViolentTypeReoffendingOneYear = oneYear,
      provenViolentTypeReoffendingTwoYear = twoYear,
      band = band,
      validationError = emptyList(),
    )
  }
    .getOrElse {
      OVPObject(
        null,
        null,
        null,
        arrayListOf(
          ValidationErrorResponse(
            type = ValidationErrorType.NO_MATCHING_INPUT,
            message = "Error: ${it.message}",
            fields = emptyList(),
          ),
        ),
      )
    }
}
