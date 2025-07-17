package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

data class OGRS3Object(
  val algorithmVersion: String,
  val ogrs3OneYear: Int?,
  val ogrs3TwoYear: Int?,
  val band: RiskBand?,
  val validationError: List<ValidationErrorResponse>?,
)
