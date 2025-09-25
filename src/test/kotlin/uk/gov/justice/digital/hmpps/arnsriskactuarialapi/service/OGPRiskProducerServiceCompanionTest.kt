package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.MotivationLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.YesSometimesNo
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp.OGPInputValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp.OGPObject

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OGPRiskProducerServiceCompanionTest {

  var testSubject: OGPRiskProducerService = OGPRiskProducerService()

  fun getOGPTestCases(): List<Arguments> = listOf(
    Arguments.of(
      OGPInputValidated(
        ogrs3TwoYear = 81,
        isCurrentlyOfNoFixedAbodeOrTransientAccommodation = false,
        isUnemployed = true,
        regularOffendingActivities = ProblemLevel.SOME_PROBLEMS,
        currentDrugMisuse = ProblemLevel.NO_PROBLEMS,
        motivationToTackleDrugMisuse = MotivationLevel.FULL_MOTIVATION,
        problemSolvingSkills = ProblemLevel.SOME_PROBLEMS,
        awarenessOfConsequences = YesSometimesNo.SOMETIMES,
        understandsOtherPeoplesViews = ProblemLevel.NO_PROBLEMS,
        proCriminalAttitudes = ProblemLevel.SOME_PROBLEMS,
      ),
      OGPObject(
        ogpReoffendingOneYear = 53,
        ogpReoffendingTwoYear = 68,
        bandOGP = RiskBand.HIGH,
        totalOGPScore = 62,
        validationError = emptyList(),
      ),
    ),
    Arguments.of(
      OGPInputValidated(
        ogrs3TwoYear = 67,
        isCurrentlyOfNoFixedAbodeOrTransientAccommodation = false,
        isUnemployed = true,
        regularOffendingActivities = ProblemLevel.SOME_PROBLEMS,
        currentDrugMisuse = ProblemLevel.NO_PROBLEMS,
        motivationToTackleDrugMisuse = MotivationLevel.FULL_MOTIVATION,
        problemSolvingSkills = ProblemLevel.SOME_PROBLEMS,
        awarenessOfConsequences = YesSometimesNo.SOMETIMES,
        understandsOtherPeoplesViews = ProblemLevel.NO_PROBLEMS,
        proCriminalAttitudes = ProblemLevel.SOME_PROBLEMS,
      ),
      OGPObject(
        ogpReoffendingOneYear = 40,
        ogpReoffendingTwoYear = 55,
        bandOGP = RiskBand.MEDIUM,
        totalOGPScore = 53,
        validationError = emptyList(),
      ),
    ),
    Arguments.of(
      OGPInputValidated(
        ogrs3TwoYear = 37,
        isCurrentlyOfNoFixedAbodeOrTransientAccommodation = false,
        isUnemployed = true,
        regularOffendingActivities = ProblemLevel.SOME_PROBLEMS,
        currentDrugMisuse = ProblemLevel.SIGNIFICANT_PROBLEMS,
        motivationToTackleDrugMisuse = MotivationLevel.FULL_MOTIVATION,
        problemSolvingSkills = ProblemLevel.SOME_PROBLEMS,
        awarenessOfConsequences = YesSometimesNo.SOMETIMES,
        understandsOtherPeoplesViews = ProblemLevel.NO_PROBLEMS,
        proCriminalAttitudes = ProblemLevel.SOME_PROBLEMS,
      ),
      OGPObject(
        ogpReoffendingOneYear = 26,
        ogpReoffendingTwoYear = 39,
        bandOGP = RiskBand.MEDIUM,
        totalOGPScore = 43,
        validationError = emptyList(),
      ),
    ),
    Arguments.of(
      OGPInputValidated(
        ogrs3TwoYear = 17,
        isCurrentlyOfNoFixedAbodeOrTransientAccommodation = false,
        isUnemployed = true,
        regularOffendingActivities = ProblemLevel.SOME_PROBLEMS,
        currentDrugMisuse = ProblemLevel.SIGNIFICANT_PROBLEMS,
        motivationToTackleDrugMisuse = MotivationLevel.FULL_MOTIVATION,
        problemSolvingSkills = ProblemLevel.SOME_PROBLEMS,
        awarenessOfConsequences = YesSometimesNo.SOMETIMES,
        understandsOtherPeoplesViews = ProblemLevel.NO_PROBLEMS,
        proCriminalAttitudes = ProblemLevel.SOME_PROBLEMS,
      ),
      OGPObject(
        ogpReoffendingOneYear = 15,
        ogpReoffendingTwoYear = 24,
        bandOGP = RiskBand.LOW,
        totalOGPScore = 31,
        validationError = emptyList(),
      ),
    ),
  )

  @Test
  fun `testing single test case`() {
    val input = OGPInputValidated(
      ogrs3TwoYear = 81,
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
    val output = testSubject.getOGPOutput(input)
    val expected = OGPObject(
      ogpReoffendingOneYear = 53,
      ogpReoffendingTwoYear = 68,
      bandOGP = RiskBand.HIGH,
      totalOGPScore = 62,
      validationError = emptyList(),
    )
    assertEquals(expected, output)
  }

  @ParameterizedTest
  @MethodSource("getOGPTestCases")
  fun `testing from OGP test cases`(input: OGPInputValidated, expected: OGPObject) {
    assertEquals(expected, testSubject.getOGPOutput(input))
  }
}
