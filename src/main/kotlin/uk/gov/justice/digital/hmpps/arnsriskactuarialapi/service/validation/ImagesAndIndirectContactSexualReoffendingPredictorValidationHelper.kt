package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import kotlin.reflect.KProperty1

val IMAGES_AND_INDIRECT_CONTACT_SEXUAL_REOFFENDING_PREDICTOR_REQUIRED_FIELDS: List<KProperty1<RiskScoreRequest, Any?>> = listOf(
  RiskScoreRequest::gender,
  RiskScoreRequest::totalIndecentImageSanctions,
  RiskScoreRequest::totalContactAdultSexualSanctions,
  RiskScoreRequest::totalContactChildSexualSanctions,
  RiskScoreRequest::totalNonContactSexualOffences,
)

fun validateImagesAndIndirectContactSexualReoffendingPredictor(request: RiskScoreRequest): List<ValidationError> {
  val errors = mutableListOf<ValidationError>()
  // TODO: Do validation
  // validateRequiredFields(request, errors, IMAGES_AND_INDIRECT_CONTACT_SEXUAL_REOFFENDING_PREDICTOR_REQUIRED_FIELDS, StaticOrDynamic.STATIC)
  // TODO: Add further validation logic
  return errors
}
