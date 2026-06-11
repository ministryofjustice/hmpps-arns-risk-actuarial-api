package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError

data class SNSVObject(
  val snsvScore: Double?,
  val scoreType: ScoreType?,
  val validationError: List<ValidationError>?,
  val featureValues: Map<String, Double>?,
)
