package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cosh
import kotlin.math.exp

class NumbersUtilsTest {

  companion object {
    const val SCALE = 2
    fun Double.equalsDelta(other: Double) = BigDecimal(abs(this / other - 1)).setScale(SCALE, RoundingMode.HALF_UP)
      .equals(BigDecimal(0).setScale(SCALE, RoundingMode.HALF_UP))

    // for Horner's rule
    val cubic = doubleArrayOf(-1.0, 0.0, 0.0, 1.0)
    val linear = doubleArrayOf(-1.0, 1.0)
    val quadratic = doubleArrayOf(1.0, 1.0, 1.0)

    private val today = LocalDate.of(2025, 1, 1)
  }

  @Test
  fun `roundTo5Decimals should round correctly`() {
    assertEquals(1.23457, 1.23456789.roundTo5Decimals())
    assertEquals(1.23456, 1.23456123.roundTo5Decimals())
    assertEquals(0.0, 0.0.roundTo5Decimals())
  }

  @Test
  fun `asPercentage should convert decimal to int percentage correctly`() {
    assertEquals(50, 0.5.asPercentage())
    assertEquals(50, 0.49678.asPercentage())
    assertEquals(100, 0.996.asPercentage())
    assertEquals(100, 1.0.asPercentage())
    assertEquals(0, 0.004.asPercentage())
    assertEquals(99, 0.994.asPercentage())
  }

  @Test
  fun `asDoublePercentage should convert decimal to 2 decimal percentage correctly`() {
    assertEquals(50.00, 0.5.asDoublePercentage())
    assertEquals(49.68, 0.49678.asDoublePercentage())
    assertEquals(99.60, 0.996.asDoublePercentage())
    assertEquals(100.00, 1.0.asDoublePercentage())
    assertEquals(0.40, 0.004.asDoublePercentage())
    assertEquals(99.40, 0.994.asDoublePercentage())
    assertEquals(2.88, 0.02875687371686357.asDoublePercentage())
  }

  @Test
  fun `sanitisePercentage should default 0 with 1 and 100 with 99`() {
    assertEquals(1, (-1).sanitisePercentage())
    assertEquals(1, 0.sanitisePercentage())
    assertEquals(99, 100.sanitisePercentage())
    assertEquals(99, 121.sanitisePercentage())
    assertEquals(50, 50.sanitisePercentage())
    assertEquals(25, 25.sanitisePercentage())
  }

  @Test
  fun `rounding should round correctly`() {
    assertEquals(1, 1.23456789.roundToInt())
    assertEquals(2, 1.500000000001.roundToInt())
  }

  @Test
  fun `sigmoid should scale correctly`() {
    assertTrue(0.5.equalsDelta(0.0.sigmoid()))
    // should be true for any x (but delta may have to be tweaked)
    val x = Math.PI
    val g = { x: Double -> (x / 2).let { u -> exp(u) / (2 * cosh(u)) } }
    val f = { x: Double -> x.sigmoid() }
    assertTrue(f(x).equalsDelta(g(x)))
  }

  @Test
  fun `polynomial calculations`() {
    assertEquals(7.0, calculatePolynomial(cubic, 2.0), 0.00001)
    assertEquals(PI - 1, calculatePolynomial(linear, PI), 0.00001)
    assertEquals(21.0, calculatePolynomial(quadratic, 4.0), 0.00001)
    // should be true for any x
    val x = PI
    val productOfPolynomials = { x: Double -> calculatePolynomial(linear, x) * calculatePolynomial(quadratic, x) }
    val cubicPolynomial = { x: Double -> calculatePolynomial(cubic, x) }
    assertEquals(productOfPolynomials(x), cubicPolynomial(x), 0.00001)
  }

  @Test
  fun `getAgeAtDate exact`() {
    val dob = today.minusYears(15)
    assertEquals(
      15,
      getAgeAtDate(dob, today, "fieldName"),
    )
  }

  @Test
  fun `getAgeAtDate should round down months`() {
    val dob = today.minusYears(10).minusMonths(3)
    val result = getAgeAtDate(dob, today, "fieldName")
    assertEquals(10, result)
  }

  @Test
  fun `getAgeAtDate date before dateOfBirth should return error`() {
    val result = runCatching {
      getAgeAtDate(today, today.minusYears(10), "fieldName")
    }
    assertTrue(result.isFailure)
    assertEquals("fieldName cannot be on or before date of birth.", result.exceptionOrNull()?.message)
  }

  @Test
  fun `getAgeAtDate date equal to dateOfBirth should return error`() {
    val result = runCatching {
      getAgeAtDate(today, today, "fieldName")
    }
    assertTrue(result.isFailure)
    assertEquals("fieldName cannot be on or before date of birth.", result.exceptionOrNull()?.message)
  }
}
