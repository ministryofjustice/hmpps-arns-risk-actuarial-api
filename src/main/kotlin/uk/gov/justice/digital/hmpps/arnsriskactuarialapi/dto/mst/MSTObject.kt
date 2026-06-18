package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError

data class MSTObject(
  val maturityScore: Int?,
  val maturityFlag: Boolean?,
  val isMstApplicable: Boolean?,
  val validationError: List<ValidationError>?,
)
