package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.directContactSexualReoffendingPredictor

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import java.math.BigDecimal

data class DirectContactSexualReoffendingPredictorObject(
  val band: RiskBand?,
  val score: Double?,
  val pointScore: Int?,
  val riskReduction: Boolean?,
  val femaleVersion: Boolean?,
  val sexualOffenceHistory: Boolean?,
  val validationError: List<ValidationError>?,
  val featureValues: Map<String, BigDecimal>?,
)
