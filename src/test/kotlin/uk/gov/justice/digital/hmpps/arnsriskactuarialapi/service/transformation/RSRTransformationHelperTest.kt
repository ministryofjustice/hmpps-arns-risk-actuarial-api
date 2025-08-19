package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand

class RSRTransformationHelperTest {

  @Test
  fun `should return LOW for RSR score within 0 point 0 to 3 point 0`() {
    assertEquals(RiskBand.LOW, getRSRBand(0.0))
    assertEquals(RiskBand.LOW, getRSRBand(1.5))
    assertEquals(RiskBand.LOW, getRSRBand(3.0))
  }

  @Test
  fun `should return MEDIUM for RSR score within 3 point 0 to 6 point 8`() {
    assertEquals(RiskBand.MEDIUM, getRSRBand(3.1))
    assertEquals(RiskBand.MEDIUM, getRSRBand(5.5))
    assertEquals(RiskBand.MEDIUM, getRSRBand(6.87))
    assertEquals(RiskBand.MEDIUM, getRSRBand(6.81))
    assertEquals(RiskBand.MEDIUM, getRSRBand(6.82))
  }

  @Test
  fun `should return HIGH for RSR score 6 point 9 or above`() {
    assertEquals(RiskBand.HIGH, getRSRBand(6.9))
    assertEquals(RiskBand.HIGH, getRSRBand(10.0))
  }

  @Test
  fun `should throw IllegalArgumentException for null RSR score`() {
    val exception = assertThrows<IllegalArgumentException> {
      getRSRBand(null)
    }
    assertEquals("RSR Score is null", exception.message)
  }

  @Test
  fun `should throw IllegalArgumentException for negative RSR score`() {
    val exception = assertThrows<IllegalArgumentException> {
      getRSRBand(-1.0)
    }
    assertEquals("RSR Score out of supported range: -1.0", exception.message)
  }
}
