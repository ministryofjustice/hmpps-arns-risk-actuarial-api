package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import java.io.File
import java.util.stream.Stream
import kotlin.test.fail

/**
 * Snapshot testing for risk score calculations.
 * Each test fixture contains both request and expected response JSON in one file.
 * To add a new test file go to test/resources/fixtures and add in the relevant subfolder copying the existing structure.
 */
class ApiIntegrationTest : IntegrationTestBase() {

  companion object {
    private val objectMapper = ObjectMapper()

    private const val FIXTURE_ROOT = "fixtures"
    private val MODELS = listOf("ogrs3", "lds", "mst", "ogp", "opd", "ovp", "rsr", "pni")

    @JvmStatic
    fun requestResponseProvider(): Stream<Array<String>> {
      val classLoader = Thread.currentThread().contextClassLoader

      return MODELS
        .flatMap { model ->
          val dirUrl = classLoader.getResource("$FIXTURE_ROOT/$model")
            ?: throw IllegalArgumentException("Missing fixtures directory: $FIXTURE_ROOT/$model")

          File(dirUrl.toURI())
            .listFiles { f -> f.isFile && f.name.endsWith(".json") }
            ?.map { arrayOf(model, "$FIXTURE_ROOT/$model/${it.name}") }
            ?: emptyList()
        }
        .stream()
    }

    private fun readFixture(path: String): JsonNode {
      val resource = ClassPathResource(path)
      val fileContent = resource.inputStream.bufferedReader().use { it.readText() }
      return objectMapper.readTree(fileContent)
    }
  }

  @ParameterizedTest
  @MethodSource("requestResponseProvider")
  fun `post risk score returns expected response`(
    predictorName: String,
    fixturePath: String,
  ) {
    val fixtureJson = readFixture(fixturePath)

    val requestBody = fixtureJson["request"].toString()
    val expectedJson: JsonNode = fixtureJson["response"]

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

    val actualJson: JsonNode = objectMapper.readTree(responseBody).path(predictorName)

    if (expectedJson != actualJson) {
      println("Fixture file: $fixturePath")
      println("Expected JSON:\n${objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(expectedJson)}")
      println("Actual JSON:\n${objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(actualJson)}")
      fail("Response JSON did not match expected output.")
    }
  }
}
