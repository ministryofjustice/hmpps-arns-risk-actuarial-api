package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import kotlin.reflect.KProperty1

val IMAGES_AND_INDIRECT_CONTACT_SEXUAL_REOFFENDING_PREDICTOR_REQUIRED_FIELDS: List<KProperty1<RiskScoreRequest, Any?>> =
  listOf(
    RiskScoreRequest::totalIndecentImageSanctions,
    RiskScoreRequest::totalContactAdultSexualSanctions,
    RiskScoreRequest::totalContactChildSexualSanctions,
    RiskScoreRequest::totalNonContactSexualOffences,
  )

fun validateImagesAndIndirectContactSexualReoffendingPredictor(request: RiskScoreRequest): List<ValidationError> {
  val errors = mutableListOf<ValidationError>()
  if (request.gender == null) {
    errors += ValidationErrorType.MISSING_MANDATORY_INPUT.asError(listOf(RiskScoreRequest::gender.name))
  }

  if (request.hasEverCommittedSexualOffence == true) {
    validateRequiredFields(
      request,
      errors,
      IMAGES_AND_INDIRECT_CONTACT_SEXUAL_REOFFENDING_PREDICTOR_REQUIRED_FIELDS,
      StaticOrDynamic.STATIC,
    )
  } else {
    val existingFields = arrayListOf<String>()

    IMAGES_AND_INDIRECT_CONTACT_SEXUAL_REOFFENDING_PREDICTOR_REQUIRED_FIELDS.forEach {
      existingFields.addIfNotNull(
        request,
        it,
      )
    }

    if (existingFields.isNotEmpty()) {
      existingFields.addFirst(RiskScoreRequest::hasEverCommittedSexualOffence.name)
      errors += ValidationErrorType.AMBIGUOUS_INPUT.asError(existingFields)
    }
  }

  return errors
}
