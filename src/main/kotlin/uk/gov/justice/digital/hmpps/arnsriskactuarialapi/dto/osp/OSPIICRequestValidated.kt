package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.osp

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender

data class OSPIICRequestValidated(
  val gender: Gender,
  val hasCommittedSexualOffence: Boolean,
  val totalIndecentImageSanctions: Int,
  val totalContactChildSexualSanctions: Int,
)
