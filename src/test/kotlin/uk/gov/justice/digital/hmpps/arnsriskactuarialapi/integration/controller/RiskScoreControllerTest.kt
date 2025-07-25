package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.controller

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.IntegrationTestBase
import java.time.LocalDate

class RiskScoreControllerTest : IntegrationTestBase() {

  private val basicRequest = RiskScoreRequest(
    RiskScoreVersion.V1_0,
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
    webTestClient.post()
      .uri("/risk-scores/v1")
      .headers(setAuthorisation(roles = listOf("ARNS_RISK_ACTUARIAL")))
      .bodyValue(basicRequest)
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("$.version").isEqualTo(RiskScoreVersion.V1_0)
      .jsonPath("$.ogrs3.ogrs3OneYear").isEqualTo(20)
      .jsonPath("$.ogrs3.ogrs3TwoYear").isEqualTo(34)
      .jsonPath("$.ogrs3.band").isEqualTo("LOW")
      .jsonPath("$.ogrs3.validationError").isEmpty()
  }

  @Test
  fun `postRiskScores returns 200 ok and risk score for correct user role - default version`() {
    webTestClient.post()
      .uri("/risk-scores/v1")
      .headers(setAuthorisation(roles = listOf("ARNS_RISK_ACTUARIAL")))
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "gender": "MALE",
          "dateOfBirth": "1965-01-01",
          "dateOfCurrentConviction": "2024-01-01",
          "dateAtStartOfFollowup": "2027-01-01",
          "totalNumberOfSanctions": 3,
          "ageAtFirstSanction": 50,
          "currentOffence": "05101"
        }
        """.trimIndent(),
      )
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("$.version").isEqualTo(RiskScoreVersion.V1_0)
  }

  @Test
  fun `postRiskScores returns 403 forbidden for wrong user role`() {
    webTestClient.post()
      .uri("/risk-scores/v1")
      .headers(setAuthorisation())
      .bodyValue(basicRequest)
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `postRiskScores returns 401 unauthorised for no auth token`() {
    webTestClient.post()
      .uri("/risk-scores/v1")
      .bodyValue(basicRequest)
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `postRiskScores returns 400 if incorrect enum value used`() {
    webTestClient.post()
      .uri("/risk-scores/v1")
      .headers(setAuthorisation(roles = listOf("ARNS_RISK_ACTUARIAL")))
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "version": "V1_0",
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
      .uri("/risk-scores/v1")
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
