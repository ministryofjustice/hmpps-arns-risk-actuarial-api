package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.combinedseriousreoffendingpredictor

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError

data class CombinedSeriousReoffendingPredictorObject(
  val directContactSexualReoffendingPredictorBand: RiskBand? = null,
  val directContactSexualReoffendingPredictorScore: Double? = null,
  val imagesAndIndirectContactSexualReoffendingPredictorBand: RiskBand? = null,
  val imagesAndIndirectContactSexualReoffendingPredictorScore: Double? = null,
  val seriousViolentReoffendingPredictorBand: RiskBand? = null,
  val seriousViolentReoffendingPredictorScore: Double?,
  val combinedSeriousReoffendingPredictorScore: Double? = null,
  val combinedSeriousReoffendingPredictorBand: RiskBand? = null,
  val scoreType: StaticOrDynamic? = null,
  val riskReduction: Boolean? = null,
  val femaleVersion: Boolean?,
  val sexualOffenceHistory: Boolean?,
  val validationError: List<ValidationError>? = null,
)
