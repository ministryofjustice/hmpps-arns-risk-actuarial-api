package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import java.math.BigDecimal

data class PredictorResponse<T : PredictorOutputResponse>(
  val algorithm: AlgorithmResponse,
  val type: ScoreTypeResponse?,
  val modelVersion: String = "1.0",
  val thresholdsVersion: String = "1.0",
  val output: T,
  val featureValues: Map<String, BigDecimal> = emptyMap(),
  val validationErrors: List<ValidationError>,
)
