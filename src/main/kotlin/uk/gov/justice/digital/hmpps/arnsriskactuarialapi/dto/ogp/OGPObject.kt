package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

data class OGPObject(
  val algorithmVersion: String,
  val ogpReoffendingOneYear: Double?,
  val ogpReoffendingTwoYear: Double?,
  val bandOGP: OGPBand?,
  val validationError: List<ValidationErrorResponse>?,
)
