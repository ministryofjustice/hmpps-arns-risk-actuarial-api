package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import kotlin.reflect.KProperty1

@Component
class ImagesAndIndirectContactSexualReoffendingPredictorValidator(val commonValidator: CommonValidator) : AbstractActuarialValidator(commonValidator) {
  companion object {
    val IMAGES_AND_INDIRECT_CONTACT_SEXUAL_REOFFENDING_PREDICTOR_REQUIRED_FIELDS: List<KProperty1<RiskScoreRequest, Any?>> =
      listOf(
        RiskScoreRequest::totalIndecentImageSanctions,
        RiskScoreRequest::totalContactAdultSexualSanctions,
        RiskScoreRequest::totalContactChildSexualSanctions,
        RiskScoreRequest::totalNonContactSexualOffences,
      )
  }

  override fun validateStaticCustom(request: RiskScoreRequest): List<ValidationError> = commonValidator.validateImagesAndIndirectSexualFields(request, IMAGES_AND_INDIRECT_CONTACT_SEXUAL_REOFFENDING_PREDICTOR_REQUIRED_FIELDS)

  override fun validateDynamicCustom(request: RiskScoreRequest): List<ValidationError> {
    // Not applicable
    return listOfNotNull()
  }

  override fun staticRequiredFields(): List<KProperty1<RiskScoreRequest, Any?>> = listOf(
    RiskScoreRequest::gender,
    RiskScoreRequest::hasEverCommittedSexualOffence,
  )

  override fun dynamicRequiredFields(): List<KProperty1<RiskScoreRequest, Any?>> {
    // Not applicable
    return listOfNotNull()
  }
}
