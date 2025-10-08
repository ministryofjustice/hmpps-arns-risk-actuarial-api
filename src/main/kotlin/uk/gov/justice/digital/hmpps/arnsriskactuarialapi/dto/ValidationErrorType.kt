package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

enum class ValidationErrorType(val message: String) {

  DOMESTIC_ABUSE_INCONSISTENT_INPUT("No evidence of domestic abuse identified - domesticAbuseAgainstPartner and domesticAbuseAgainstFamily should not be provided"),
  SEXUAL_OFFENDING_MISSING_COUNTS("Sexual motivation/offending identified - complete sexual offence counts"),
  SEXUAL_OFFENDING_INCONSISTENT_INPUT("No sexual motivation/offending identified - additional fields should not be provided"),
  NEED_DETAILS_OF_EXACT_OFFENCE("For this group of offences, the OGRS 3 offence category takes different values depending on the nature of the exact offence. Therefore, it is not possible to calculate an OGRS 3 score without details of the exact offence."),
  MISSING_MANDATORY_INPUT("Mandatory input field(s) missing"),
  AGE_AT_FIRST_SANCTION_AFTER_AGE_AT_CURRENT_CONVICTION("Age at first sanction must be before age at current conviction"),
  TOTAL_NUMBER_OF_SANCTIONS_LESS_THAN_ONE("Total number of sanctions must be one or greater"),
  AGE_AT_CURRENT_CONVICTION_LESS_THAN_TEN("Age at current conviction must be 10 or greater"),
  LDS_NOT_ENOUGH_FIELDS_PRESENT("At least three input fields must be provided"),
  OFFENCE_CODE_INCORRECT_FORMAT("Offence code must be a string of 5 digits"),
  OFFENCE_CODE_MAPPING_NOT_FOUND("No offence code to actuarial weighting mapping found for offence code"),
  COMPONENT_VALIDATION_ERROR("Validation error(s) in component scores"),
  UNEXPECTED_ERROR("An unexpected error occurred"),

  // TODO - All below are to be deprecated.
  NOT_APPLICABLE("ERR1 - Does not meet eligibility criteria"),
  MISSING_INPUT("ERR5 - Field is Null"),
  ;

  fun asErrorResponse(fields: List<String>): ValidationErrorResponse = ValidationErrorResponse(this, message, fields)

  fun asErrorResponseForUnexpectedError(message: String): ValidationErrorResponse = ValidationErrorResponse(
    this,
    "Unexpected error thrown during calculation, see logs for further details: $message",
    listOf(),
  )

  fun asErrorResponseForOffenceCodeMappingNotFound(offenceCode: String?, fields: List<String>): ValidationErrorResponse = ValidationErrorResponse(
    this,
    "No offence code to actuarial weighting mapping found for $offenceCode",
    fields,
  )
}
