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

private const val TEST_CSV_FILE = "/regression/v4_1_1_oasys_test_data.csv"

class ActuarialRegressionTest : IntegrationTestBase() {

  @ParameterizedTest(name = "[{index}] {arguments}")
  @CsvFileSource(
    resources = [TEST_CSV_FILE],
    useHeadersInDisplayName = true,
    ignoreLeadingAndTrailingWhitespace = true,
    encoding = "UTF8",
  )
  fun `actuarial predictors regression test suite`(@CsvToActuarialRegressionTestCase testCase: ActuarialRegressionTestCase) {
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

    assumeFalse(removedOffenceCodes.contains(testCase.offenceCode)) {
      "Test case ${testCase.id}: skipping as offence code ${testCase.offenceCode} has been removed"
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

  private val removedOffenceCodes = listOf(
    "03605",
    "10522",
    "10519",
    "11510",
    "10523",
    "11516",
    "19722",
    "06807",
    "07306",
    "07304",
    "59900",
    "06618",
    "06808",
    "02126",
    "02127",
    "14901",
    "05805",
    "05603",
    "09585",
    "05211",
    "19436",
    "19709",
    "19466",
    "13850",
    "07910",
    "09572",
    "19710",
    "09860",
    "07803",
    "07908",
    "07827",
    "07826",
    "19706",
    "05358",
    "05359",
    "07802",
    "09858",
    "03703",
    "09218",
    "11515",
    "09412",
    "08308",
    "09216",
    "19356",
    "50400",
    "15116",
    "09295",
    "02804",
    "07906",
    "10518",
    "15117",
    "06619",
    "06823",
    "11012",
    "82525",
    "06811",
    "07303",
    "09348",
    "03614",
    "04901",
    "19702",
    "08141",
    "17506",
    "15118",
    "06814",
    "12701",
    "00413",
    "12587",
    "05212",
    "06620",
    "08003",
    "07913",
    "00000",
    "09344",
    "08128",
    "06608",
    "19707",
    "10840",
    "09861",
    "17507",
    "00868",
    "09380",
    "09416",
    "02226",
    "13840",
    "81903",
    "06821",
    "12590",
    "59800",
    "09189",
    "09362",
    "09857",
    "09607",
    "80801",
    "09345",
    "08813",
    "10447",
    "11301",
    "12593",
    "07766",
    "00869",
    "09894",
    "80314",
    "07305",
    "19716",
    "03607",
    "10524",
    "12588",
    "08190",
    "09347",
    "09346",
    "19598",
    "50300",
    "09415",
    "08189",
    "11302",
    "09897",
    "03613",
    "09006",
    "19467",
    "10448",
    "09586",
    "09368",
    "09342",
    "06617",
    "13836",
    "09701",
    "19719",
    "11202",
    "09580",
    "09577",
    "10446",
    "09862",
    "19357",
    "09578",
    "81602",
    "17505",
    "19708",
    "02230",
    "00417",
    "19464",
    "12589",
    "09191",
    "11514",
    "09390",
    "09418",
    "81005",
    "19714",
    "09859",
    "19597",
    "16857",
    "19463",
    "09603",
    "09414",
    "09341",
    "09918",
    "15520",
    "10443",
    "15530",
    "11599",
    "81004",
    "09343",
    "11508",
    "09217",
    "15522",
    "09573",
    "10520",
    "19358",
    "11540",
    "08182",
    "09704",
    "09895",
    "09576",
    "11011",
  )
}
