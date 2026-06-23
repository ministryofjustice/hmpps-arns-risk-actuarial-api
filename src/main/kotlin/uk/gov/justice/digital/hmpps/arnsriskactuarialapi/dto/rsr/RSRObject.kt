package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.rsr

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError

data class RSRObject(
  val directContactSexualReoffendingPredictorBand: RiskBand? = null,
  val directContactSexualReoffendingPredictorScore: Double? = null,
  val imagesAndIndirectContactSexualReoffendingPredictorBand: RiskBand? = null,
  val imagesAndIndirectContactSexualReoffendingPredictorScore: Double? = null,
  val seriousViolentReoffendingPredictorScore: Double?,
  val rsrScore: Double? = null,
  val rsrBand: RiskBand? = null,
  val scoreType: StaticOrDynamic? = null,
  val riskReduction: Boolean? = null,
  val femaleVersion: Boolean?,
  val sexualOffenceHistory: Boolean?,
  val validationError: List<ValidationError>? = null,
)
