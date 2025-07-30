package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.HasQualifications

class LDSTransformationHelper {
  companion object {

    // Transformation Functions

    fun currentAccommodationOffendersScoreLDS(currentAccommodation: Boolean?): Int = if (currentAccommodation ?: false) 1 else 0

    fun transferableSkillsOffendersScore(transferableSkills: ProblemLevel?): Int = (transferableSkills ?: ProblemLevel.NO_PROBLEMS).score

    fun educationDifficultiesOffendersScore(educationDifficulties: ProblemLevel?): Int = (educationDifficulties ?: ProblemLevel.NO_PROBLEMS).score

    fun readingDifficultiesOffendersScore(readingDifficulties: Boolean?): Int = if (readingDifficulties ?: false) 1 else 0

    fun numeracyDifficultiesOffendersScore(numeracyDifficulties: Boolean?): Int = if (numeracyDifficulties ?: false) 1 else 0

    fun learningDifficultiesOffendersScore(learningDifficulties: ProblemLevel?) = (learningDifficulties ?: ProblemLevel.NO_PROBLEMS).score

    fun professionalOrVocationalQualificationsOffendersScore(professionalOrVocationalQualifications: HasQualifications?) = (professionalOrVocationalQualifications ?: HasQualifications.ANY_QUALIFICATION).score

    // Final Outputs

    fun ldsSubTotal(
      currentAccommodationOffendersScoreLDS: Int,
      transferableSkillsOffendersScore: Int,
      educationDifficultiesOffendersScore: Int,
      readingDifficultiesOffendersScore: Int,
      numeracyDifficultiesOffendersScore: Int,
      learningDifficultiesOffendersScore: Int,
      professionalOrVocationalQualifications: Int,
    ): Int = (
      currentAccommodationOffendersScoreLDS +
        transferableSkillsOffendersScore +
        educationDifficultiesOffendersScore +
        readingDifficultiesOffendersScore +
        numeracyDifficultiesOffendersScore +
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
