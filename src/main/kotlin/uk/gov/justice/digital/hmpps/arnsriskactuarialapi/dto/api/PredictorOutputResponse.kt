package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api

abstract class PredictorOutputResponse(
  val band: RiskBandResponse?,
)

// TODO What's a better way of doing this?
class AlphaPredictorOutputResponse(band: RiskBandResponse?, val oneYearScore: Int?, val twoYearScore: Int?) : PredictorOutputResponse(band)
class BetaPredictorOutputResponse(band: RiskBandResponse?, val oneYearScore: Int?, val twoYearScore: Int?, val pointScore: Int?) : PredictorOutputResponse(band)
class CharliePredictorOutputResponse(band: RiskBandResponse?, val pointScore: Int?, val score: Double?, val femaleVersion: Boolean?, val hasSexualOffenceHistory: Boolean?, val riskBandReductionApplied: Boolean?) : PredictorOutputResponse(band)
class DeltaPredictorOutputResponse(band: RiskBandResponse?, val score: Double?, val femaleVersion: Boolean?, val hasSexualOffenceHistory: Boolean?) : PredictorOutputResponse(band)
class EchoPredictorOutputResponse(band: RiskBandResponse?, val score: Double?) : PredictorOutputResponse(band)
class FoxtrotPredictorOutputResponse(band: RiskBandResponse?, val overallScore: Double?, val femaleVersion: Boolean?, val hasSexualOffenceHistory: Boolean?, val componentScores: List<PredictorResponse>) : PredictorOutputResponse(band)
