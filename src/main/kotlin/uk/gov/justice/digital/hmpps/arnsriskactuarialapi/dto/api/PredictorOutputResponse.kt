package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api

abstract class PredictorOutputResponse(
  open val band: RiskBandResponse?,
)

data class AllPredictorPredictorOutputResponse(
  override val band: RiskBandResponse?,
  val oneYearScore: Int?,
  val twoYearScore: Int?,
) : PredictorOutputResponse(band)

data class ViolentOrNonViolentPredictorPredictorOutputResponse(
  override val band: RiskBandResponse?,
  val oneYearScore: Int?,
  val twoYearScore: Int?,
  val pointScore: Int?,
) : PredictorOutputResponse(band)

data class DirectContactSexualPredictorOutputResponse(
  override val band: RiskBandResponse?,
  val pointScore: Int?,
  val score: Double?,
  val femaleVersion: Boolean?,
  val hasSexualOffenceHistory: Boolean?,
  val riskBandReductionApplied: Boolean?,
) : PredictorOutputResponse(band)

data class IndirectContactSexualPredictorPredictorOutputResponse(
  override val band: RiskBandResponse?,
  val score: Double?,
  val femaleVersion: Boolean?,
  val hasSexualOffenceHistory: Boolean?,
) : PredictorOutputResponse(band)

data class SeriousViolencePredictorPredictorOutputResponse(
  override val band: RiskBandResponse?,
  val score: Double?,
) : PredictorOutputResponse(band)

data class SeriousPredictorPredictorOutputResponse(
  override val band: RiskBandResponse?,
  val overallScore: Double?,
  val femaleVersion: Boolean?,
  val hasSexualOffenceHistory: Boolean?,
  val componentScores: SeriousPredictorComponentScores,
) : PredictorOutputResponse(band)

data class SeriousPredictorComponentScores(
  val directContactSexualPredictorScore: PredictorResponse<DirectContactSexualPredictorOutputResponse>,
  val indirectContactSexualPredictorScore: PredictorResponse<IndirectContactSexualPredictorPredictorOutputResponse>,
  val seriousViolencePredictorScore: PredictorResponse<SeriousViolencePredictorPredictorOutputResponse>,
)
