package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.RiskScoreRequestTestConstants.BAD_READING_DIFFICULTY_LDS_REQUEST
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.RiskScoreRequestTestConstants.FULL_LDS_REQUEST
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.RiskScoreRequestTestConstants.INELIGIBLE_LDS_REQUEST
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.RiskScoreRequestTestConstants.NULL_REQUEST
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType

class LDSValidationHelperTest {

  @Test
  fun `initial validation no errors`() {
    val result = validateLDS(FULL_LDS_REQUEST)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `fields not eligible`() {
    val result = validateLDS(NULL_REQUEST)
    assertEquals(
      listOf(
        ValidationErrorResponse(
          type = ValidationErrorType.LDS_NOT_ENOUGH_FIELDS_PRESENT,
          message = "At least three input fields must be provided",
          fields = listOf("workRelatedSkills", "problemsWithReadingWritingNumeracy", "learningDifficulties", "professionalOrVocationalQualifications"),
        ),
      ),
      result,
    )
  }

  @Test
  fun `educational field not present but reading difficulty present`() {
    val result = validateLDS(BAD_READING_DIFFICULTY_LDS_REQUEST)
    assertEquals(
      listOf(
        ValidationErrorResponse(
          type = ValidationErrorType.MISSING_MANDATORY_INPUT,
          message = "Mandatory input field(s) missing",
          fields = listOf(
            "problemsWithReadingWritingNumeracy Field Not Present But hasProblemsWithReading Present",
          ),
        ),
      ),
      result,
    )
  }

  @Test
  fun `fields not eligible but some present error`() {
    val result = validateLDS(INELIGIBLE_LDS_REQUEST)
    assertEquals(
      listOf(
        ValidationErrorResponse(
          type = ValidationErrorType.MISSING_MANDATORY_INPUT,
          message = "Mandatory input field(s) missing",
          fields = listOf(
            "problemsWithReadingWritingNumeracy Field Not Present But hasProblemsWithReading Present",
            "problemsWithReadingWritingNumeracy Field Not Present But hasProblemsWithNumeracy Present",
          ),
        ),
        ValidationErrorResponse(
          type = ValidationErrorType.LDS_NOT_ENOUGH_FIELDS_PRESENT,
          message = "At least three input fields must be provided",
          fields = listOf("workRelatedSkills", "problemsWithReadingWritingNumeracy", "learningDifficulties", "professionalOrVocationalQualifications"),
        ),
      ),
      result,
    )
  }
}
