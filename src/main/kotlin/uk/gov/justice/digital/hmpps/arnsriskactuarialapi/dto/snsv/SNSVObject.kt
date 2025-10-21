package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

data class SNSVObject(
  val snsvScore: Double?,
  val scoreType: ScoreType?,
  val validationError: List<ValidationErrorResponse>?,
  val featureValues: Map<String, Double>?,
)
