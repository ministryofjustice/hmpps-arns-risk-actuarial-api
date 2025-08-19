package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validPNIRiskScoreRequest

class PNIValidationHelperTest {

  @Test
  fun `opdInitialValidation no errors`() {
    val result = pniInitialValidation(validPNIRiskScoreRequest())
    assertTrue(result.isEmpty())
  }

  @Test
  fun `opdInitialValidation missing field error with all field populated`() {
    val request = validPNIRiskScoreRequest().copy(
      gender = null,
      supervisionStatus = null,
    )
    val result = pniInitialValidation(request)

    val expectedFields = listOf(
      "supervisionStatus",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_INPUT, error.type)
    assertEquals("ERR5 - Field is Null", error.message)
    assertEquals(expectedFields, error.fields)
  }
}
