package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import kotlin.reflect.KProperty1

@Component
class CombinedSeriousReoffendingPredictorValidator(val commonValidator: CommonValidator) : AbstractActuarialValidator(commonValidator) {

  override fun validateStaticCustom(request: RiskScoreRequest): List<ValidationError> = listOfNotNull()
  override fun validateDynamicCustom(request: RiskScoreRequest): List<ValidationError> = emptyList()

  override fun staticRequiredFields(): List<KProperty1<RiskScoreRequest, Any?>> = listOf(
    RiskScoreRequest::gender,
    RiskScoreRequest::hasEverCommittedSexualOffence,
  )

  override fun dynamicRequiredFields(): List<KProperty1<RiskScoreRequest, Any?>> = listOf()
}
