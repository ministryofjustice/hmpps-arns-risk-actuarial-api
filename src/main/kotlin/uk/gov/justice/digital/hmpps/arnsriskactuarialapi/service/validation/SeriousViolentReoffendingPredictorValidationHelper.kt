package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import kotlin.reflect.KProperty1

val SERIOUS_VIOLENT_REOFFENDING_PREDICTOR_STATIC_REQUIRED_FIELDS: List<KProperty1<RiskScoreRequest, Any?>> = listOf(
  RiskScoreRequest::assessmentDate,
  RiskScoreRequest::dateOfBirth,
  RiskScoreRequest::dateOfCurrentConviction,
  RiskScoreRequest::ageAtFirstSanction,
  RiskScoreRequest::gender,
  RiskScoreRequest::currentOffenceCode,
  RiskScoreRequest::totalNumberOfSanctionsForAllOffences,
  RiskScoreRequest::totalNumberOfViolentSanctions,
)

val SERIOUS_VIOLENT_REOFFENDING_PREDICTOR_DYNAMIC_REQUIRED_FIELDS: List<KProperty1<RiskScoreRequest, Any?>> = listOf(
  RiskScoreRequest::didOffenceInvolveCarryingOrUsingWeapon,
  RiskScoreRequest::suitabilityOfAccommodation,
  RiskScoreRequest::isUnemployed,
  RiskScoreRequest::currentAlcoholUseProblems,
  RiskScoreRequest::temperControl,
  RiskScoreRequest::proCriminalAttitudes,
  RiskScoreRequest::previousConvictions,
)

fun validateSeriousViolentReoffendingPredictorStatic(request: RiskScoreRequest): List<ValidationError> {
  val errors = mutableListOf<ValidationError>()
  validateRequiredFields(request, errors, SERIOUS_VIOLENT_REOFFENDING_PREDICTOR_STATIC_REQUIRED_FIELDS, StaticOrDynamic.STATIC)
  // TODO: Add further validation logic
  return errors
}

fun validateSeriousViolentReoffendingPredictorDynamic(request: RiskScoreRequest): List<ValidationError> {
  val errors = mutableListOf<ValidationError>()
  validateRequiredFields(request, errors, SERIOUS_VIOLENT_REOFFENDING_PREDICTOR_DYNAMIC_REQUIRED_FIELDS, StaticOrDynamic.DYNAMIC)
  // TODO: Add further validation logic
  return errors
}
