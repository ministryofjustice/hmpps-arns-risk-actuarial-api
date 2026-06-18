package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError

data class PNIObject(
  val pniPathway: ProgrammeNeedIdentifier,
  val validationError: List<ValidationError>?,
)
