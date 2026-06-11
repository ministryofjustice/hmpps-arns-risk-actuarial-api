package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ovp.OVPObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ovp.OVPRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.calculateOVPPercentageOneYear
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.calculateOVPPercentageTwoYears
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getAgeAtStartOfFollowup
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getAlcoholMisuseWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getAnyPreviousSanctionsWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getDoesRecogniseImpactOfOffendingOnOthersWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getGenderWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getHasCurrentPsychiatricTreatmentWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getIsCurrentlyOfNoFixedAbodeOrTransientAccommodationOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getIsCurrentlyOfNoFixedAbodeOrTransientAccommodationWeightedOVP
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getIsUnemployedOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getIsUnemployedWeightedOVP
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOVPBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOffenderAgeGroupOVP
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getTotalNumberOfNonViolentSanctionsWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getTotalNumberOfViolentSanctionsWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validateOVP

@Service
class OVPRiskProducerService : BaseRiskScoreProducer() {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val errors = validateOVP(request)

    if (errors.isNotEmpty()) {
      return applyErrorsToContextAndReturn(context, errors)
    }

    val validRequest = OVPRequestValidated(
      request.totalNumberOfSanctionsForAllOffences!!.toInt(),
      request.totalNumberOfViolentSanctions!!.toInt(),
      request.dateAtStartOfFollowupCalculated!!,
      request.dateOfBirth!!,
      request.gender!!,
      request.doesRecogniseImpactOfOffendingOnOthers!!,
      request.isCurrentlyOfNoFixedAbodeOrTransientAccommodation!!,
      request.isUnemployed!!,
      request.currentAlcoholUseProblems!!,
      request.excessiveAlcoholUse!!,
      request.hasCurrentPsychiatricTreatment!!,
      request.temperControl!!,
      request.proCriminalAttitudes!!,
    )
    return context.apply { OVP = getOVPObject(validRequest) }
  }

  override fun applyErrorsToContextAndReturn(
    context: RiskScoreContext,
    validationErrorResponses: List<ValidationErrorResponse>,
  ): RiskScoreContext = context.apply { OVP = OVPObject(null, null, null, null, validationErrorResponses) }

  private fun getOVPObject(
    request: OVPRequestValidated,
  ): OVPObject {
    val alcoholMisuseWeighted = getAlcoholMisuseWeighted(request)

    val staticItems = listOf(
      getAnyPreviousSanctionsWeighted(request),
      getTotalNumberOfViolentSanctionsWeighted(request.totalNumberOfViolentSanctions),
      getTotalNumberOfNonViolentSanctionsWeighted(
        request.totalNumberOfSanctionsForAllOffences - request.totalNumberOfViolentSanctions,
      ),
      getOffenderAgeGroupOVP(getAgeAtStartOfFollowup(request)),
      getGenderWeighted(request),
    ).sum()

    val dynamicItems = listOf(
      getDoesRecogniseImpactOfOffendingOnOthersWeighted(request),
      getIsCurrentlyOfNoFixedAbodeOrTransientAccommodationWeightedOVP(
        getIsCurrentlyOfNoFixedAbodeOrTransientAccommodationOffendersScore(request),
      ),
      getIsUnemployedWeightedOVP(getIsUnemployedOffendersScore(request)),
      alcoholMisuseWeighted,
      getHasCurrentPsychiatricTreatmentWeighted(request),
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
      pointScore = totalOVPScore,
      band = band,
      validationError = emptyList(),
    )
  }
}
