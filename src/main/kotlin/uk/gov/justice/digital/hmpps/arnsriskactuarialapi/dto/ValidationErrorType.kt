package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

enum class ValidationErrorType(val asErrorResponse: (List<String>) -> ValidationErrorResponse) {
  NOT_APPLICABLE({ fields -> ValidationErrorResponse(NOT_APPLICABLE, "ERR1 - Does not meet eligibility criteria", fields) }),
  BELOW_MIN_VALUE({ fields -> ValidationErrorResponse(BELOW_MIN_VALUE, "ERR2 - Below minimum value", fields) }),
  ABOVE_MAX_VALUE({ fields -> ValidationErrorResponse(ABOVE_MAX_VALUE, "ERR3 - Above minimum value", fields) }),
  NO_MATCHING_INPUT({ fields -> ValidationErrorResponse(NO_MATCHING_INPUT, "ERR4 - Does not match agreed input", fields) }),
  MISSING_INPUT({ fields -> ValidationErrorResponse(MISSING_INPUT, "ERR5 - Field is Null", fields) }),
  UNEXPECTED_VALUE({ fields -> ValidationErrorResponse(UNEXPECTED_VALUE, "ERR6 - Field is unexpected", fields) }),
}
