package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api

data class ActuarialPredictorsResponse(
  val allPredictor: PredictorResponse<AllPredictorPredictorOutputResponse>,
  val violentPredictor: PredictorResponse<ViolentOrNonViolentPredictorPredictorOutputResponse>,
  val nonViolentPredictor: PredictorResponse<ViolentOrNonViolentPredictorPredictorOutputResponse>,
  val directContactSexualPredictor: PredictorResponse<DirectContactSexualPredictorOutputResponse>,
  val indirectContactSexualPredictor: PredictorResponse<IndirectContactSexualPredictorPredictorOutputResponse>,
  val seriousViolencePredictor: PredictorResponse<SeriousViolencePredictorPredictorOutputResponse>,
  val seriousPredictor: PredictorResponse<SeriousPredictorPredictorOutputResponse>,
)
