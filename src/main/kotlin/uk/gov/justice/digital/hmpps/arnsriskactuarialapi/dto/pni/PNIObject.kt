package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.PNIVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

data class PNIObject(
  val algorithmVersion: PNIVersion,
  val pniPathway: ProgrammeNeedIdentifier,
  val validationError: List<ValidationErrorResponse>?,
)
