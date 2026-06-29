package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import kotlin.reflect.KProperty1

val GENERAL_REQUIRED_FIELDS: List<KProperty1<RiskScoreRequest, Any?>> =
  listOf(
    RiskScoreRequest::gender,
    RiskScoreRequest::hasEverCommittedSexualOffence,
  )

val IMAGES_AND_INDIRECT_CONTACT_SEXUAL_REOFFENDING_PREDICTOR_REQUIRED_FIELDS: List<KProperty1<RiskScoreRequest, Any?>> =
  listOf(
    RiskScoreRequest::totalIndecentImageSanctions,
    RiskScoreRequest::totalContactAdultSexualSanctions,
    RiskScoreRequest::totalContactChildSexualSanctions,
    RiskScoreRequest::totalNonContactSexualOffences,
  )

fun validateImagesAndIndirectContactSexualReoffendingPredictor(request: RiskScoreRequest): List<ValidationError> {
  val errors = mutableListOf<ValidationError>()
  validateRequiredFields(
    request,
    errors,
    GENERAL_REQUIRED_FIELDS,
    StaticOrDynamic.STATIC,
  )

  when (request.hasEverCommittedSexualOffence) {
    true -> validateSanctions(request, errors)
    false -> checkForExistingFields(request, errors)
    else -> null
  }

  return errors
}

private fun validateSanctions(
  request: RiskScoreRequest,
  errors: MutableList<ValidationError>,
) {
  validateRequiredFields(
    request,
    errors,
    IMAGES_AND_INDIRECT_CONTACT_SEXUAL_REOFFENDING_PREDICTOR_REQUIRED_FIELDS,
    StaticOrDynamic.STATIC,
  )

  if (request.totalIndecentImageSanctions == 0 && request.totalContactAdultSexualSanctions == 0 && request.totalContactChildSexualSanctions == 0 && request.totalNonContactSexualOffences == 0) {
    errors += ValidationErrorType.IMAGES_AND_INDIRECT_CONTACT_SEXUAL_REOFFENDING_PREDICTOR_NO_SANCTIONS.asError(
      IMAGES_AND_INDIRECT_CONTACT_SEXUAL_REOFFENDING_PREDICTOR_REQUIRED_FIELDS.names(),
    )
  }
}

private fun checkForExistingFields(request: RiskScoreRequest, errors: MutableList<ValidationError>) {
  val existingFields = arrayListOf<String>()

  IMAGES_AND_INDIRECT_CONTACT_SEXUAL_REOFFENDING_PREDICTOR_REQUIRED_FIELDS.forEach {
    existingFields.addIfNotNullAndZero(
      request,
      it,
    )
  }

  if (existingFields.isNotEmpty()) {
    existingFields.addFirst(RiskScoreRequest::hasEverCommittedSexualOffence.name)
    errors += ValidationErrorType.AMBIGUOUS_INPUT.asError(existingFields)
  }
}
