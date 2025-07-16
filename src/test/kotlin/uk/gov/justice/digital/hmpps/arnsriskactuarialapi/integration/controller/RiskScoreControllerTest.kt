package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.controller

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.OGRS3Object
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.RiskScoreService
import java.time.LocalDate

class RiskScoreControllerTest : IntegrationTestBase() {

  @MockitoBean
  private lateinit var riskScoreService: RiskScoreService

  private val basicRequest = RiskScoreRequest(
    "1_0",
    Gender.MALE,
    LocalDate.of(1964, 10, 15),
    LocalDate.of(2014, 12, 13),
    LocalDate.of(2027, 12, 12),
    10,
    30,
    "05101",
  )

  @Test
  fun `postRiskScores returns 200 ok and risk score for correct user role`() {
    val ogrs3 = OGRS3Object(
      "1_0",
      1,
      1,
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
      .jsonPath("$.ogrs3.algorithmVersion").isEqualTo(ogrs3.algorithmVersion)
      .jsonPath("$.ogrs3.ogrs3OneYear").isEqualTo(ogrs3.ogrs3OneYear)
      .jsonPath("$.ogrs3.ogrs3TwoYear").isEqualTo(ogrs3.ogrs3TwoYear)
      .jsonPath("$.ogrs3.band").isEqualTo(ogrs3.band)
      .jsonPath("$.ogrs3.validationError").isEmpty()
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
