package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

data class ValidationErrorResponse(
  val type: ValidationErrorType,
  val message: String,
  val fields: List<String>?,
)
