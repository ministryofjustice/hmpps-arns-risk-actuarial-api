package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
  fun `sanitisePercentage should default 0 with 1 and 100 with 99`() {
    assertEquals(1, (-1).sanitisePercentage())
    assertEquals(1, 0.sanitisePercentage())
    assertEquals(99, 100.sanitisePercentage())
    assertEquals(99, 121.sanitisePercentage())
    assertEquals(50, 50.sanitisePercentage())
    assertEquals(25, 25.sanitisePercentage())
  }
}
