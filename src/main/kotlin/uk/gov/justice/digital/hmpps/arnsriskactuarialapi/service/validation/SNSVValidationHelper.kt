package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getDomesticViolencePerpetrator

val SNSV_STATIC_REQUIRED_PROPERTIES = listOf(
  RiskScoreRequest::gender,
  RiskScoreRequest::dateOfBirth,
  RiskScoreRequest::dateOfCurrentConviction,
  RiskScoreRequest::currentOffenceCode,
  RiskScoreRequest::totalNumberOfSanctionsForAllOffences,
  RiskScoreRequest::ageAtFirstSanction,
  RiskScoreRequest::supervisionStatus,
  RiskScoreRequest::dateAtStartOfFollowup,
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
  validateRequiredFields(request, errors)
  validateCurrentOffenceCode(request, errors)
  return errors
}

private fun validateRequiredFields(request: RiskScoreRequest, errors: MutableList<ValidationErrorResponse>) {
  val missingFields = arrayListOf<String>()

  SNSV_STATIC_REQUIRED_PROPERTIES.forEach { field -> missingFields.addIfNull(request, field) }

  if (missingFields.isNotEmpty()) {
    errors += ValidationErrorType.MISSING_INPUT.asErrorResponse(missingFields)
  }
}

fun isValidDynamicSnsv(request: RiskScoreRequest): Boolean = getNullValuesFromProperties(
  request,
  SNSV_DYNAMIC_ADDITIONAL_REQUIRED_PROPERTIES,
).isEmpty() &&
  hasDomesticViolencePerpetrator(request)

fun snsvDynamicValidation(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val errors = arrayListOf<ValidationErrorResponse>()
  if (!hasDomesticViolencePerpetrator(request)) {
    return addMissingFields(
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
  return errors
}

fun hasDomesticViolencePerpetrator(request: RiskScoreRequest) = getDomesticViolencePerpetrator(
  request.evidenceOfDomesticAbuse,
  request.domesticAbuseAgainstPartner,
) != null
