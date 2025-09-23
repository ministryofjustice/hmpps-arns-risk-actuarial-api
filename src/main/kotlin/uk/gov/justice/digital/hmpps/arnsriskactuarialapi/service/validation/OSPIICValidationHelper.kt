package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType

val OSPIIC_MALE_REQUIRED_PROPERTIES = listOf(
  RiskScoreRequest::totalContactAdultSexualSanctions,
  RiskScoreRequest::totalContactChildSexualSanctions,
  RiskScoreRequest::totalIndecentImageSanctions,
  RiskScoreRequest::totalNonContactSexualOffences,
)

fun validateOSPIIC(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val errors = mutableListOf<ValidationErrorResponse>()
  validateRequiredFields(request, errors)
  return errors
}

private fun validateRequiredFields(request: RiskScoreRequest, errors: MutableList<ValidationErrorResponse>) {
  val missingFields = arrayListOf<String>()

  when (request.gender) {
    Gender.MALE -> validateMaleRequiredFields(request)
    Gender.FEMALE -> emptyList()
    null -> listOf(RiskScoreRequest::gender.name)
  }.let(missingFields::addAll)

  if (Gender.MALE == request.gender) {
    validateMaleSexualOffences(request, errors)
  }

  if (missingFields.isNotEmpty()) {
    errors += ValidationErrorType.MISSING_MANDATORY_INPUT.asErrorResponse(missingFields)
  }
}

private fun validateMaleRequiredFields(request: RiskScoreRequest): List<String> {
  val missingFields = arrayListOf<String>()
  OSPIIC_MALE_REQUIRED_PROPERTIES.filter { it.get(request) == null }
    .forEach { missingFields.add(it.name) }

  return missingFields
}

private fun validateMaleSexualOffences(request: RiskScoreRequest, errors: MutableList<ValidationErrorResponse>) {
  if (request.hasEverCommittedSexualOffence == true &&
    request.totalContactAdultSexualSanctions == 0 &&
    request.totalContactChildSexualSanctions == 0 &&
    request.totalIndecentImageSanctions == 0 &&
    request.totalNonContactSexualOffences == 0
  ) {
    errors += ValidationErrorType.SEXUAL_OFFENDING_MISSING_COUNTS.asErrorResponse(
      listOf(
        RiskScoreRequest::totalContactAdultSexualSanctions.name,
        RiskScoreRequest::totalContactChildSexualSanctions.name,
        RiskScoreRequest::totalIndecentImageSanctions.name,
        RiskScoreRequest::totalNonContactSexualOffences.name,
      ),
    )
  }
}

fun validateMaleSexualOffencesInconsistentFields(request: RiskScoreRequest, errors: MutableList<ValidationErrorResponse>) {
  if (request.hasEverCommittedSexualOffence == false) {
    val unexpectedFields = arrayListOf<String>()

    if (request.totalContactAdultSexualSanctions != null && request.totalContactAdultSexualSanctions != 0) {
      unexpectedFields.add(RiskScoreRequest::totalContactAdultSexualSanctions.name)
    }
    if (request.totalContactChildSexualSanctions != null && request.totalContactChildSexualSanctions != 0) {
      unexpectedFields.add(RiskScoreRequest::totalContactChildSexualSanctions.name)
    }
    if (request.totalIndecentImageSanctions != null && request.totalIndecentImageSanctions != 0) {
      unexpectedFields.add(RiskScoreRequest::totalIndecentImageSanctions.name)
    }
    if (request.totalNonContactSexualOffences != null && request.totalNonContactSexualOffences != 0) {
      unexpectedFields.add(RiskScoreRequest::totalNonContactSexualOffences.name)
    }
    if (unexpectedFields.isNotEmpty()) {
      errors += ValidationErrorType.SEXUAL_OFFENDING_INCONSISTENT_INPUT.asErrorResponse(unexpectedFields)
    }
  }
}
