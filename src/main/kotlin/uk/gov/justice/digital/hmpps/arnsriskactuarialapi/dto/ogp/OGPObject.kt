package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

data class OGPObject(
  val ogpReoffendingOneYear: Int?,
  val ogpReoffendingTwoYear: Int?,
  val bandOGP: RiskBand?,
  val totalOGPScore: Int?,
  val validationError: List<ValidationErrorResponse>?,
)
