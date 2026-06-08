package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.allreoffendingpredictor

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import java.math.BigDecimal

data class AllReoffendingPredictorObject(
  val twoYearScore: Double?,
  val band: RiskBand?,
  val staticOrDynamic: StaticOrDynamic?,
  var validationError: List<ValidationErrorResponse>?,
  val featureValues: Map<String, BigDecimal>?,
)