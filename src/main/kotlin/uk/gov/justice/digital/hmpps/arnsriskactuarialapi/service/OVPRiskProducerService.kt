package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
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
class OVPRiskProducerService : RiskProducer<OVPObject> {

  override fun getRiskScore(riskScoreRequest: RiskScoreRequest): OVPObject {
    val errors = ovpInitialValidation(riskScoreRequest)

    if (errors.isNotEmpty()) {
      return OVPObject(riskScoreRequest.version, null, null, null, errors)
    }

    val validRequest = OVPRequestValidated(
      riskScoreRequest.version,
      riskScoreRequest.totalNumberOfSanctions!!.toInt(),
      riskScoreRequest.totalNumberOfViolentSanctions!!.toInt(),
      riskScoreRequest.dateAtStartOfFollowup!!,
      riskScoreRequest.dateOfBirth!!,
      riskScoreRequest.gender!!,
      riskScoreRequest.impactOfOffendingOnOthers!!,
      riskScoreRequest.currentAccommodation!!,
      riskScoreRequest.employmentStatus!!,
      riskScoreRequest.alcoholIsCurrentUseAProblem!!,
      riskScoreRequest.alcoholExcessive6Months!!,
      riskScoreRequest.currentPsychiatricTreatmentOrPending!!,
      riskScoreRequest.temperControl!!,
      riskScoreRequest.proCriminalAttitudes!!,
    )
    return getOVPObject(validRequest, errors)
  }

  private fun getOVPObject(
    request: OVPRequestValidated,
    errors: MutableList<ValidationErrorResponse>,
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
      algorithmVersion = request.version,
      provenViolentTypeReoffendingOneYear = oneYear,
      provenViolentTypeReoffendingTwoYear = twoYear,
      band = band,
      validationError = errors,
    )
  }
    .getOrElse {
      errors.add(
        ValidationErrorResponse(
          type = ValidationErrorType.NO_MATCHING_INPUT,
          message = "Error: ${it.message}",
          fields = emptyList(),
        ),
      )
      OVPObject(request.version, null, null, null, errors)
    }
}
