package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3.OGRS3Object

private const val MIN_CONVICTION_AGE = 10

fun validateOGRS3(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val errors = mutableListOf<ValidationErrorResponse>()
  validateRequiredFields(request, errors)
  validateTotalNumberOfSanctionsForAllOffences(request, errors)
  validateCurrentOffenceCode(request, errors)
  return errors
}

private fun validateRequiredFields(request: RiskScoreRequest, errors: MutableList<ValidationErrorResponse>) {
  val missingFields = arrayListOf<String>()

  missingFields.addIfNull(request, RiskScoreRequest::gender)
  missingFields.addIfNull(request, RiskScoreRequest::dateOfBirth)
  missingFields.addIfNull(request, RiskScoreRequest::dateOfCurrentConviction)
  missingFields.addIfNull(request, RiskScoreRequest::dateAtStartOfFollowup)
  missingFields.addIfNull(request, RiskScoreRequest::totalNumberOfSanctionsForAllOffences)
  missingFields.addIfNull(request, RiskScoreRequest::ageAtFirstSanction)
  missingFields.addIfNull(request, RiskScoreRequest::currentOffenceCode)

  if (missingFields.isNotEmpty()) {
    errors += ValidationErrorType.MISSING_INPUT.asErrorResponse(missingFields)
  }
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

fun returnOGRS3ObjectWithError(validationErrorResponse: ValidationErrorResponse): OGRS3Object = OGRS3Object(
  null,
  null,
  null,
  listOf(validationErrorResponse),
)
