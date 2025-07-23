package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

data class PNIObject(
  val algorithmVersion: String,
  val pniPathway: ProgrammeNeedIdentifier,
  val validationError: List<ValidationErrorResponse>?,
)
