package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.rsr

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.ScoreType

data class RSRObject(
  val ospdcBand: RiskBand? = null,
  val ospdcScore: Double? = null,
  val ospiicBand: RiskBand? = null,
  val ospiicScore: Double? = null,
  val snsvScore: Double?,
  val rsrScore: Double? = null,
  val rsrBand: RiskBand? = null,
  val scoreType: ScoreType? = null,
  val ospRiskReduction: Boolean? = null,
  val femaleVersion: Boolean?,
  val sexualOffenceHistory: Boolean?,
  val validationError: List<ValidationErrorResponse>? = null,
)
