package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validPNIRiskScoreRequest

@ExtendWith(MockitoExtension::class)
class PNIValidationHelperTest {

  @Mock
  private lateinit var commonValidator : CommonValidator
  @InjectMocks
  private lateinit var validator : PNIValidator

  @Test
  fun `pniInitialValidation no errors`() {
    val result = validator.validatePNI(validPNIRiskScoreRequest())
    assertTrue(result.isEmpty())
  }

  @Test
  fun `pniInitialValidation missing field error with all field populated`() {
    val request = validPNIRiskScoreRequest().copy(
      gender = null,
      supervisionStatus = null,
    )

    val expectedError = ValidationError(
      type = ValidationErrorType.MISSING_MANDATORY_INPUT,
      message = "Mandatory input field(s) missing",
      fields = listOf("supervisionStatus"),
    )

    whenever(commonValidator.validateRequiredFields(request, listOf(RiskScoreRequest::supervisionStatus))).thenReturn(expectedError)

    val result = validator.validatePNI(request)

    assertEquals(listOf(expectedError), result)
  }
}
