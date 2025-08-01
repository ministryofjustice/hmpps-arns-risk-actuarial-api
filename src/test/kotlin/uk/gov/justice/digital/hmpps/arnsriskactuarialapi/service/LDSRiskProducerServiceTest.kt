package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.HasQualifications
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.LDSObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.LDSValidationHelper.Companion.ELIGIBLE_FIELDS
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.LDSValidationHelper.Companion.ERR_LESS_THAN_THREE_FIELDS

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LDSRiskProducerServiceTest {

  companion object {

    const val NOT_ENOUGH_ITEMS_SCORED = "Not enough items scored"

    val LDS_ERROR_OBJECT =
      LDSObject(
        null,
        listOf(
          ValidationErrorResponse(
            type = ValidationErrorType.NOT_APPLICABLE,
            message = ERR_LESS_THAN_THREE_FIELDS,
            fields = ELIGIBLE_FIELDS,
          ),
        ),
      )

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
      currentAccommodationCSV: String?,
      transferableSkillsCSV: String?,
      educationDifficultiesCSV: String?,
      readingDifficultiesCSV: String?,
      numeracyDifficultiesCSV: String?,
      learningDifficultiesCSV: String?,
      professionalOrVocationalQualificationsCSV: String?,
    ): RiskScoreRequest = RiskScoreRequest(
      currentAccommodation = currentAccommodationCSV.toLDSBoolean(),
      transferableSkills = transferableSkillsCSV.toLDSProblemLevel(),
      educationDifficulties = educationDifficultiesCSV.toLDSProblemLevel(),
      readingDifficulties = readingDifficultiesCSV.toLDSBoolean(),
      numeracyDifficulties = numeracyDifficultiesCSV.toLDSBoolean(),
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
      currentAccommodationCSV = "No",
      transferableSkillsCSV = "NO_PROBLEMS",
      educationDifficultiesCSV = "NO_PROBLEMS",
      readingDifficultiesCSV = null,
      numeracyDifficultiesCSV = null,
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

  @ParameterizedTest(name = "[{index}] {arguments}")
  @CsvFileSource(
    resources = ["/data/LDS/LDS_Test_Data.csv"],
    useHeadersInDisplayName = true,
    nullValues = ["Null", "Missing"],
  )
  fun `requests from CSV file`(
    currentAccommodationCSV: String?,
    transferableSkillsCSV: String?,
    educationDifficultiesCSV: String?,
    readingDifficultiesCSV: String?,
    numeracyDifficultiesCSV: String?,
    learningDifficultiesCSV: String?,
    professionalOrVocationalQualificationsCSV: String?,
    @Suppress("UNUSED_PARAMETER") ldsSubTotalCSV: String?,
    ldsScoreCSV: String?,
    commentsCSV: String?,
  ) {
    val request = toRequest(
      currentAccommodationCSV = currentAccommodationCSV,
      transferableSkillsCSV = transferableSkillsCSV,
      educationDifficultiesCSV = educationDifficultiesCSV,
      readingDifficultiesCSV = readingDifficultiesCSV,
      numeracyDifficultiesCSV = numeracyDifficultiesCSV,
      learningDifficultiesCSV = learningDifficultiesCSV,
      professionalOrVocationalQualificationsCSV = professionalOrVocationalQualificationsCSV,
    )
    val result = service.getRiskScore(request, context)
    val expected = RiskScoreContext(
      RiskScoreVersion.V1_0,
      LDS = if (NOT_ENOUGH_ITEMS_SCORED == commentsCSV) {
        LDS_ERROR_OBJECT
      } else {
        LDSObject(ldsScoreCSV!!.toInt(), emptyList())
      },
    )
    assertEquals(expected, result)
  }
}
