package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.RiskScoresService

class RiskScoresServiceTest {
  private val riskScoresService = RiskScoresService()

  @Test
  fun `riskScoreProducer returns risk score as RiskScoreResponse`() {
    val result = riskScoresService.riskScoreProducer(RiskScoreRequest(1))
    assertEquals(1, result.score)
  }
}
