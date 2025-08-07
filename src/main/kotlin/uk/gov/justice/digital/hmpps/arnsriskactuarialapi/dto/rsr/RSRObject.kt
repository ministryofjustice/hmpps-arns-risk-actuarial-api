package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.rsr

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.ScoreType

data class RSRObject(
  val ospdcBand: RiskBand?,
  val ospdcScore: Double?,
  val ospiicBand: RiskBand?,
  val ospiicScore: Double?,
  val rsrBand: RiskBand?,
  val rsrScore: Int?,
  val scoreType: ScoreType?,
  val ospRiskReduction: Boolean?,
  val validationError: List<ValidationErrorResponse>?,
)
