package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.osp

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

data class OSPIICObject(
  val ospiicBand: RiskBand?,
  val ospiicScore: Double?,
  val validationError: List<ValidationErrorResponse>?,
)
