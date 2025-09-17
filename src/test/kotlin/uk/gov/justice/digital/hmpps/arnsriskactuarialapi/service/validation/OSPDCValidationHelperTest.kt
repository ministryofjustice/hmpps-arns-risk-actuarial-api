package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validOSPDCRiskScoreRequest
import kotlin.collections.listOf

class OSPDCValidationHelperTest {

  @Test
  fun `oospdcInitialValidation no errors`() {
    val result = validateOSPDC(validOSPDCRiskScoreRequest())
    assertTrue(result.isEmpty())
  }

  @Test
  fun `oospdcInitialValidation missing field error with all field populated`() {
    val request = validOSPDCRiskScoreRequest().copy(
      gender = null,
      dateOfBirth = null,
      hasEverCommittedSexualOffence = null,
      totalContactAdultSexualSanctions = null,
      totalContactChildSexualSanctions = null,
      totalNonContactSexualOffences = null,
      totalIndecentImageSanctions = null,
      dateAtStartOfFollowup = null,
      dateOfMostRecentSexualOffence = null,
      totalNumberOfSanctionsForAllOffences = null,
      supervisionStatus = null,
      mostRecentOffenceDate = null,
    )

    val result = validateOSPDC(request)

    val expectedFields = listOf(
      "gender",
      "dateOfBirth",
      "totalContactAdultSexualSanctions",
      "totalContactChildSexualSanctions",
      "totalNonContactSexualOffences",
      "totalIndecentImageSanctions",
      "dateAtStartOfFollowup",
      "totalNumberOfSanctionsForAllOffences",
      "supervisionStatus",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_MANDATORY_INPUT, error.type)
    assertEquals("Mandatory input field(s) missing", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @Test
  fun `oospdcInitialValidation missing sexual motivation question`() {
    val request = validOSPDCRiskScoreRequest().copy(
      gender = Gender.MALE,
      hasEverCommittedSexualOffence = true,
      isCurrentOffenceSexuallyMotivated = null,
    )

    val result = validateOSPDC(request)

    val expectedFields = listOf(
      "isCurrentOffenceSexuallyMotivated",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_MANDATORY_INPUT, error.type)
    assertEquals("Mandatory input field(s) missing", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @Test
  fun `oospdcInitialValidation no error when hasEverCommittedSexualOffence is true`() {
    val request = validOSPDCRiskScoreRequest().copy(
      hasEverCommittedSexualOffence = true,
    )

    val result = validateOSPDC(request)

    assertTrue(result.isEmpty())
  }
}
