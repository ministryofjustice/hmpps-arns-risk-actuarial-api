package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.regression

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.RiskScoreResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.asDoublePercentage
import kotlin.test.assertEquals

private const val TEST_CSV_FILE = "/regression/v4_1_2_oasys_test_data.csv"

// Note - there is no test case with id 1953 as this was an invalid case and has been removed with the agreement of data science
class ActuarialRegressionTest : IntegrationTestBase() {

  @ParameterizedTest(name = "[{index}] {arguments}")
  @CsvFileSource(
    resources = [TEST_CSV_FILE],
    useHeadersInDisplayName = true,
    ignoreLeadingAndTrailingWhitespace = true,

    encoding = "UTF8",
  )
  fun `actuarial predictors regression test suite`(@CsvToActuarialRegressionTestCase testCase: ActuarialRegressionTestCase) {
    // Skip test cases where the expected offence code category mapping does not match the reference data
    assumeFalse(mismatchedOffenceCodes.contains(testCase.offenceCode)) {
      "Test case ${testCase.id}: skipping as offence code ${testCase.offenceCode} mapping mismatch"
    }

    // Skip test case with id=598 as we get the same result as OASys but the expected values in the test data are different
    assumeFalse(testCase.id == 598) {
      "Test case ${testCase.id}: skipping as expected values don't match test data (under investigation)"
    }

    // Build and run STATIC and DYNAMIC calculations using API
    val staticResponse = postToApi(buildRiskScoreRequest(testCase, StaticOrDynamic.STATIC))
    val dynamicResponse = postToApi(buildRiskScoreRequest(testCase, StaticOrDynamic.DYNAMIC))

    println("STATIC response: $staticResponse")
    println("DYNAMIC response: $dynamicResponse")

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

  private val mismatchedOffenceCodes = listOf(
    "06608",
    "06617",
    "06618",
    "06619",
    "06620",
    "06807",
    "06808",
    "07303",
    "07304",
    "07305",
    "07306",
    "17505",
    "17506",
    "17507",
    "50300",
    "50400",
  )
}
