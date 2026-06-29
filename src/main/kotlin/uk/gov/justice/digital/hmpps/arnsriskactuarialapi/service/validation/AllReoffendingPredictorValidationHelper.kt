package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.getAgeAtDate
import kotlin.reflect.KProperty1

val ALL_REOFFENDING_PREDICTOR_STATIC_REQUIRED_FIELDS: List<KProperty1<RiskScoreRequest, Any?>> = listOf(
  RiskScoreRequest::assessmentDate,
  RiskScoreRequest::dateOfBirth,
  RiskScoreRequest::dateOfCurrentConviction,
  RiskScoreRequest::ageAtFirstSanction,
  RiskScoreRequest::gender,
  RiskScoreRequest::currentOffenceCode,
  RiskScoreRequest::totalNumberOfSanctionsForAllOffences,
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
  validateDateOfCurrentConvictionAgainstDateOfBirth(request, errors)
  validateDateOfCurrentConvictionAgainstAgeAtFirstSanction(request, errors)
  validateDateOfCurrentConvictionAgainstAssessmentDate(request, errors)
  validateAgeAtFirstSanction(request, errors)
  validateCurrentOffenceCode(request, errors)
  validateTotalNumberOfSanctionsForAllOffences(request, errors)
  validateDateAtStartOfFollowupAgainstDateOfCurrentConviction(request, errors)
  validateDateAtStartOfFollowupAgainstDateOfBirth(request, errors)
  validateDateAtStartOfFollowupAge(request, errors)
  return errors
}

fun validateAllReoffendingPredictorDynamic(request: RiskScoreRequest): List<ValidationError> {
  val errors = mutableListOf<ValidationError>()
  validateRequiredFields(request, errors, ALL_REOFFENDING_PREDICTOR_DYNAMIC_REQUIRED_FIELDS, StaticOrDynamic.DYNAMIC)
  // TODO: Add further validation logic
  return errors
}

fun validateDateAtStartOfFollowupAgainstDateOfCurrentConviction(request: RiskScoreRequest, errors: MutableList<ValidationError>) {
  // One of dateAtStartOfFollowup and dateOfCurrentConviction must be provided
  // This is not strictly needed but adding to ensure consistency with OASys
  if (request.dateAtStartOfFollowupCalculated == null && request.dateOfCurrentConviction == null) {
    errors.add(ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_REQUIRED.asError(listOf(RiskScoreRequest::dateAtStartOfFollowupCalculated.name)))
  }
}

fun validateDateAtStartOfFollowupAgainstDateOfBirth(request: RiskScoreRequest, errors: MutableList<ValidationError>) {
  // dateAtStartOfFollowupCalculated must be after dateOfDate
  if (request.dateAtStartOfFollowupCalculated != null && request.dateOfBirth != null && request.dateAtStartOfFollowupCalculated <= request.dateOfBirth) {
    errors.add(ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_BEFORE_DATE_OF_BIRTH.asError(listOf(RiskScoreRequest::dateAtStartOfFollowupCalculated.name)))
  }
}

fun validateDateAtStartOfFollowupAge(request: RiskScoreRequest, errors: MutableList<ValidationError>) {
  if (request.dateAtStartOfFollowupCalculated != null && request.dateOfBirth != null) {
    val ageAtStartOfFollowup = getAgeAtDate(request.dateOfBirth, request.dateAtStartOfFollowupCalculated, RiskScoreRequest::dateAtStartOfFollowupCalculated.name)
    if (ageAtStartOfFollowup >= 110) {
      errors.add(ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_OUT_OF_RANGE.asError(listOf(RiskScoreRequest::dateAtStartOfFollowupCalculated.name)))
    }
  }
}
