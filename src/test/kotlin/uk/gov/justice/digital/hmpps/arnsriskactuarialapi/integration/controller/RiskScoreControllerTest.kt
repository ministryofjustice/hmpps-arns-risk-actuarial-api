package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.controller

import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.OGRS3Object
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.RiskScoreService

class RiskScoreControllerTest : IntegrationTestBase() {

  private val riskScoreService: RiskScoreService = mock()

  private val basicRequest = RiskScoreRequest(
    "1_0",
    null,
    null,
    null,
    null,
    null,
    null,
    null,
  )

  @Test
  fun `postRiskScores returns 200 ok and risk score for correct user role`() {
    val ogrs3 = OGRS3Object(
      "1_0",
      1.0,
      1.0,
      RiskBand.LOW,
      listOf(),
    )

    whenever(riskScoreService.riskScoreProducer(basicRequest))
      .thenReturn(RiskScoreResponse(ogrs3))

    webTestClient.post()
      .uri("/risk-scores")
      .headers(setAuthorisation(roles = listOf("ARNS_RISK_ACTUARIAL")))
      .bodyValue(basicRequest)
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("$.ogrs3.algorithmVersion").isEqualTo("1_0")
  }

  @Test
  fun `postRiskScores returns 403 forbidden for wrong user role`() {
    webTestClient.post()
      .uri("/risk-scores")
      .headers(setAuthorisation())
      .bodyValue(basicRequest)
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `postRiskScores returns 401 unauthorised for no auth token`() {
    webTestClient.post()
      .uri("/risk-scores")
      .bodyValue(basicRequest)
      .exchange()
      .expectStatus().isUnauthorized
  }
}
