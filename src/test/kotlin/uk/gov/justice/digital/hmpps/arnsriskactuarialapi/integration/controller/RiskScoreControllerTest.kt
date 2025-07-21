package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.controller

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3.OGRS3Object
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyOGP
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyOVP
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
    10 as Integer?,
    30 as Integer?,
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
      .thenReturn(RiskScoreResponse(ogrs3, emptyOVP(), emptyOGP()))

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

  @Test
  fun `postRiskScores returns 400 if incorrect enum value used`() {
    webTestClient.post()
      .uri("/risk-scores")
      .headers(setAuthorisation(roles = listOf("ARNS_RISK_ACTUARIAL")))
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "version": 1,
          "gender": "INVALID_OPTION",
          "dateOfBirth": null,
          "dateOfCurrentConviction": null,
          "dateAtStartOfFollowup": null,
          "totalNumberOfSanctions": null,
          "ageAtFirstSanction": null,
          "currentOffence": null
        }
        """.trimIndent(),
      )
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `postRiskScores returns 400 if incorrect type used`() {
    webTestClient.post()
      .uri("/risk-scores")
      .headers(setAuthorisation(roles = listOf("ARNS_RISK_ACTUARIAL")))
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "version": 1,
          "gender": "MALE",
          "dateOfBirth": null,
          "dateOfCurrentConviction": null,
          "dateAtStartOfFollowup": null,
          "totalNumberOfSanctions": "not a number",
          "ageAtFirstSanction": null,
          "currentOffence": null
        }
        """.trimIndent(),
      )
      .exchange()
      .expectStatus().isBadRequest
  }
}
