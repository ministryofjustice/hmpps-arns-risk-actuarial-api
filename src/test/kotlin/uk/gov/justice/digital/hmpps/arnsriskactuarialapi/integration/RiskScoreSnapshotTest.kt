package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Stream
import kotlin.test.fail

/**
 * Uses snapshot testing as a mean of comparing complex calculations with results for OASys.
 * Tests compare the current output to the expected json snapshot.
 */
class RiskScoreSnapshotTest : IntegrationTestBase() {

  companion object {
    @JvmStatic
    fun requestResponseProvider(): Stream<Array<String>> = Stream.of(
      arrayOf("requests/osgr3-input-1.json", "responses/osgr3-expected-1.json"),
      // Add more as needed
    )

    private fun readFileFromClasspath(path: String): String = Files.readString(Paths.get(ClassPathResource(path).uri))
  }

  @ParameterizedTest
  @MethodSource("requestResponseProvider")
  fun `post risk score returns expected response`(requestPath: String, expectedResponsePath: String) {
    val objectMapper = ObjectMapper()
    val requestBody = readFileFromClasspath(requestPath)
    val expectedResponseBody = readFileFromClasspath(expectedResponsePath)

    val responseBody = webTestClient.post()
      .uri("/risk-scores")
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ARNS_RISK_ACTUARIAL")))
      .bodyValue(requestBody)
      .exchange()
      .expectStatus().isOk
      .expectBody(String::class.java)
      .returnResult()
      .responseBody ?: fail("No response body received")

    val expectedJson: JsonNode = objectMapper.readTree(expectedResponseBody)
    val actualJson: JsonNode = objectMapper.readTree(responseBody)

    if (expectedJson != actualJson) {
      println("Expected JSON:\n${objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(expectedJson)}")
      println("Actual JSON:\n${objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(actualJson)}")
        fail("Response JSON did not match expected output.")
    }
  }
}