package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.MotivationLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.YesSometimesNo
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp.OGPBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp.OGPObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3.OGRS3Object

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OGPRiskProducerServiceTest {

  private val service = OGPRiskProducerService()

  @Test
  fun `single request`() {
    val ogrs3Output = OGRS3Object(
      ogrs3OneYear = null,
      ogrs3TwoYear = 81,
      band = null,
      validationError = null,
    )
    val context = RiskScoreContext(
      version = RiskScoreVersion.V1_0,
      OGRS3 = ogrs3Output,
    )
    val request = RiskScoreRequest(
      isCurrentlyOfNoFixedAbodeOrTransientAccommodation = false,
      isUnemployed = true,
      regularOffendingActivities = ProblemLevel.SOME_PROBLEMS,
      currentDrugMisuse = ProblemLevel.NO_PROBLEMS,
      motivationToTackleDrugMisuse = MotivationLevel.FULL_MOTIVATION,
      problemSolvingSkills = ProblemLevel.SOME_PROBLEMS,
      awarenessOfConsequences = YesSometimesNo.SOMETIMES,
      understandsOtherPeoplesViews = ProblemLevel.NO_PROBLEMS,
      proCriminalAttitudes = ProblemLevel.SOME_PROBLEMS,
    )
    val result = service.getRiskScore(request, context)
    val expected = context.copy(
      OGP = OGPObject(
        ogpReoffendingOneYear = 53,
        ogpReoffendingTwoYear = 68,
        bandOGP = OGPBand.HIGH,
        totalOGPScore = 62,
        validationError = emptyList(),
      ),
    )
    assertEquals(expected, result)
  }
}
