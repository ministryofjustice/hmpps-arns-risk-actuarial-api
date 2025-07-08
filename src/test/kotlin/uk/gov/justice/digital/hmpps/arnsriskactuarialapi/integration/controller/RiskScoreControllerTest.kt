package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.controller

import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.RiskScoresService

class RiskScoreControllerTest : IntegrationTestBase() {

  private val riskScoresService: RiskScoresService = mock()

  @Test
  fun `postRiskScores returns 200 ok and risk score for correct user role`() {
    whenever(riskScoresService.riskScoreProducer(RiskScoreRequest(1))).thenReturn(RiskScoreResponse(1))

    webTestClient.post()
      .uri("/risk-scores")
      .headers(setAuthorisation(roles = listOf("ARNS_RISK_ACTUARIAL")))
      .bodyValue(RiskScoreRequest(1))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("$.score").isEqualTo(1)
  }

  @Test
  fun `postRiskScores returns 403 forbidden for wrong user role`() {
    whenever(riskScoresService.riskScoreProducer(RiskScoreRequest(1))).thenReturn(RiskScoreResponse(1))

    webTestClient.post()
      .uri("/risk-scores")
      .headers(setAuthorisation())
      .bodyValue(RiskScoreRequest(1))
      .exchange()
      .expectStatus().isForbidden
  }
}
