package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType

fun ogrs3InitialValidation(request: RiskScoreRequest): MutableList<ValidationErrorResponse> {
  val missingFieldValidationErrorStep = getMissingFieldsValidation(request)
  val totalSanctionsValidationErrorStep = getTotalNumberOfSanctionsValidation(request.totalNumberOfSanctions, missingFieldValidationErrorStep)
  val currentOffenceValidationErrorStep = getCurrentOffenceValidation(request.currentOffence, totalSanctionsValidationErrorStep)
  return currentOffenceValidationErrorStep
}

fun getMissingFieldsValidation(request: RiskScoreRequest): MutableList<ValidationErrorResponse> {
  val errors = mutableListOf<ValidationErrorResponse>()

  val missingFields = mutableListOf<String>()

  if (request.gender == null) missingFields.add("Gender")
  if (request.dateOfBirth == null) missingFields.add("Date of birth")
  if (request.dateOfCurrentConviction == null) missingFields.add("Date of current conviction")
  if (request.dateAtStartOfFollowup == null) missingFields.add("Date at start of followup")
  if (request.totalNumberOfSanctions == null) missingFields.add("Total number of sanctions")
  if (request.ageAtFirstSanction == null) missingFields.add("Age at first sanction")
  if (request.currentOffence == null) missingFields.add("Current offence")

  if (missingFields.isNotEmpty()) {
    errors.add(
      ValidationErrorResponse(
        type = ValidationErrorType.MISSING_INPUT,
        message = "ERR5 - Field is Null",
        fields = missingFields,
      ),
    )
  }
  return errors
}

fun getTotalNumberOfSanctionsValidation(totalNumberOfSanctions: Integer?, errors: MutableList<ValidationErrorResponse>): MutableList<ValidationErrorResponse> {
  if (totalNumberOfSanctions != null) {
    if (totalNumberOfSanctions < 1) {
      errors.add(
        ValidationErrorResponse(
          type = ValidationErrorType.BELOW_MIN_VALUE,
          message = "ERR2 - Below minimum value",
          fields = listOf("Total number of sanctions"),
        ),
      )
    }
  }
  return errors
}

fun getCurrentOffenceValidation(currentOffence: String?, errors: MutableList<ValidationErrorResponse>): MutableList<ValidationErrorResponse> {
  if (currentOffence != null) {
    if (currentOffence.length != 5) {
      errors.add(
        ValidationErrorResponse(
          type = ValidationErrorType.NO_MATCHING_INPUT,
          message = "ERR4 - Does not match agreed input",
          fields = listOf("Current offence"),
        ),
      )
    }
  }
  return errors
}
