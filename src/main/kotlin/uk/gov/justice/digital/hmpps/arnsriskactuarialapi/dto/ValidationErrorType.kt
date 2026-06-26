package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

enum class ValidationErrorType(val message: String) {

  DATE_OF_CURRENT_CONVICTION_BEFORE_DATE_OF_BIRTH("Date of current conviction cannot be before date of birth"),
  DOMESTIC_ABUSE_INCONSISTENT_INPUT("No evidence of domestic abuse identified - domesticAbuseAgainstPartner and domesticAbuseAgainstFamily should not be provided"),
  SEXUAL_OFFENDING_MISSING_COUNTS("Sexual motivation/offending identified - complete sexual offence counts"),
  SEXUAL_OFFENDING_INCONSISTENT_INPUT("No sexual motivation/offending identified - additional fields should not be provided"),
  NEED_DETAILS_OF_EXACT_OFFENCE("For this group of offences, the OGRS 3 offence category takes different values depending on the nature of the exact offence. Therefore, it is not possible to calculate an OGRS 3 score without details of the exact offence."),
  MISSING_MANDATORY_INPUT("Mandatory input field(s) missing"),
  MISSING_DYNAMIC_INPUT("Dynamic input field(s) missing"),
  AGE_AT_FIRST_SANCTION_AFTER_AGE_AT_CURRENT_CONVICTION("Age at first sanction must be before age at current conviction"),
  DATE_OF_CURRENT_CONVICTION_WITHIN_THREE_MONTHS_OF_ASSESSMENT_DATE("Date of current conviction must be less than 3 months after the assessment date"),
  TOTAL_NUMBER_OF_SANCTIONS_OUT_OF_RANGE("Total number of sanctions must be between 1 and 999 (inclusive)"),
  VIOLENT_SANCTION_OUT_OF_RANGE("Violent sanctions count must be greater than 0, but less than or equal to total number of sanctions"),
  AGE_AT_FIRST_SANCTION_OUT_OF_RANGE("Age at current conviction must be between 9 and 98 (inclusive)"),
  LDS_NOT_ENOUGH_FIELDS_PRESENT("At least three input fields must be provided"),
  OFFENCE_CODE_INCORRECT_FORMAT("Offence code must be a string of 5 digits"),
  OFFENCE_CODE_MAPPING_NOT_FOUND("No offence code to actuarial weighting mapping found for offence code"),
  COMPONENT_VALIDATION_ERROR("Validation error(s) in component scores"),
  FOLLOW_UP_DATE_BEFORE_CONVICTION_DATE("Offender's date of commencement of community sentence or earliest possible release from custody is before conviction date"),
  UNEXPECTED_ERROR("An unexpected error occurred"),
  ;

  fun asError(fields: List<String>): ValidationError = ValidationError(this, message, fields)

  fun asErrorForUnexpectedError(message: String): ValidationError = ValidationError(
    this,
    "Unexpected error thrown during calculation, see logs for further details: $message",
    listOf(),
  )

  fun asErrorForOffenceCodeMappingNotFound(offenceCode: String?, fields: List<String>): ValidationError = ValidationError(
    this,
    "No offence code to actuarial weighting mapping found for $offenceCode",
    fields,
  )
}
