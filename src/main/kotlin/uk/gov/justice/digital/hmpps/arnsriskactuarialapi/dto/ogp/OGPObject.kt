package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

data class OGPObject(
  val algorithmVersion: String,
  val ogpReoffendingOneYear: Int?,
  val ogpReoffendingTwoYear: Int?,
  val bandOGP: OGPBand?,
  val totalOGPScore: Int?,
  val validationError: List<ValidationErrorResponse>?,
)
