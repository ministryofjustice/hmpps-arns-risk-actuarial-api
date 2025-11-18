package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.OffenceCodeService
import java.io.File
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Snapshot testing for risk score calculations.
 * Each test fixture contains both request and expected response JSON in one file.
 * To add a new test file go to test/resources/fixtures and add in the relevant subfolder copying the existing structure.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiE2ETest : IntegrationTestBase() {

  @Autowired
  lateinit var offenceCodeService: OffenceCodeService

  @BeforeAll
  fun setup() {
    offenceCodeService.updateOffenceCodeMappings()
  }

  companion object {
    private val objectMapper = ObjectMapper()

    private const val FIXTURE_ROOT = "e2e"

    @JvmStatic
    fun requestResponseProvider(): Stream<Array<String>> {
      val classLoader = Thread.currentThread().contextClassLoader

      val dirUrl = classLoader.getResource(FIXTURE_ROOT)
        ?: throw IllegalArgumentException("Missing fixtures directory: $FIXTURE_ROOT")

      val files = File(dirUrl.toURI())
        .listFiles { f -> f.isFile && f.name.endsWith(".json") }
        ?.map { arrayOf("${FIXTURE_ROOT}/${it.name}") }
        ?: emptyList()

      return files.stream()
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

    val actualJson: JsonNode = objectMapper.readTree(responseBody).at("")

    assertEquals(expectedJson.toPrettyString(), actualJson.toPrettyString(), "Error with Fixture file: $fixturePath")
  }
}
