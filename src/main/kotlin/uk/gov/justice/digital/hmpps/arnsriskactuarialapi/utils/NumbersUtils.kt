package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.Period
import kotlin.collections.fold
import kotlin.math.exp
import kotlin.math.pow

fun Double.asDoublePercentage(): Double = BigDecimal(this).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP).toDouble()
fun Double.roundToNDecimals(n: Int): Double = BigDecimal(this).setScale(n, RoundingMode.HALF_UP).toDouble()
fun Double.roundTo5Decimals(): Double = roundToNDecimals(5)
fun Double.asPercentage(): Int = BigDecimal(this).multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).toInt()
fun Double.roundToInt(): Int = roundToNDecimals(0).toInt()

/**
 * This is business logic that does not accept 0% nor 100% instead they are defaulted to 1% and 99% respectively.
 */
fun Int.sanitisePercentage(): Int = when {
  this <= 0 -> 1
  this > 99 -> 99
  else -> this
}

fun Double.sigmoid(): Double = exp(this).let { it / (1 + it) }

fun calculatePolynomial(coeffs: DoubleArray, x: Double): Double = (0..<coeffs.size).fold(0.0) { sum, i -> sum + coeffs[i] * x.pow(i) }

fun getAgeAtDate(
  dateOfBirth: LocalDate,
  date: LocalDate,
  fieldName: String,
): Int {
  if (date.isEqual(dateOfBirth) || date.isBefore(dateOfBirth)) {
    throw IllegalArgumentException("$fieldName cannot be on or before date of birth.")
  }
  val ageAtCurrentConviction = Period.between(dateOfBirth, date).years
  return ageAtCurrentConviction
}
