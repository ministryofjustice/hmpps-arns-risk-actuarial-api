package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validOSPDCRiskScoreRequest
import kotlin.collections.listOf

class OSPDCValidationHelperTest {

  @Test
  fun `oospdcInitialValidation no errors`() {
    val result = ospdcInitialValidation(validOSPDCRiskScoreRequest())
    assertTrue(result.isEmpty())
  }

  @Test
  fun `oospdcInitialValidation missing field error with all field populated`() {
    val request = validOSPDCRiskScoreRequest().copy(
      gender = null,
      dateOfBirth = null,
      hasCommittedSexualOffence = null,
      totalContactAdultSexualSanctions = null,
      totalContactChildSexualSanctions = null,
      totalNonContactSexualOffences = null,
      totalIndecentImageSanctions = null,
      dateAtStartOfFollowup = null,
      dateOfMostRecentSexualOffence = null,
      totalNumberOfSanctions = null,
      inCustodyOrCommunity = null,
      mostRecentOffenceDate = null,
    )

    val result = ospdcInitialValidation(request)

    val expectedFields = listOf(
      "gender",
      "dateOfBirth",
      "hasCommittedSexualOffence",
      "totalContactAdultSexualSanctions",
      "totalContactChildSexualSanctions",
      "totalNonContactSexualOffences",
      "totalIndecentImageSanctions",
      "dateAtStartOfFollowup",
      "dateOfMostRecentSexualOffence",
      "totalNumberOfSanctions",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_INPUT, error.type)
    assertEquals("ERR5 - Field is Null", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @Test
  fun `oospdcInitialValidation not applicable error when hasCommittedSexualOffence is false`() {
    val request = validOSPDCRiskScoreRequest().copy(
      hasCommittedSexualOffence = false,
    )

    val result = ospdcInitialValidation(request)
    val expectedFields = listOf("hasCommittedSexualOffence")

    val error = result.first()
    assertEquals(ValidationErrorType.NOT_APPLICABLE, error.type)
    assertEquals("ERR - Does not meet eligibility criteria", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @Test
  fun `oospdcInitialValidation no error when hasCommittedSexualOffence is true`() {
    val request = validOSPDCRiskScoreRequest().copy(
      hasCommittedSexualOffence = true,
    )

    val result = ospdcInitialValidation(request)

    assertTrue(result.isEmpty())
  }
}
