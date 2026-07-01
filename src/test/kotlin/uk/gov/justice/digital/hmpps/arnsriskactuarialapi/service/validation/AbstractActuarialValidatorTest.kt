package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import kotlin.reflect.KProperty1

class AbstractActuarialValidatorTest {

  companion object {
    val customError1 = ValidationErrorType.OFFENCE_CODE_INCORRECT_FORMAT.asError(listOf("field1"))
    val customError2 = ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_REQUIRED.asError(listOf("field2"))
    val customError3 = ValidationErrorType.NEED_DETAILS_OF_EXACT_OFFENCE.asError(listOf("field3"))
    val customError4 = ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_BEFORE_DATE_OF_BIRTH.asError(listOf("field2"))
  }

  private val commonValidator: CommonValidator = mock()

  private class TestActuarialValidator(
    commonValidator: CommonValidator,
  ) : AbstractActuarialValidator(commonValidator) {
    override fun validateStaticCustom(request: RiskScoreRequest) = listOf(customError1, customError2)
    override fun validateDynamicCustom(request: RiskScoreRequest) = listOf(customError3, customError4)
    override fun staticRequiredFields() = emptyList<KProperty1<RiskScoreRequest, Any?>>()
    override fun dynamicRequiredFields() = emptyList<KProperty1<RiskScoreRequest, Any?>>()
  }

  @Test
  fun `validateStatic should combine required field errors and custom errors`() {
    val request: RiskScoreRequest = mock()
    val requiredFieldError = ValidationErrorType.MISSING_MANDATORY_INPUT.asError(listOf("field1", "field2"))

    whenever(commonValidator.validateRequiredFields(request, emptyList(), StaticOrDynamic.STATIC))
      .thenReturn(requiredFieldError)

    val validator = TestActuarialValidator(commonValidator)

    val actualErrors = validator.validateStatic(request)

    assertEquals(listOf(requiredFieldError, customError1, customError2), actualErrors)
    verify(commonValidator).validateRequiredFields(request, emptyList(), StaticOrDynamic.STATIC)
  }

  @Test
  fun `validateStatic should combine required field errors and custom errors - null required fields error`() {
    val request: RiskScoreRequest = mock()

    whenever(commonValidator.validateRequiredFields(request, emptyList(), StaticOrDynamic.STATIC))
      .thenReturn(null)

    val validator = TestActuarialValidator(commonValidator)

    val actualErrors = validator.validateStatic(request)

    assertEquals(listOf(customError1, customError2), actualErrors)
    verify(commonValidator).validateRequiredFields(request, emptyList(), StaticOrDynamic.STATIC)
  }

  @Test
  fun `validateDynamic should combine required field errors and custom errors`() {
    val request: RiskScoreRequest = mock()
    val requiredFieldError = ValidationErrorType.MISSING_MANDATORY_INPUT.asError(listOf("field1", "field2"))

    whenever(commonValidator.validateRequiredFields(request, emptyList(), StaticOrDynamic.DYNAMIC))
      .thenReturn(requiredFieldError)

    val validator = TestActuarialValidator(commonValidator)

    val actualErrors = validator.validateDynamic(request)

    assertEquals(listOf(requiredFieldError, customError3, customError4), actualErrors)
    verify(commonValidator).validateRequiredFields(request, emptyList(), StaticOrDynamic.DYNAMIC)
  }

  @Test
  fun `validateDynamic should combine required field errors and custom errors - null required fields error`() {
    val request: RiskScoreRequest = mock()

    whenever(commonValidator.validateRequiredFields(request, emptyList(), StaticOrDynamic.DYNAMIC))
      .thenReturn(null)

    val validator = TestActuarialValidator(commonValidator)

    val actualErrors = validator.validateDynamic(request)

    assertEquals(listOf(customError3, customError4), actualErrors)
    verify(commonValidator).validateRequiredFields(request, emptyList(), StaticOrDynamic.DYNAMIC)
  }
}
