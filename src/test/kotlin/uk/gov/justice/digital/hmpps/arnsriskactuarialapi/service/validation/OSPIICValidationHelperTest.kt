package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.RiskScoreRequestTestConstants.FULL_OSPIIC_REQUEST
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.RiskScoreRequestTestConstants.NULL_REQUEST
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.OSPIICValidationHelper.Companion.ospiicInitialValidation

class OSPIICValidationHelperTest {

  @Test
  fun `initial validation no errors`() {
    val result = ospiicInitialValidation(FULL_OSPIIC_REQUEST)
    println(result)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `initial validation with no gender`() {
    val result = ospiicInitialValidation(FULL_OSPIIC_REQUEST.copy(gender = null))
    assertEquals(
      listOf(
        ValidationErrorResponse(
          type = ValidationErrorType.MISSING_INPUT,
          message = "ERR5 - Field is Null",
          fields = listOf("gender"),
        ),
      ),
      result,
    )
  }

  @Test
  fun `initial validation for female`() {
    val result = ospiicInitialValidation(NULL_REQUEST.copy(gender = Gender.FEMALE))
    assertTrue(result.isEmpty())
  }

  @Test
  fun `initial validation for male`() {
    val result = ospiicInitialValidation(NULL_REQUEST.copy(gender = Gender.MALE))
    assertEquals(
      listOf(
        ValidationErrorResponse(
          type = ValidationErrorType.MISSING_INPUT,
          message = "ERR5 - Field is Null",
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
}
