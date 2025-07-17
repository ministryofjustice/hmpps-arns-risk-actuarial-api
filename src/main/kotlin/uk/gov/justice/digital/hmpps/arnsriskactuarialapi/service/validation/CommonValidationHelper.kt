package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType

fun getTotalNumberOfSanctionsValidation(
  totalNumberOfSanctions: Integer?,
  errors: MutableList<ValidationErrorResponse>,
): MutableList<ValidationErrorResponse> {
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

fun getCurrentOffenceValidation(
  currentOffence: String?,
  errors: MutableList<ValidationErrorResponse>,
): MutableList<ValidationErrorResponse> {
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

fun addMissingFields(
  missingFields: MutableList<String>,
  errors: MutableList<ValidationErrorResponse>,
): MutableList<ValidationErrorResponse> {
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
