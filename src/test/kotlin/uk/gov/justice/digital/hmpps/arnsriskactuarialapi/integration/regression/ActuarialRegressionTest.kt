package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.regression

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.RiskScoreResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.asDoublePercentage
import java.util.stream.Stream
import kotlin.test.assertEquals

private const val TEST_CSV_FILE = "/regression/v4_1_1_oasys_test_data.csv"

// NOTE - for debugging locally change this to SAME_THREAD otherwise logging won't work
@Execution(ExecutionMode.CONCURRENT)
class ActuarialRegressionTest : IntegrationTestBase() {

  @TestFactory
  fun `run actuarial regression test suite`(): Stream<DynamicTest> {
    val testCases = loadActuarialRegressionTestCsv(TEST_CSV_FILE)

    return testCases.map { testCase ->
      dynamicTest("Test case: $testCase") {
        runActuarialRegressionTest(testCase)
      }
    }.stream()
  }

  fun runActuarialRegressionTest(testCase: ActuarialRegressionTestCase) {
    // Skip some test cases with invalid test inputs for now
    assumeFalse(testCase.fourPointTwo == 1) {
      "Test case ${testCase.id}: skipping as fourPointTwo/unemployment cannot be 1"
    }

    assumeFalse(testCase.sixPointEight == 0) {
      "Test case ${testCase.id}: skipping as sixPointEight/currentRelationshipStatus cannot be 0"
    }

    assumeFalse(testCase.offenceCode == "14100" || testCase.offenceCode == "08800" || testCase.offenceCode == "11100") {
      "Test case ${testCase.id}: skipping as offence code category ${testCase.offenceCode} cannot be NEED_DETAILS_OF_EXACT_OFFENCE"
    }

    // Build and run STATIC and DYNAMIC calculations using API
    val staticResponse = postToApi(buildRiskScoreRequest(testCase, StaticOrDynamic.STATIC))
    val dynamicResponse = postToApi(buildRiskScoreRequest(testCase, StaticOrDynamic.DYNAMIC))

    // Uncomment these when debugging locally
//    println("STATIC response: $staticResponse")
//    println("DYNAMIC response: $dynamicResponse")

    // Check all scores match
    assertAll(
      "Check risk scores",
      { assertEquals(testCase.allBriefPredictions?.asDoublePercentage(), staticResponse?.actuarialPredictors?.allPredictor?.output?.score, "All reoffending predictor STATIC score mismatch") },
      { assertEquals(testCase.violenceBriefPredictions?.asDoublePercentage(), staticResponse?.actuarialPredictors?.violentPredictor?.output?.score, "Violent reoffending predictor STATIC score mismatch") },
      { assertEquals(testCase.seriousViolenceBriefPredictions?.asDoublePercentage(), staticResponse?.actuarialPredictors?.seriousViolentPredictor?.output?.score, "Serious violent predictor STATIC score mismatch") },
      { assertEquals(testCase.allExtendedPredictions?.asDoublePercentage(), dynamicResponse?.actuarialPredictors?.allPredictor?.output?.score, "All reoffending predictor score DYNAMIC mismatch") },
      { assertEquals(testCase.violenceExtendedPredictions?.asDoublePercentage(), dynamicResponse?.actuarialPredictors?.violentPredictor?.output?.score, "Violent reoffending predictor DYNAMIC score mismatch") },
      { assertEquals(testCase.seriousViolenceExtendedPredictions?.asDoublePercentage(), dynamicResponse?.actuarialPredictors?.seriousViolentPredictor?.output?.score, "Serious violent DYNAMIC predictor score mismatch") },
    )
  }

  private fun postToApi(request: RiskScoreRequest) = webTestClient.post()
    .uri("/risk-scores/v1")
    .contentType(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation(roles = listOf("ARNS_RISK_ACTUARIAL")))
    .bodyValue(request)
    .exchange()
    .expectStatus().isOk
    .expectBody<RiskScoreResponse>()
    .returnResult()
    .responseBody
}
