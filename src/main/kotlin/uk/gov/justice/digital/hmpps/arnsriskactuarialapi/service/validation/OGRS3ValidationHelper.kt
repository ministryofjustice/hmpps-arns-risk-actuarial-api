package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import kotlin.reflect.KProperty1

private const val MIN_CONVICTION_AGE = 10

val OGRS3_REQUIRED_FIELDS: List<KProperty1<RiskScoreRequest, Any?>> = listOf(
  RiskScoreRequest::gender,
  RiskScoreRequest::dateOfBirth,
  RiskScoreRequest::dateOfCurrentConviction,
  RiskScoreRequest::dateAtStartOfFollowupCalculated,
  RiskScoreRequest::totalNumberOfSanctionsForAllOffences,
  RiskScoreRequest::ageAtFirstSanction,
  RiskScoreRequest::currentOffenceCode,
)

fun validateOGRS3(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val errors = mutableListOf<ValidationErrorResponse>()
  validateRequiredFields(request, errors, OGRS3_REQUIRED_FIELDS)
  validateTotalNumberOfSanctionsForAllOffences(request, errors)
  validateCurrentOffenceCode(request, errors)
  return errors
}

fun validateAgeAtCurrentConviction(ageAtCurrentConviction: Int): ValidationErrorResponse? = if (ageAtCurrentConviction < MIN_CONVICTION_AGE) {
  ValidationErrorType.AGE_AT_CURRENT_CONVICTION_LESS_THAN_TEN.asErrorResponse(
    listOf(
      RiskScoreRequest::dateOfBirth.name,
      RiskScoreRequest::dateOfCurrentConviction.name,
    ),
  )
} else {
  null
}

fun validateAgeAtFirstSanction(ageAtFirstSanction: Int, ageAtCurrentConviction: Int): ValidationErrorResponse? = if (ageAtFirstSanction > ageAtCurrentConviction) {
  ValidationErrorType.AGE_AT_FIRST_SANCTION_AFTER_AGE_AT_CURRENT_CONVICTION.asErrorResponse(
    listOf(
      RiskScoreRequest::dateOfBirth.name,
      RiskScoreRequest::dateOfCurrentConviction.name,
      RiskScoreRequest::ageAtFirstSanction.name,
    ),
  )
} else {
  null
}
