package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OGPBandTest {

  @Test
  fun `enum constants are set correctly`() {
    assertEquals(
      mapOf(
        OGPBand.LOW to 1..33,
        OGPBand.MEDIUM to 34..66,
        OGPBand.HIGH to 67..84,
        OGPBand.VERY_HIGH to 85..99,
      ),
      OGPBand.entries.associateWith { grading -> grading.bounds },
    )
  }

  @Test
  fun `bands are found correctly`() {
    assertEquals(OGPBand.LOW, OGPBand.findBand(1))
    assertEquals(OGPBand.LOW, OGPBand.findBand(15))
    assertEquals(OGPBand.LOW, OGPBand.findBand(33))
    assertEquals(OGPBand.MEDIUM, OGPBand.findBand(34))
    assertEquals(OGPBand.MEDIUM, OGPBand.findBand(66))
    assertEquals(OGPBand.HIGH, OGPBand.findBand(67))
    assertEquals(OGPBand.HIGH, OGPBand.findBand(84))
    assertEquals(OGPBand.VERY_HIGH, OGPBand.findBand(85))
    assertEquals(OGPBand.VERY_HIGH, OGPBand.findBand(99))
  }

  @Test
  fun `out of range values`() {
    var exception = assertFailsWith<IllegalArgumentException>(
      block = { OGPBand.findBand(0) },
    )
    assertEquals(exception.message, "Percentage 0 should be between 1 and 99")
    exception = assertFailsWith<IllegalArgumentException>(
      block = { OGPBand.findBand(100) },
    )
    assertEquals(exception.message, "Percentage 100 should be between 1 and 99")
  }
}
