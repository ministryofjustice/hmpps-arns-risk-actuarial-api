package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class OGPBandTest {

  @Test
  fun `enum constants are set correctly`() {
    assertEquals(
      mapOf(
        OGPBand.LOW to Pair(0.0, 34.0),
        OGPBand.MEDIUM to Pair(34.0, 67.0),
        OGPBand.HIGH to Pair(67.0, 85.0),
        OGPBand.VERY_HIGH to Pair(85.0, 100.0),
      ),
      OGPBand.entries.associateWith { grading -> grading.bounds },
    )
  }

  @Test
  fun `bands are found correctly`() {
    assertEquals(OGPBand.LOW, OGPBand.findBand(0.000000001))
    assertEquals(OGPBand.LOW, OGPBand.findBand(32.6))
    assertEquals(OGPBand.LOW, OGPBand.findBand(33.999999))
    assertEquals(OGPBand.MEDIUM, OGPBand.findBand(34.000000001))
    assertEquals(OGPBand.MEDIUM, OGPBand.findBand(66.999999))
    assertEquals(OGPBand.HIGH, OGPBand.findBand(67.00000001))
    assertEquals(OGPBand.HIGH, OGPBand.findBand(84.999999))
    assertEquals(OGPBand.VERY_HIGH, OGPBand.findBand(85.000000001))
    assertEquals(OGPBand.VERY_HIGH, OGPBand.findBand(99.999999))
  }

  @Test
  fun `out of range values`() {
    var exception = assertFailsWith<IllegalArgumentException>(
      block = { OGPBand.findBand(0.0) },
    )
    assertEquals(exception.message, "Percentage 0.0 should be between 0 and 100")
    exception = assertFailsWith<IllegalArgumentException>(
      block = { OGPBand.findBand(100.000000001) },
    )
    assertEquals(exception.message, "Percentage 100.000000001 should be between 0 and 100")
  }
}
