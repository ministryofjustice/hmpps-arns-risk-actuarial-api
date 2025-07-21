package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.RiskScoreRequestTestConstants.FULL_OGP_REQUEST
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.RiskScoreRequestTestConstants.NULL_REQUEST
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.RiskScoreRequestTestConstants.OGP_REQUEST_01569
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.OGPRiskProducerService.Companion.coalesceForOGP

class OGPRiskProducerServiceTest {

  @ParameterizedTest()
  @MethodSource("getRiskScoreRequests")
  fun `requests are coalesced correctly`(request: RiskScoreRequest, ogrs3TwoYear: Int?, expected: RiskScoreRequest) {
    assertEquals(expected, coalesceForOGP(request, ogrs3TwoYear))
  }

  companion object {

    @JvmStatic
    fun getRiskScoreRequests(): List<Arguments> = listOf(
      Arguments.of(
        NULL_REQUEST,
        22,
        RiskScoreRequest(version = "1_0", ogrs3TwoYear = 22 as Integer?),
      ),
      Arguments.of(
        FULL_OGP_REQUEST,
        null,
        RiskScoreRequest(
          version = "1_0",
          ogrs3TwoYear = null,
          currentAccomodation = true,
          employmentStatus = false,
          regularOffendingActivities = ProblemLevel.NO_PROBLEMS,
          currentDrugMisuse = ProblemLevel.NO_PROBLEMS,
          motivationDrug = ProblemLevel.NO_PROBLEMS,
          problemSolvingSkills = ProblemLevel.NO_PROBLEMS,
          awarenessOfConsequences = ProblemLevel.NO_PROBLEMS,
          understandsPeoplesViews = ProblemLevel.NO_PROBLEMS,
          proCriminalAttitudes = ProblemLevel.SOME_PROBLEMS,
        ),
      ),
      Arguments.of(
        OGP_REQUEST_01569,
        null,
        OGP_REQUEST_01569,
      ),
    )
  }
}
