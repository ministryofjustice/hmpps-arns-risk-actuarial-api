package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

enum class ValidationErrorType(val asErrorResponse: (List<String>) -> ValidationErrorResponse) {

  MISSING_MANDATORY_INPUT({ fields -> ValidationErrorResponse(MISSING_MANDATORY_INPUT, "Mandatory input field(s) missing", fields) }),
  AGE_AT_FIRST_SANCTION_AFTER_AGE_AT_CURRENT_CONVICTION({ fields -> ValidationErrorResponse(AGE_AT_FIRST_SANCTION_AFTER_AGE_AT_CURRENT_CONVICTION, "Age at first sanction must be before age at current conviction", fields) }),
  TOTAL_NUMBER_OF_SANCTIONS_LESS_THAN_ONE({ fields -> ValidationErrorResponse(TOTAL_NUMBER_OF_SANCTIONS_LESS_THAN_ONE, "Total number of sanctions must be one or greater", fields) }),
  AGE_AT_CURRENT_CONVICTION_LESS_THAN_TEN({ fields -> ValidationErrorResponse(AGE_AT_CURRENT_CONVICTION_LESS_THAN_TEN, "Age at current conviction must be 10 or greater", fields) }),
  LDS_NOT_ENOUGH_FIELDS_PRESENT({ fields -> ValidationErrorResponse(LDS_NOT_ENOUGH_FIELDS_PRESENT, "At least three input fields must be provided", fields) }),
  OFFENCE_CODE_INCORRECT_FORMAT({ fields -> ValidationErrorResponse(OFFENCE_CODE_INCORRECT_FORMAT, "Offence code must be a string of 5 digits", fields) }),
  OFFENCE_CODE_MAPPING_NOT_FOUND({ fields -> ValidationErrorResponse(OFFENCE_CODE_MAPPING_NOT_FOUND, "No offence code to actuarial weighting mapping found for offence code", fields) }),

  // Potential Legacy
  NOT_APPLICABLE({ fields -> ValidationErrorResponse(NOT_APPLICABLE, "ERR1 - Does not meet eligibility criteria", fields) }),
  BELOW_MIN_VALUE({ fields -> ValidationErrorResponse(BELOW_MIN_VALUE, "ERR2 - Below minimum value", fields) }),
  ABOVE_MAX_VALUE({ fields -> ValidationErrorResponse(ABOVE_MAX_VALUE, "ERR3 - Above minimum value", fields) }),
  NO_MATCHING_INPUT({ fields -> ValidationErrorResponse(NO_MATCHING_INPUT, "ERR4 - Does not match agreed input", fields) }),
  MISSING_INPUT({ fields -> ValidationErrorResponse(MISSING_INPUT, "ERR5 - Field is Null", fields) }),
  UNEXPECTED_VALUE({ fields -> ValidationErrorResponse(UNEXPECTED_VALUE, "ERR6 - Field is unexpected", fields) }),
}
