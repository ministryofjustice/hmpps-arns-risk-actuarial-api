package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api

data class ActuarialPredictorsResponse(
  val allPredictor: PredictorResponse,
  val violentPredictor: PredictorResponse,
  val nonViolentPredictor: PredictorResponse,
  val directContactSexualPredictor: PredictorResponse,
  val indirectContactSexualPredictor: PredictorResponse,
  val seriousViolencePredictor: PredictorResponse,
  val seriousPredictor: PredictorResponse,
)
