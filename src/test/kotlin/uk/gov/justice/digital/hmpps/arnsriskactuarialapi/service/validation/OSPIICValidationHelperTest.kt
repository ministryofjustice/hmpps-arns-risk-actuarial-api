package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.RiskScoreRequestTestConstants.FULL_OSPIIC_REQUEST
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.RiskScoreRequestTestConstants.NULL_REQUEST
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validOSPDCRiskScoreRequest

class OSPIICValidationHelperTest {

  @Test
  fun `initial validation no errors`() {
    val result = validateOSPIIC(FULL_OSPIIC_REQUEST)
    println(result)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `initial validation with no gender`() {
    val result = validateOSPIIC(FULL_OSPIIC_REQUEST.copy(gender = null))
    assertEquals(
      listOf(
        ValidationErrorResponse(
          type = ValidationErrorType.MISSING_MANDATORY_INPUT,
          message = "Mandatory input field(s) missing",
          fields = listOf("gender"),
        ),
      ),
      result,
    )
  }

  @Test
  fun `initial validation for female`() {
    val result = validateOSPIIC(NULL_REQUEST.copy(gender = Gender.FEMALE))
    assertTrue(result.isEmpty())
  }

  @Test
  fun `initial validation for male`() {
    val result = validateOSPIIC(NULL_REQUEST.copy(gender = Gender.MALE))
    assertEquals(
      listOf(
        ValidationErrorResponse(
          type = ValidationErrorType.MISSING_MANDATORY_INPUT,
          message = "Mandatory input field(s) missing",
          fields = listOf(
            "totalContactAdultSexualSanctions",
            "totalContactChildSexualSanctions",
            "totalIndecentImageSanctions",
            "totalNonContactSexualOffences",
          ),
        ),
      ),
      result,
    )
  }

  @Test
  fun `ospiicSexualOffenceTrueValidation error when hasEverCommittedSexualOffence is true but values are 0`() {
    val request = validOSPDCRiskScoreRequest().copy(
      hasEverCommittedSexualOffence = true,
      totalContactAdultSexualSanctions = 0,
      totalContactChildSexualSanctions = 0,
      totalIndecentImageSanctions = 0,
      totalNonContactSexualOffences = 0,
    )

    val expectedFields = listOf(
      "totalContactAdultSexualSanctions",
      "totalContactChildSexualSanctions",
      "totalIndecentImageSanctions",
      "totalNonContactSexualOffences",
    )

    val result = validateOSPIIC(request)

    val error = result.first()
    assertEquals(ValidationErrorType.UNEXPECTED_VALUE, error.type)
    assertEquals("Error: Sexual motivation/offending identified - please complete sexual offence counts.", error.message)
    assertEquals(expectedFields, error.fields)
  }
}
