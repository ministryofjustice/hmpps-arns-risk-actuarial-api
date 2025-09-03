package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.osp

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

data class OSPDCObject(
  val ospdcBand: RiskBand?,
  val ospdcScore: Double?,
  val ospRiskReduction: Boolean?,
  val validationError: List<ValidationErrorResponse>?,
)
