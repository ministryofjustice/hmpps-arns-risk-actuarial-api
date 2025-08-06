package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp.OGPBand
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

    fun currentAccommodationOffendersScore(currentAccomodation: Boolean): Int = currentAccomodation.booleanToScore()

    fun employmentStatusOffendersScore(employmentStatus: Boolean): Int = employmentStatus.booleanToScore()

    fun regularOffendingActivitiesOffendersScore(regularOffendingActivities: ProblemLevel) = regularOffendingActivities.score

    fun currentDrugMisuseOffendersScore(currentDrugMisuse: ProblemLevel) = currentDrugMisuse.score

    fun motivationDrugOffendersScore(motivationDrug: ProblemLevel) = motivationDrug.score

    fun problemSolvingSkillsOffendersScore(problemSolvingSkillsOffendersScore: ProblemLevel) = problemSolvingSkillsOffendersScore.score

    fun awarenessOfConsequencesOffendersScore(awarenessOfConsequences: ProblemLevel) = awarenessOfConsequences.score

    fun understandsPeoplesViewsOffendersScore(understandsPeoplesOffendersScore: ProblemLevel) = understandsPeoplesOffendersScore.score

    fun proCriminalAttitudesOffendersScore(proCriminalAttitudes: ProblemLevel) = proCriminalAttitudes.score

    fun drugMisuseNonViolentOffendersScore(currentDrugMisuseOffendersScore: Int, motivationDrugOffendersScore: Int) = currentDrugMisuseOffendersScore + motivationDrugOffendersScore

    fun thinkingAndBehaviourNonViolentOffendersScore(
      problemSolvingSkillsOffendersScore: Int,
      awarenessOfConsequencesOffendersScore: Int,
      understandsPeoplesViewsOffendersScore: Int,
    ) = problemSolvingSkillsOffendersScore + awarenessOfConsequencesOffendersScore + understandsPeoplesViewsOffendersScore

    // Weighted Values

    fun ogrs3TwoYearWeighted(ogrs3TwoYear: Int): Int = (ogrs3TwoYear * 0.6).roundToInt()

    fun currentAccommodationWeighted(currentAccommodationOffendersScore: Int): Int = if (currentAccommodationOffendersScore == 2) 5 else 0

    fun employmentStatusWeighted(employmentStatusOffendersScore: Int): Int = if (employmentStatusOffendersScore == 2) 5 else 0

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
      currentAccommodationWeighted: Int,
      employmentStatusWeighted: Int,
      regularOffendingActivitiesWeighted: Int,
      drugMisuseNonViolentWeighted: Int,
      thinkingAndBehaviourNonViolentWeighted: Int,
      proCriminalAttitudesWeighted: Int,
    ): Int = (
      ogrs3TwoYearWeighted +
        currentAccommodationWeighted +
        employmentStatusWeighted +
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

    fun bandOGP(ogpReoffendingTwoYear: Int) = OGPBand.findBand(ogpReoffendingTwoYear)
  }
}
