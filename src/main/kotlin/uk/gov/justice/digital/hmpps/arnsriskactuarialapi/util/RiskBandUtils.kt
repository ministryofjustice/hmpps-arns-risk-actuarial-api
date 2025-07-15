package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.util

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import kotlin.math.floor

fun getRiskBand(ogrs3TwoYear: Double): RiskBand {
  val percentage = floor(ogrs3TwoYear*100).toInt()
  return when (percentage) {
    in 0..49 -> RiskBand.LOW
    in 50..74 -> RiskBand.MEDIUM
    in 75..89 -> RiskBand.HIGH
    in 90..Int.MAX_VALUE -> RiskBand.VERY_HIGH
    else -> throw IllegalArgumentException("Unhandled ogrs3TwoYear percent: $percentage")
  }
}