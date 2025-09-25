package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OGPBandTest {

  @Test
  fun `bands are found correctly`() {
    assertEquals(RiskBand.LOW, OGPTransformationHelper.bandOGP(1))
    assertEquals(RiskBand.LOW, OGPTransformationHelper.bandOGP(15))
    assertEquals(RiskBand.LOW, OGPTransformationHelper.bandOGP(33))
    assertEquals(RiskBand.MEDIUM, OGPTransformationHelper.bandOGP(34))
    assertEquals(RiskBand.MEDIUM, OGPTransformationHelper.bandOGP(66))
    assertEquals(RiskBand.HIGH, OGPTransformationHelper.bandOGP(67))
    assertEquals(RiskBand.HIGH, OGPTransformationHelper.bandOGP(84))
    assertEquals(RiskBand.VERY_HIGH, OGPTransformationHelper.bandOGP(85))
    assertEquals(RiskBand.VERY_HIGH, OGPTransformationHelper.bandOGP(99))
  }

  @Test
  fun `out of range values`() {
    var exception = assertFailsWith<IllegalArgumentException>(
      block = { OGPTransformationHelper.bandOGP(0) },
    )
    assertEquals(exception.message, "Percentage 0 should be between 1 and 99")
    exception = assertFailsWith<IllegalArgumentException>(
      block = { OGPTransformationHelper.bandOGP(100) },
    )
    assertEquals(exception.message, "Percentage 100 should be between 1 and 99")
  }
}
