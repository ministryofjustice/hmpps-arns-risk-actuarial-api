package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.controller

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.IntegrationTestBase

class RiskScoreControllerTest : IntegrationTestBase() {

  @Test
  fun `postRiskScores returns 200 ok and risk score for correct user role`() {
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
    webTestClient.post()
      .uri("/risk-scores")
      .headers(setAuthorisation())
      .bodyValue(RiskScoreRequest(1))
      .exchange()
      .expectStatus().isForbidden
  }
}
