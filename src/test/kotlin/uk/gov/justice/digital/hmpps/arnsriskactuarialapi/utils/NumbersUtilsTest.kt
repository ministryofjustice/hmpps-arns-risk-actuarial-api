package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.AllReoffendingPredictorStatic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.ViolentReoffendingPredictorDynamic
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cosh
import kotlin.math.exp

class NumbersUtilsTest {
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

  @ParameterizedTest
  @MethodSource("calculatePolynomialProvider")
  fun `should calculate polynomial with all powers`(
    coefficients: Array<BigDecimal>,
    x: BigDecimal,
    expectedResult: BigDecimal,
  ) {
    // Act
    val result = calculatePolynomial(coefficients, x)

    // Assert
    assertTrue(expectedResult.compareTo(result) == 0) {
      "Expected $expectedResult, but got $result"
    }
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

  companion object {
    const val SCALE = 2
    fun Double.equalsDelta(other: Double) = BigDecimal(abs(this / other - 1)).setScale(SCALE, RoundingMode.HALF_UP)
      .equals(BigDecimal(0).setScale(SCALE, RoundingMode.HALF_UP))

    private val today = LocalDate.of(2025, 1, 1)

    @JvmStatic
    fun calculatePolynomialProvider() = listOf(
      Arguments.of(
        arrayOf(
          ViolentReoffendingPredictorDynamic.AAI_MALE.coefficient,
          ViolentReoffendingPredictorDynamic.AAI_QUADRATIC_MALE.coefficient,
        ),
        BigDecimal(2),
        BigDecimal("-0.1271894341895465959518807252948136010672897100448608398437500"),
      ),
      Arguments.of(
        arrayOf(
          AllReoffendingPredictorStatic.AAI_MALE.coefficient,
          AllReoffendingPredictorStatic.AAI_QUADRATIC_MALE.coefficient,
          AllReoffendingPredictorStatic.AAI_CUBIC_MALE.coefficient,
          AllReoffendingPredictorStatic.AAI_QUARTIC_MALE.coefficient,
        ),
        BigDecimal.TWO,
        BigDecimal("-0.28030216260947281525577160247661434588906104181660339236259460449218750000"),
      ),
      Arguments.of(
        arrayOf(
          BigDecimal.ONE,
          BigDecimal.TWO,
        ),
        BigDecimal.TWO,
        BigDecimal(10),
      ),
      Arguments.of(
        arrayOf(
          BigDecimal.ONE,
          BigDecimal.TWO,
          BigDecimal(3),
        ),
        BigDecimal.TWO,
        BigDecimal(34),
      ),
      Arguments.of(
        arrayOf(
          BigDecimal.ONE,
          BigDecimal.TWO,
          BigDecimal(3),
          BigDecimal(4),
        ),
        BigDecimal.TWO,
        BigDecimal(98),
      ),
    )
  }
}
