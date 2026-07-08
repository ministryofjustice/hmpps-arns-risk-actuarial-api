package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class CombinedSeriousReoffendingPredictorValidatorTest {

  @Mock
  private lateinit var commonValidator: CommonValidator

  @InjectMocks
  private lateinit var validator: CombinedSeriousReoffendingPredictorValidator

  private val expectedStaticRequiredFields = listOf(
    RiskScoreRequest::gender,
    RiskScoreRequest::hasEverCommittedSexualOffence,
  )

  @Test
  fun `test validateStatic`() {
    // Create request object
    val request: RiskScoreRequest = mock()

    val validationError1 = ValidationErrorType.MISSING_MANDATORY_INPUT.asError(listOf("field1", "field2"))

    // Mock common validator method calls
    whenever(commonValidator.validateRequiredFields(request, expectedStaticRequiredFields, StaticOrDynamic.STATIC)).thenReturn(validationError1)

    // Check that validation errors are returned
    assertEquals(listOf(validationError1), validator.validateStatic(request))

    // verify each validation method is called once
    verify(commonValidator).validateRequiredFields(request, expectedStaticRequiredFields, StaticOrDynamic.STATIC)

    verifyNoMoreInteractions(commonValidator)
  }
}
