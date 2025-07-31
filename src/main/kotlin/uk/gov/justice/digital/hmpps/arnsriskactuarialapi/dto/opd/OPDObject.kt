package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

data class OPDObject(
  val opdCheck: Boolean,
  val opdResult: OPDResult?,
  val validationError: List<ValidationErrorResponse>?,
)
