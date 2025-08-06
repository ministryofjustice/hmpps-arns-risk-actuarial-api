package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.rsr

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

data class RSRObject(
  val ospdcBand: RiskBand?,
  val ospdcScore: Double?,
  val ospiicBand: RiskBand?,
  val ospiicScore: Double?,
  val rsrBand: RiskBand?,
  val scoreType: ScoreType?,
  val ospRiskReduction: Boolean?,
  val validationError: List<ValidationErrorResponse>?,
)
