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
    LocalDate.of(2025, 1, 1),
    LocalDate.of(1964, 10, 15),
    LocalDate.of(2014, 12, 13),
    10 as Integer?,
    30 as Integer?,
    "05101",
    dateAtStartOfFollowupUserInput = LocalDate.of(2027, 12, 12),
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
          "dateAtStartOfFollowupUserInput": "2027-01-01",
          "totalNumberOfSanctionsForAllOffences": 3,
          "ageAtFirstSanction": 50,
          "currentOffenceCode": "05101"
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
  fun `postRiskScores returns 400 if incorrect enum text value used`() {
    val expectedError =
      "JSON parse error: Cannot deserialize value of type `uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender` from String \"INVALID_OPTION\": not one of the values accepted for Enum class: [FEMALE, MALE]"

    webTestClient.post()
      .uri("/risk-scores/v1")
      .headers(setAuthorisation(roles = listOf("ARNS_RISK_ACTUARIAL")))
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "gender": "INVALID_OPTION"
        }
        """.trimIndent(),
      )
      .exchange()
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$.message").isEqualTo(expectedError)
  }

  @Test
  fun `postRiskScores returns 400 if incorrect enum numeric value used`() {
    val expectedError =
      "JSON parse error: Cannot deserialize value of type `uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender` from number 1: not allowed to deserialize Enum value out of number: disable DeserializationConfig.DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS to allow"
    webTestClient.post()
      .uri("/risk-scores/v1")
      .headers(setAuthorisation(roles = listOf("ARNS_RISK_ACTUARIAL")))
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "gender": 1
        }
        """.trimIndent(),
      )
      .exchange()
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$.message").isEqualTo(expectedError)
  }

  @Test
  fun `postRiskScores returns 400 if incorrect boolean numeric value used like 123 instead of true, false`() {
    val expectedError =
      "JSON parse error: Cannot coerce Integer value (123) to `java.lang.Boolean` value (but could if coercion was enabled using `CoercionConfig`)"
    webTestClient.post()
      .uri("/risk-scores/v1")
      .headers(setAuthorisation(roles = listOf("ARNS_RISK_ACTUARIAL")))
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "isUnemployed": 123
        }
        """.trimIndent(),
      )
      .exchange()
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$.message").isEqualTo(expectedError)
  }

  @Test
  fun `postRiskScores returns 400 if incorrect boolean string value used instead of true, false`() {
    val expectedError =
      "JSON parse error: Cannot coerce String value (\"true\") to `java.lang.Boolean` value (but might if coercion using `CoercionConfig` was enabled)"
    webTestClient.post()
      .uri("/risk-scores/v1")
      .headers(setAuthorisation(roles = listOf("ARNS_RISK_ACTUARIAL")))
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "hasBeenOnMedicationForMentalHealthProblems": "true"
        }
        """.trimIndent(),
      )
      .exchange()
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$.message").isEqualTo(expectedError)
  }

  @Test
  fun `postRiskScores returns 400 if incorrect type used`() {
    val expectedError =
      "JSON parse error: Cannot deserialize value of type `java.lang.Integer` from String \"not a number\": not a valid `java.lang.Integer` value"
    webTestClient.post()
      .uri("/risk-scores/v1")
      .headers(setAuthorisation(roles = listOf("ARNS_RISK_ACTUARIAL")))
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "totalNumberOfSanctionsForAllOffences": "not a number"
        }
        """.trimIndent(),
      )
      .exchange()
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$.message").isEqualTo(expectedError)
  }

  @Test
  fun `postRiskScores returns 400 if unknown field is passed`() {
    val expectedError =
      "JSON parse error: Unrecognized field \"IdoNotExists\" (class uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest), not marked as ignorable"
    webTestClient.post()
      .uri("/risk-scores/v1")
      .headers(setAuthorisation(roles = listOf("ARNS_RISK_ACTUARIAL")))
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "IdoNotExists": "1234"
        }
        """.trimIndent(),
      )
      .exchange()
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$.message").isEqualTo(expectedError)
  }
}
