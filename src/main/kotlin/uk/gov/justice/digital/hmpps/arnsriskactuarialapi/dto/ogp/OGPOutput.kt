package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

data class OGPOutput(
  val algorithmVersion: String,
  val nonOvpReoffendingOneYear: Double?,
  val nonOvpReoffendingTwoYear: Double?,
  val bandOGP: OGPBand?,
  val validationError: List<ValidationErrorResponse>?,
)
