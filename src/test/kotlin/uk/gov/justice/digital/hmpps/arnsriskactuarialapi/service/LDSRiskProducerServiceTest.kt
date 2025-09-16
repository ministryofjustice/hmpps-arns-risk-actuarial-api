package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.HasQualifications
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.LDSObject

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LDSRiskProducerServiceTest {

  companion object {

    fun String?.toLDSBoolean() = when (this) {
      null -> null
      "Yes" -> true
      else -> false
    }

    fun String?.toLDSProblemLevel() = when (this) {
      null -> null
      else -> ProblemLevel.valueOf(this)
    }

    fun String?.toHasQualifications() = when (this) {
      null -> null
      else -> HasQualifications.valueOf(this)
    }

    fun toRequest(
      isCurrentlyOfNoFixedAbodeOrTransientAccommodationCSV: String?,
      workRelatedSkillsCSV: String?,
      problemsWithReadingWritingNumeracyCSV: String?,
      hasProblemsWithReadingCSV: String?,
      hasProblemsWithNumeracyCSV: String?,
      learningDifficultiesCSV: String?,
      professionalOrVocationalQualificationsCSV: String?,
    ): RiskScoreRequest = RiskScoreRequest(
      isCurrentlyOfNoFixedAbodeOrTransientAccommodation = isCurrentlyOfNoFixedAbodeOrTransientAccommodationCSV.toLDSBoolean(),
      workRelatedSkills = workRelatedSkillsCSV.toLDSProblemLevel(),
      problemsWithReadingWritingNumeracy = problemsWithReadingWritingNumeracyCSV.toLDSProblemLevel(),
      hasProblemsWithReading = hasProblemsWithReadingCSV.toLDSBoolean(),
      hasProblemsWithNumeracy = hasProblemsWithNumeracyCSV.toLDSBoolean(),
      learningDifficulties = learningDifficultiesCSV.toLDSProblemLevel(),
      professionalOrVocationalQualifications = professionalOrVocationalQualificationsCSV.toHasQualifications(),
    )
  }

  private val service = LDSRiskProducerService()
  private val context = RiskScoreContext(RiskScoreVersion.V1_0)

  @Test
  fun `single request`() {
    // No,NO_PROBLEMS,NO_PROBLEMS,Null,Null,NO_PROBLEMS,ANY_QUALIFICATION,0,0,5 items scored
    val request = toRequest(
      isCurrentlyOfNoFixedAbodeOrTransientAccommodationCSV = "No",
      workRelatedSkillsCSV = "NO_PROBLEMS",
      problemsWithReadingWritingNumeracyCSV = "NO_PROBLEMS",
      hasProblemsWithReadingCSV = null,
      hasProblemsWithNumeracyCSV = null,
      learningDifficultiesCSV = "NO_PROBLEMS",
      professionalOrVocationalQualificationsCSV = "ANY_QUALIFICATION",
    )
    val result = service.getRiskScore(request, context)
    val expected = RiskScoreContext(
      RiskScoreVersion.V1_0,
      LDS = LDSObject(0, emptyList()),
    )
    assertEquals(expected, result)
  }
}
