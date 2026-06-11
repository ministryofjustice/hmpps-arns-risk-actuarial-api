package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import kotlin.reflect.KProperty1

val ALL_REOFFENDING_PREDICTOR_STATIC_REQUIRED_FIELDS: List<KProperty1<RiskScoreRequest, Any?>> = listOf(
  // TODO: Add required fields
)

val ALL_REOFFENDING_PREDICTOR_DYNAMIC_REQUIRED_FIELDS: List<KProperty1<RiskScoreRequest, Any?>> = listOf(
  // TODO: Add required fields
)

fun validateAllReoffendingPredictorStatic(request: RiskScoreRequest): List<ValidationError> {
  val errors = mutableListOf<ValidationError>()
  // TODO: Ensure ValidationError.type is set to MISSING_MANDATORY_INPUT
  validateRequiredFields(request, errors, ALL_REOFFENDING_PREDICTOR_STATIC_REQUIRED_FIELDS)
  // TODO: Add further validation logic
  return errors
}

fun validateAllReoffendingPredictorDynamic(request: RiskScoreRequest): List<ValidationError> {
  val errors = mutableListOf<ValidationError>()
  // TODO: Ensure ValidationError.type is set to MISSING_DYNAMIC_INPUT
  validateRequiredFields(request, errors, ALL_REOFFENDING_PREDICTOR_DYNAMIC_REQUIRED_FIELDS)
  // TODO: Add further validation logic ensuring ValidationError.type is set to MISSING_DYNAMIC_INPUT
  return errors
}
