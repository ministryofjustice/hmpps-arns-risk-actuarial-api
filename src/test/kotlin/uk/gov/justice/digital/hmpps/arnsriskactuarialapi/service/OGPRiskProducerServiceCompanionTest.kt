package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp.OGPBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp.OGPInputValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp.OGPObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.OGPRiskProducerService.Companion.getOGPOutput

class OGPRiskProducerServiceCompanionTest {

  companion object {

    @JvmStatic
    fun getOGPTestCases(): List<Arguments> = listOf(
      Arguments.of(
        OGPInputValidated(
          ogrs3TwoYear = 81,
          currentAccommodation = false,
          employmentStatus = true,
          regularOffendingActivities = ProblemLevel.SOME_PROBLEMS,
          currentDrugMisuse = ProblemLevel.NO_PROBLEMS,
          motivationDrug = ProblemLevel.NO_PROBLEMS,
          problemSolvingSkills = ProblemLevel.SOME_PROBLEMS,
          awarenessOfConsequences = ProblemLevel.SOME_PROBLEMS,
          understandsPeoplesViews = ProblemLevel.NO_PROBLEMS,
          proCriminalAttitudes = ProblemLevel.SOME_PROBLEMS,
        ),
        OGPObject(
          ogpReoffendingOneYear = 53,
          ogpReoffendingTwoYear = 68,
          bandOGP = OGPBand.HIGH,
          totalOGPScore = 62,
          validationError = emptyList(),
        ),
      ),
      Arguments.of(
        OGPInputValidated(
          ogrs3TwoYear = 67,
          currentAccommodation = false,
          employmentStatus = true,
          regularOffendingActivities = ProblemLevel.SOME_PROBLEMS,
          currentDrugMisuse = ProblemLevel.NO_PROBLEMS,
          motivationDrug = ProblemLevel.NO_PROBLEMS,
          problemSolvingSkills = ProblemLevel.SOME_PROBLEMS,
          awarenessOfConsequences = ProblemLevel.SOME_PROBLEMS,
          understandsPeoplesViews = ProblemLevel.NO_PROBLEMS,
          proCriminalAttitudes = ProblemLevel.SOME_PROBLEMS,
        ),
        OGPObject(
          ogpReoffendingOneYear = 40,
          ogpReoffendingTwoYear = 55,
          bandOGP = OGPBand.MEDIUM,
          totalOGPScore = 53,
          validationError = emptyList(),
        ),
      ),
      Arguments.of(
        OGPInputValidated(
          ogrs3TwoYear = 37,
          currentAccommodation = false,
          employmentStatus = true,
          regularOffendingActivities = ProblemLevel.SOME_PROBLEMS,
          currentDrugMisuse = ProblemLevel.SIGNIFICANT_PROBLEMS,
          motivationDrug = ProblemLevel.NO_PROBLEMS,
          problemSolvingSkills = ProblemLevel.SOME_PROBLEMS,
          awarenessOfConsequences = ProblemLevel.SOME_PROBLEMS,
          understandsPeoplesViews = ProblemLevel.NO_PROBLEMS,
          proCriminalAttitudes = ProblemLevel.SOME_PROBLEMS,
        ),
        OGPObject(
          ogpReoffendingOneYear = 26,
          ogpReoffendingTwoYear = 39,
          bandOGP = OGPBand.MEDIUM,
          totalOGPScore = 43,
          validationError = emptyList(),
        ),
      ),
      Arguments.of(
        OGPInputValidated(
          ogrs3TwoYear = 17,
          currentAccommodation = false,
          employmentStatus = true,
          regularOffendingActivities = ProblemLevel.SOME_PROBLEMS,
          currentDrugMisuse = ProblemLevel.SIGNIFICANT_PROBLEMS,
          motivationDrug = ProblemLevel.NO_PROBLEMS,
          problemSolvingSkills = ProblemLevel.SOME_PROBLEMS,
          awarenessOfConsequences = ProblemLevel.SOME_PROBLEMS,
          understandsPeoplesViews = ProblemLevel.NO_PROBLEMS,
          proCriminalAttitudes = ProblemLevel.SOME_PROBLEMS,
        ),
        OGPObject(
          ogpReoffendingOneYear = 15,
          ogpReoffendingTwoYear = 24,
          bandOGP = OGPBand.LOW,
          totalOGPScore = 31,
          validationError = emptyList(),
        ),
      ),
    )
  }

  @Test
  fun `testing single test case`() {
    val input = OGPInputValidated(
      ogrs3TwoYear = 81,
      currentAccommodation = false,
      employmentStatus = true,
      regularOffendingActivities = ProblemLevel.SOME_PROBLEMS,
      currentDrugMisuse = ProblemLevel.NO_PROBLEMS,
      motivationDrug = ProblemLevel.NO_PROBLEMS,
      problemSolvingSkills = ProblemLevel.SOME_PROBLEMS,
      awarenessOfConsequences = ProblemLevel.SOME_PROBLEMS,
      understandsPeoplesViews = ProblemLevel.NO_PROBLEMS,
      proCriminalAttitudes = ProblemLevel.SOME_PROBLEMS,
    )
    val output = getOGPOutput(input)
    val expected = OGPObject(
      ogpReoffendingOneYear = 53,
      ogpReoffendingTwoYear = 68,
      bandOGP = OGPBand.HIGH,
      totalOGPScore = 62,
      validationError = emptyList(),
    )
    assertEquals(expected, output)
  }

  @ParameterizedTest()
  @MethodSource("getOGPTestCases")
  fun `testing from OGP test cases`(input: OGPInputValidated, expected: OGPObject) {
    assertEquals(expected, getOGPOutput(input))
  }
}
