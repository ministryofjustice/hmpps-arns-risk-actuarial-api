package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

enum class ValidationErrorType {
  MISSING_INPUT,
  BELOW_MIN_VALUE,
  ABOVE_MAX_VALUE,
  NO_MATCHING_INPUT,
  NOT_APPLICABLE,
  UNEXPECTED_VALUE,
}
