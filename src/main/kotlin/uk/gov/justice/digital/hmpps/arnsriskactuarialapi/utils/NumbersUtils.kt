package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils

import java.math.BigDecimal
import java.math.RoundingMode

fun Double.roundTo5Decimals(): Double = BigDecimal(this).setScale(5, RoundingMode.HALF_UP).toDouble()
fun Double.asPercentage(): Int = BigDecimal(this).multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).toInt()

/**
 * This is business logic that does not accept 0% nor 100% instead they are defaulted to 1% and 99% respectively.
 */
fun Int.sanitisePercentage(): Int = when (this) {
  0 -> 1
  100 -> 99
  else -> this
}
