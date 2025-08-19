package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.HasQualifications

class LDSTransformationHelper {
  companion object {

    // Transformation Functions

    fun isCurrentlyOfNoFixedAbodeOrTransientAccommodationOffendersScoreLDS(isCurrentlyOfNoFixedAbodeOrTransientAccommodation: Boolean?): Int = if (isCurrentlyOfNoFixedAbodeOrTransientAccommodation ?: false) 1 else 0

    fun workRelatedSkillsOffendersScore(workRelatedSkills: ProblemLevel?): Int = (workRelatedSkills ?: ProblemLevel.NO_PROBLEMS).score

    fun problemsWithReadingWritingNumeracyOffendersScore(problemsWithReadingWritingNumeracy: ProblemLevel?): Int = (problemsWithReadingWritingNumeracy ?: ProblemLevel.NO_PROBLEMS).score

    fun hasProblemsWithReadingOffendersScore(hasProblemsWithReading: Boolean?): Int = if (hasProblemsWithReading ?: false) 1 else 0

    fun hasProblemsWithNumeracyOffendersScore(hasProblemsWithNumeracy: Boolean?): Int = if (hasProblemsWithNumeracy ?: false) 1 else 0

    fun learningDifficultiesOffendersScore(learningDifficulties: ProblemLevel?) = (learningDifficulties ?: ProblemLevel.NO_PROBLEMS).score

    fun professionalOrVocationalQualificationsOffendersScore(professionalOrVocationalQualifications: HasQualifications?) = (professionalOrVocationalQualifications ?: HasQualifications.ANY_QUALIFICATION).score

    // Final Outputs

    fun ldsSubTotal(
      isCurrentlyOfNoFixedAbodeOrTransientAccommodationOffendersScoreLDS: Int,
      workRelatedSkillsOffendersScore: Int,
      problemsWithReadingWritingNumeracyOffendersScore: Int,
      hasProblemsWithReadingOffendersScore: Int,
      hasProblemsWithNumeracyOffendersScore: Int,
      learningDifficultiesOffendersScore: Int,
      professionalOrVocationalQualifications: Int,
    ): Int = (
      isCurrentlyOfNoFixedAbodeOrTransientAccommodationOffendersScoreLDS +
        workRelatedSkillsOffendersScore +
        problemsWithReadingWritingNumeracyOffendersScore +
        hasProblemsWithReadingOffendersScore +
        hasProblemsWithNumeracyOffendersScore +
        learningDifficultiesOffendersScore +
        professionalOrVocationalQualifications
      )

    fun ldsScore(ldsSubTotal: Int) = when {
      ldsSubTotal in (0..2) -> 0
      ldsSubTotal in (3..4) -> 1
      (ldsSubTotal >= 5) -> 2
      else -> null
    }
  }
}
