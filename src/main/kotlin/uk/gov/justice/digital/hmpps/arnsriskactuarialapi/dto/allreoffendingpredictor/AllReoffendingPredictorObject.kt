package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.allreoffendingpredictor

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import java.math.BigDecimal

data class AllReoffendingPredictorObject(
  val score: Double?,
  val band: RiskBand?,
  val staticOrDynamic: StaticOrDynamic?,
  val validationErrors: List<ValidationError>?,
  val featureValues: Map<String, BigDecimal>?,
)
