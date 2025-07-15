package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

data class OGRS3Object(
  val algorithmVersion: String,
  val ogrs3OneYear: Double?,
  val ogrs3TwoYear: Double?,
  val band: RiskBand?,
  val validationError: List<ValidationErrorResponse>?,
)
