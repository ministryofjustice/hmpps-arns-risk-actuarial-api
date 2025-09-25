package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.MotivationLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.YesSometimesNo
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.ConversionUtils.Companion.booleanToScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.asPercentage
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.roundToInt
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.sanitisePercentage
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.sigmoid

class OGPTransformationHelper {

  companion object {

    const val REOFFENDING_SCALE: Double = 0.0616
    const val REOFFENDING_ONE_YEAR_SHIFT: Double = 3.6832
    const val REOFFENDING_TWO_YEAR_SHIFT: Double = 3.0795

    // Transformation Functions

    fun isCurrentlyOfNoFixedAbodeOrTransientAccommodationOffendersScore(isCurrentlyOfNoFixedAbodeOrTransientAccommodation: Boolean): Int = isCurrentlyOfNoFixedAbodeOrTransientAccommodation.booleanToScore()

    fun isUnemployedOffendersScore(isUnemployed: Boolean): Int = isUnemployed.booleanToScore()

    fun regularOffendingActivitiesOffendersScore(regularOffendingActivities: ProblemLevel) = regularOffendingActivities.score

    fun currentDrugMisuseOffendersScore(currentDrugMisuse: ProblemLevel) = currentDrugMisuse.score

    fun motivationToTackleDrugMisuseOffendersScore(motivationToTackleDrugMisuse: MotivationLevel) = motivationToTackleDrugMisuse.score

    fun problemSolvingSkillsOffendersScore(problemSolvingSkillsOffendersScore: ProblemLevel) = problemSolvingSkillsOffendersScore.score

    fun awarenessOfConsequencesOffendersScore(awarenessOfConsequences: YesSometimesNo) = awarenessOfConsequences.score

    fun understandsOtherPeoplesViewsOffendersScore(understandsPeoplesOffendersScore: ProblemLevel) = understandsPeoplesOffendersScore.score

    fun proCriminalAttitudesOffendersScore(proCriminalAttitudes: ProblemLevel) = proCriminalAttitudes.score

    fun drugMisuseNonViolentOffendersScore(currentDrugMisuseOffendersScore: Int, motivationToTackleDrugMisuseOffendersScore: Int) = currentDrugMisuseOffendersScore + motivationToTackleDrugMisuseOffendersScore

    fun thinkingAndBehaviourNonViolentOffendersScore(
      problemSolvingSkillsOffendersScore: Int,
      awarenessOfConsequencesOffendersScore: Int,
      understandsOtherPeoplesViewsOffendersScore: Int,
    ) = problemSolvingSkillsOffendersScore + awarenessOfConsequencesOffendersScore + understandsOtherPeoplesViewsOffendersScore

    // Weighted Values

    fun ogrs3TwoYearWeighted(ogrs3TwoYear: Int): Int = (ogrs3TwoYear * 0.6).roundToInt()

    fun isCurrentlyOfNoFixedAbodeOrTransientAccommodationWeighted(isCurrentlyOfNoFixedAbodeOrTransientAccommodationOffendersScore: Int): Int = if (isCurrentlyOfNoFixedAbodeOrTransientAccommodationOffendersScore == 2) 5 else 0

    fun isUnemployedWeighted(isUnemployedOffendersScore: Int): Int = if (isUnemployedOffendersScore == 2) 5 else 0

    fun regularOffendingActivitiesWeighted(regularOffendingActivitiesOffendersScore: Int): Int = when (regularOffendingActivitiesOffendersScore) {
      1 -> 3
      2 -> 5
      else -> 0
    }

    fun drugMisuseNonViolentWeighted(drugMisuseNonViolentOffendersScore: Int): Int = (drugMisuseNonViolentOffendersScore * 3.75).roundToInt()

    fun thinkingAndBehaviourNonViolentWeighted(thinkingAndBehaviourNonViolentOffendersScore: Int): Int = ((thinkingAndBehaviourNonViolentOffendersScore * 5.0) / 6.0).roundToInt()

    fun proCriminalAttitudesWeighted(proCriminalAttitudesWeighted: Int): Int = when (proCriminalAttitudesWeighted) {
      1 -> 3
      2 -> 5
      else -> 0
    }

    // Final Outputs

    fun totalOGPScore(
      ogrs3TwoYearWeighted: Int,
      isCurrentlyOfNoFixedAbodeOrTransientAccommodationWeighted: Int,
      isUnemployedWeighted: Int,
      regularOffendingActivitiesWeighted: Int,
      drugMisuseNonViolentWeighted: Int,
      thinkingAndBehaviourNonViolentWeighted: Int,
      proCriminalAttitudesWeighted: Int,
    ): Int = (
      ogrs3TwoYearWeighted +
        isCurrentlyOfNoFixedAbodeOrTransientAccommodationWeighted +
        isUnemployedWeighted +
        regularOffendingActivitiesWeighted +
        drugMisuseNonViolentWeighted +
        thinkingAndBehaviourNonViolentWeighted +
        proCriminalAttitudesWeighted
      )

    fun ogpReoffendingOneYear(totalOGPScore: Int): Int = (REOFFENDING_SCALE * totalOGPScore - REOFFENDING_ONE_YEAR_SHIFT)
      .sigmoid()
      .asPercentage()
      .sanitisePercentage()

    fun ogpReoffendingTwoYear(totalOGPScore: Int): Int = (REOFFENDING_SCALE * totalOGPScore - REOFFENDING_TWO_YEAR_SHIFT)
      .sigmoid()
      .asPercentage()
      .sanitisePercentage()

    fun bandOGP(ogpReoffendingTwoYear: Int): RiskBand {
      require(ogpReoffendingTwoYear in 1..99) { "Percentage $ogpReoffendingTwoYear should be between 1 and 99" }
      return when (ogpReoffendingTwoYear) {
        in 1..33 -> RiskBand.LOW
        in 34..66 -> RiskBand.MEDIUM
        in 67..84 -> RiskBand.HIGH
        in 85..99 -> RiskBand.VERY_HIGH
        else -> throw IllegalArgumentException("Unexpected percentage value: $ogpReoffendingTwoYear")
      }
    }
  }
}
