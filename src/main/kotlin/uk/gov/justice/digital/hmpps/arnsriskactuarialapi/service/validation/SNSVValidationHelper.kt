package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
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

  // If the user requests a dynamic calculation, do the validation now so the calculation is skipped
  if (request.snsvStaticOrDynamic == StaticOrDynamic.DYNAMIC) {
    snsvDynamicValidation(request, errors)
  }
  validateCurrentOffenceCode(request, errors)
  validateSanctionCount(request, errors)
  return errors
}

fun isValidDynamicSnsv(request: RiskScoreRequest): Boolean = getNullValuesFromProperties(
  request,
  SNSV_DYNAMIC_ADDITIONAL_REQUIRED_PROPERTIES,
).isEmpty() &&
  hasDomesticViolencePerpetrator(request)

fun snsvDynamicValidation(request: RiskScoreRequest, errors: MutableList<ValidationErrorResponse>) {
  if (!isValidDynamicSnsv(request)) {
    val domesticViolencePerpetratorRequiredProperties = buildList {
      add(RiskScoreRequest::evidenceOfDomesticAbuse)
      if (request.evidenceOfDomesticAbuse == true) {
        add(RiskScoreRequest::domesticAbuseAgainstPartner)
      }
    }
    errors += addMissingFields(
      getNullValuesFromProperties(
        request,
        SNSV_DYNAMIC_ADDITIONAL_REQUIRED_PROPERTIES + domesticViolencePerpetratorRequiredProperties,
      ),
      emptyList(),
      isDynamic = true,
    )
  }
}

fun hasDomesticViolencePerpetrator(request: RiskScoreRequest) = getDomesticViolencePerpetrator(
  request.evidenceOfDomesticAbuse,
  request.domesticAbuseAgainstPartner,
) != null

fun validateSanctionCount(request: RiskScoreRequest, errors: MutableList<ValidationErrorResponse>) {
  val totalSanctions = request.totalNumberOfSanctionsForAllOffences
  val violentSanctions = request.totalNumberOfViolentSanctions
  if (totalSanctions != null && violentSanctions != null) {
    if (violentSanctions.toInt() > totalSanctions.toInt()) {
      errors += ValidationErrorType.VIOLENT_SANCTION_GREATER_THAN_TOTAL_SANCTIONS.asErrorResponse(
        listOf(
          RiskScoreRequest::totalNumberOfSanctionsForAllOffences.name,
          RiskScoreRequest::totalNumberOfViolentSanctions.name,
        ),
      )
    }
  }
}
