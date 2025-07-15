package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest

class RiskScoreServiceTest {
  private val riskScoreService = RiskScoreService()

  @Test
  fun `riskScoreProducer returns risk score response with algorithm version as 1_0`() {
    val request = RiskScoreRequest(
      "1_0",
      null,
      null,
      null,
      null,
      null,
      null,
      null,
    )

    val result = riskScoreService.riskScoreProducer(request)
    Assertions.assertEquals("1_0", result.OGRS3.algorithmVersion)
  }
}
