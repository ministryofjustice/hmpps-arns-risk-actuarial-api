package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

data class MSTObject(
  val maturityScore: Int?,
  val maturityFlag: Boolean?,
  val isMstApplicable: Boolean?,
  val validationError: List<ValidationErrorResponse>?,
)
