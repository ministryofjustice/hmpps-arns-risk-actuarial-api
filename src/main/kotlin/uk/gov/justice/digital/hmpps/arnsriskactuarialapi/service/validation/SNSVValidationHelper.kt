package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getDomesticViolencePerpetrator

val SNSV_STATIC_REQUIRED_PROPERTIES = listOf(
  RiskScoreRequest::gender,
  RiskScoreRequest::dateOfBirth,
  RiskScoreRequest::dateOfCurrentConviction,
  RiskScoreRequest::currentOffenceCode,
  RiskScoreRequest::totalNumberOfSanctionsForAllOffences,
  RiskScoreRequest::ageAtFirstSanction,
  RiskScoreRequest::supervisionStatus,
  RiskScoreRequest::dateAtStartOfFollowupUserInput,
  RiskScoreRequest::totalNumberOfViolentSanctions,
)

val SNSV_DYNAMIC_ADDITIONAL_REQUIRED_PROPERTIES = listOf(
  RiskScoreRequest::didOffenceInvolveCarryingOrUsingWeapon,
  RiskScoreRequest::suitabilityOfAccommodation,
  RiskScoreRequest::isUnemployed,
  RiskScoreRequest::currentRelationshipWithPartner,
  RiskScoreRequest::currentAlcoholUseProblems,
  RiskScoreRequest::excessiveAlcoholUse,
  RiskScoreRequest::impulsivityProblems,
  RiskScoreRequest::temperControl,
  RiskScoreRequest::proCriminalAttitudes,
  RiskScoreRequest::previousConvictions,
)

fun validateSNSV(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val errors = mutableListOf<ValidationErrorResponse>()
  validateRequiredFields(request, errors, SNSV_STATIC_REQUIRED_PROPERTIES)
  validateCurrentOffenceCode(request, errors)
  return errors
}

fun isValidDynamicSnsv(request: RiskScoreRequest): Boolean = getNullValuesFromProperties(
  request,
  SNSV_DYNAMIC_ADDITIONAL_REQUIRED_PROPERTIES,
).isEmpty() &&
  hasDomesticViolencePerpetrator(request)

fun snsvDynamicValidation(request: RiskScoreRequest, errors: MutableList<ValidationErrorResponse>) {
  if (!hasDomesticViolencePerpetrator(request)) {
    errors += addMissingFields(
      getNullValuesFromProperties(
        request,
        listOf(
          RiskScoreRequest::evidenceOfDomesticAbuse,
          RiskScoreRequest::domesticAbuseAgainstPartner,
        ),
      ),
      emptyList(),
    )
  }
}

fun hasDomesticViolencePerpetrator(request: RiskScoreRequest) = getDomesticViolencePerpetrator(
  request.evidenceOfDomesticAbuse,
  request.domesticAbuseAgainstPartner,
) != null
