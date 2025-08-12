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
class ApiIntegrationTest : IntegrationTestBase() {

  companion object {
    @JvmStatic
    fun requestResponseProvider(): Stream<Array<String>> = Stream.of(
      arrayOf("ogrs3", "requests/ogrs3-input-1-valid.json", "responses/ogrs3-expected-1.json"),
      arrayOf("ogrs3", "requests/ogrs3-input-2-missing-fields.json", "responses/ogrs3-expected-2.json"),
      arrayOf("ogrs3", "requests/ogrs3-input-3-invalid-age.json", "responses/ogrs3-expected-3.json"),
      arrayOf("ogrs3", "requests/ogrs3-input-4-invalid-offence.json", "responses/ogrs3-expected-4.json"),
      arrayOf("ogrs3", "requests/ogrs3-input-5-valid.json", "responses/ogrs3-expected-5.json"),
      arrayOf("ovp", "requests/ovp-input-1-missing-fields.json", "responses/ovp-expected-1.json"),
      arrayOf("ovp", "requests/ovp-input-2-valid-fields.json", "responses/ovp-expected-2.json"),
      arrayOf("mst", "requests/mst-input-1-valid-fields.json", "responses/mst-expected-1.json"),
      arrayOf("mst", "requests/mst-input-2-valid-fields.json", "responses/mst-expected-2.json"),
      arrayOf("mst", "requests/mst-input-3-missing-fields.json", "responses/mst-expected-3.json"),
      arrayOf("mst", "requests/mst-input-4-valid-custom-assessment-date.json", "responses/mst-expected-4.json"),
      arrayOf("mst", "requests/mst-input-5-valid-missing-fields.json", "responses/mst-expected-5.json"),
      arrayOf("ogp", "requests/ogp-input-1-valid.json", "responses/ogp-expected-1.json"),
      arrayOf("ogp", "requests/ogp-input-2-missing-fields.json", "responses/ogp-expected-2.json"),
      arrayOf("ogp", "requests/ogp-input-3-missing-fields.json", "responses/ogp-expected-3.json"),
      arrayOf("pni", "requests/pni-input-1-valid.json", "responses/pni-expected-1.json"),
      arrayOf("pni", "requests/pni-input-2-missing-fields.json", "responses/pni-expected-2.json"),
      arrayOf("opd", "requests/opd-input-1-male-valid.json", "responses/opd-expected-1.json"),
      arrayOf("opd", "requests/opd-input-2-female-valid.json", "responses/opd-expected-2.json"),
      arrayOf("opd", "requests/opd-input-3-invalid-missing.json", "responses/opd-expected-3.json"),
      arrayOf("lds", "requests/lds-input-1-valid.json", "responses/lds-expected-1.json"),
      arrayOf("lds", "requests/lds-input-2-missing-fields.json", "responses/lds-expected-2.json"),
      arrayOf("lds", "requests/lds-input-2-missing-fields.json", "responses/lds-expected-2.json"),

      // Add more as needed
    )

    private fun readFileFromClasspath(path: String): String = Files.readString(Paths.get(ClassPathResource(path).uri))
  }

  @ParameterizedTest
  @MethodSource("requestResponseProvider")
  fun `post risk score returns expected response`(
    jsonTreeToCompare: String,
    requestPath: String,
    expectedResponsePath: String,
  ) {
    val objectMapper = ObjectMapper()
    val requestBody = readFileFromClasspath(requestPath)
    val expectedResponseBody = readFileFromClasspath(expectedResponsePath)

    val responseBody = webTestClient.post()
      .uri("/risk-scores/v1")
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ARNS_RISK_ACTUARIAL")))
      .bodyValue(requestBody)
      .exchange()
      .expectStatus().isOk
      .expectBody(String::class.java)
      .returnResult()
      .responseBody ?: fail("No response body received")

    val expectedJson: JsonNode = objectMapper.readTree(expectedResponseBody).path(jsonTreeToCompare)
    val actualJson: JsonNode = objectMapper.readTree(responseBody).path(jsonTreeToCompare)

    if (expectedJson != actualJson) {
      println("Expected JSON:\n${objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(expectedJson)}")
      println("Actual JSON:\n${objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(actualJson)}")
      fail("Response JSON did not match expected output.")
    }
  }
}
