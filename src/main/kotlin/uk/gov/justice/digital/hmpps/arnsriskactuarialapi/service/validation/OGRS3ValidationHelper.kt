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
  validateDisallowedCurrentOffenceCode(request)
  return errors
}

fun validateDisallowedCurrentOffenceCode(request: RiskScoreRequest) {
  if (request.currentOffenceCode != null && request.currentOffenceCode == "00000") {
    throw IllegalArgumentException("Offence code cannot be '${request.currentOffenceCode}'")
  }
}

fun validateAgeAtCurrentConviction(ageAtCurrentConviction: Int, errors: MutableList<ValidationErrorResponse>) {
  if (ageAtCurrentConviction < MIN_CONVICTION_AGE) {
    errors += ValidationErrorType.AGE_AT_CURRENT_CONVICTION_LESS_THAN_TEN.asErrorResponse(
      listOf(
        RiskScoreRequest::dateOfBirth.name,
        RiskScoreRequest::dateOfCurrentConviction.name,
      ),
    )
  }
}

fun validateAgeAtFirstSanction(
  ageAtFirstSanction: Int,
  ageAtCurrentConviction: Int,
  errors: MutableList<ValidationErrorResponse>,
) {
  if (ageAtFirstSanction > ageAtCurrentConviction) {
    errors += ValidationErrorType.AGE_AT_FIRST_SANCTION_AFTER_AGE_AT_CURRENT_CONVICTION.asErrorResponse(
      listOf(
        RiskScoreRequest::dateOfBirth.name,
        RiskScoreRequest::dateOfCurrentConviction.name,
        RiskScoreRequest::ageAtFirstSanction.name,
      ),
    )
  }
}
