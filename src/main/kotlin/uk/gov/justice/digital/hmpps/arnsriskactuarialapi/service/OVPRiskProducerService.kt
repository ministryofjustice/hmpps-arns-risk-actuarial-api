package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ovp.OVPObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ovp.OVPRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.calculateOVPPercentage
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getAgeAtStartOfFollowup
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getAnyPreviousSanctionsWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getCurrentAccommodationOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getCurrentAccommodationWeightedOVP
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getCurrentPsychiatricTreatmentOrPendingWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getEmploymentStatusOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getEmploymentStatusWeightedOVP
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getGenderWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getImpactOfOffendingOnOthersWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOffenderAgeGroupOVP
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getTotalNumberOfNonViolentSanctionsWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getTotalNumberOfViolentSanctionsWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.ovpInitialValidation
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.asPercentage
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.sanitisePercentage
import kotlin.math.floor

@Service
class OVPRiskProducerService : RiskProducer<OVPObject> {

  @Autowired
  lateinit var offenceGroupParametersService: OffenceGroupParametersService

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
    val alcoholMisuse = request.alcoholIsCurrentUseAProblem.ordinal + request.alcoholExcessive6Months.ordinal

    val anyPreviousSanctionsWeighted = getAnyPreviousSanctionsWeighted(request)
    val totalNumberOfViolentSanctionsWeighted =
      getTotalNumberOfViolentSanctionsWeighted(request.totalNumberOfViolentSanctions)
    val totalNumberOfNonViolentSanctionsWeighted =
      getTotalNumberOfNonViolentSanctionsWeighted(request.totalNumberOfSanctions.minus(request.totalNumberOfViolentSanctions))
    val offenderAgeGroupOVP = getOffenderAgeGroupOVP(getAgeAtStartOfFollowup(request))
    val genderWeighted = getGenderWeighted(request)
    val impactOfOffendingOnOthersWeighted = getImpactOfOffendingOnOthersWeighted(request)
    val currentAccommodationWeightedOVP =
      getCurrentAccommodationWeightedOVP(getCurrentAccommodationOffendersScore(request))
    val employmentStatusWeightedOVP = getEmploymentStatusWeightedOVP(getEmploymentStatusOffendersScore(request))
    val alcoholMisuseWeighted = floor(alcoholMisuse * 2.5).toInt()
    val currentPsychiatricTreatmentOrPendingWeighted = getCurrentPsychiatricTreatmentOrPendingWeighted(request)
    val temperControlOffendersScoreWeighted = request.temperControl.ordinal * 3
    val proCriminalAttitudesWeightedOVP = request.proCriminalAttitudes.ordinal * 3

    val totalStaticItems = anyPreviousSanctionsWeighted + totalNumberOfViolentSanctionsWeighted + totalNumberOfNonViolentSanctionsWeighted + offenderAgeGroupOVP + genderWeighted
    val totalDynamicItems = impactOfOffendingOnOthersWeighted + currentAccommodationWeightedOVP + employmentStatusWeightedOVP + alcoholMisuseWeighted + currentPsychiatricTreatmentOrPendingWeighted + temperControlOffendersScoreWeighted + proCriminalAttitudesWeightedOVP
    val totalOVPScore = totalStaticItems + totalDynamicItems

    val provenViolentTypeReoffendingOneYear = calculateOVPPercentage(totalOVPScore, 4.5215).asPercentage().sanitisePercentage()
    val provenViolentTypeReoffendingTwoYear = calculateOVPPercentage(totalOVPScore, 3.8773).asPercentage().sanitisePercentage()
    val bandOVP = null
    OVPObject(request.version, provenViolentTypeReoffendingOneYear, provenViolentTypeReoffendingTwoYear, bandOVP, errors)
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
