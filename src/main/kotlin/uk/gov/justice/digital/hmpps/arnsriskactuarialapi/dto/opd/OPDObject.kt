package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

data class OPDObject(
  val opdEligibility: Boolean,
  val opdCheck: Boolean?,
  val validationError: List<ValidationErrorResponse>?,
)
