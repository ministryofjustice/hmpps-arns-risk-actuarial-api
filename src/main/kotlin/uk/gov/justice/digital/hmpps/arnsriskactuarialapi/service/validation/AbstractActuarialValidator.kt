package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import kotlin.reflect.KProperty1

abstract class AbstractActuarialValidator(private val commonValidator: CommonValidator) {

  protected abstract fun validateStaticCustom(request: RiskScoreRequest): List<ValidationError>
  protected abstract fun validateDynamicCustom(request: RiskScoreRequest): List<ValidationError>
  protected abstract fun staticRequiredFields(): List<KProperty1<RiskScoreRequest, Any?>>
  protected abstract fun dynamicRequiredFields(): List<KProperty1<RiskScoreRequest, Any?>>

  fun validateStatic(request: RiskScoreRequest): List<ValidationError> {
    val requiredFieldsValidationErrors = listOfNotNull(validateStaticRequiredFields(request))
    val customValidationErrors = validateStaticCustom(request)
    return requiredFieldsValidationErrors + customValidationErrors
  }

  fun validateDynamic(request: RiskScoreRequest): List<ValidationError> {
    val requiredFieldsValidationErrors = listOfNotNull(validateDynamicRequiredFields(request))
    val customValidationErrors = validateDynamicCustom(request)
    return requiredFieldsValidationErrors + customValidationErrors
  }

  private fun validateStaticRequiredFields(request: RiskScoreRequest): ValidationError? = commonValidator.validateRequiredFields(request, staticRequiredFields(), StaticOrDynamic.STATIC)

  private fun validateDynamicRequiredFields(request: RiskScoreRequest): ValidationError? = commonValidator.validateRequiredFields(request, dynamicRequiredFields(), StaticOrDynamic.DYNAMIC)
}
