package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import kotlin.reflect.KProperty1

val ALL_REOFFENDING_PREDICTOR_STATIC_REQUIRED_FIELDS: List<KProperty1<RiskScoreRequest, Any?>> = listOf(
  RiskScoreRequest::assessmentDate,
  RiskScoreRequest::dateOfBirth,
  RiskScoreRequest::dateOfCurrentConviction,
  RiskScoreRequest::ageAtFirstSanction,
  RiskScoreRequest::gender,
  RiskScoreRequest::currentOffenceCode,
  RiskScoreRequest::totalNumberOfSanctionsForAllOffences,
  RiskScoreRequest::dateAtStartOfFollowupCalculated,
)

val ALL_REOFFENDING_PREDICTOR_DYNAMIC_REQUIRED_FIELDS: List<KProperty1<RiskScoreRequest, Any?>> = listOf(
  RiskScoreRequest::suitabilityOfAccommodation,
  RiskScoreRequest::isUnemployed,
  RiskScoreRequest::currentRelationshipWithPartner,
  RiskScoreRequest::evidenceOfDomesticAbuse,
  RiskScoreRequest::currentRelationshipStatus,
  RiskScoreRequest::regularOffendingActivities,
  RiskScoreRequest::motivationToTackleDrugMisuse,
  RiskScoreRequest::hasHeroinUsage,
  RiskScoreRequest::hasOtherOpiateUsage,
  RiskScoreRequest::hasCrackCocaineUsage,
  RiskScoreRequest::hasPowderCocaineUsage,
  RiskScoreRequest::hasMisusedPrescriptionDrugUsage,
  RiskScoreRequest::hasBenzodiazepinesUsage,
  RiskScoreRequest::hasCannabisUsage,
  RiskScoreRequest::hasSteroidsUsage,
  RiskScoreRequest::hasOtherDrugsUsage,
  RiskScoreRequest::hasKetamineUsage,
  RiskScoreRequest::hasSpiceUsage,
  RiskScoreRequest::hasHallucinogensUsage,
  RiskScoreRequest::hasSolventsUsage,
  RiskScoreRequest::currentAlcoholUseProblems,
  RiskScoreRequest::excessiveAlcoholUse,
  RiskScoreRequest::impulsivityProblems,
  RiskScoreRequest::proCriminalAttitudes,
)

fun validateAllReoffendingPredictorStatic(request: RiskScoreRequest): List<ValidationError> {
  val errors = mutableListOf<ValidationError>()
  validateRequiredFields(request, errors, ALL_REOFFENDING_PREDICTOR_STATIC_REQUIRED_FIELDS, StaticOrDynamic.STATIC)
  // TODO: Add further validation logic
  return errors
}

fun validateAllReoffendingPredictorDynamic(request: RiskScoreRequest): List<ValidationError> {
  val errors = mutableListOf<ValidationError>()
  validateRequiredFields(request, errors, ALL_REOFFENDING_PREDICTOR_DYNAMIC_REQUIRED_FIELDS, StaticOrDynamic.DYNAMIC)
  // TODO: Add further validation logic
  return errors
}
