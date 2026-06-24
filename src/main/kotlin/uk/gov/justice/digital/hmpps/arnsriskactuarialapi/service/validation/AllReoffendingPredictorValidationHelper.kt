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
  validateDateOfCurrentConvictionAgainstAgeOfFirstSanction(request, errors)
  validateDateOfCurrentConvictionAgainstAssessmentDate(request, errors)
  validateAgeAtFirstSanction(request, errors)
  validateCurrentOffenceCode(request, errors)
  validateTotalNumberOfSanctionsForAllOffences(request, errors)
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

fun validateDateOfCurrentConvictionAgainstDateOfBirth(request: RiskScoreRequest, errors: MutableList<ValidationError>) {
  // dateOfCurrentConviction must be after dateOfDate
  if (request.dateOfCurrentConviction != null && request.dateOfBirth != null && request.dateOfCurrentConviction <= request.dateOfBirth) {
    errors.add(ValidationErrorType.DATE_OF_CURRENT_CONVICTION_BEFORE_DATE_OF_BIRTH.asError(listOf(RiskScoreRequest::dateOfCurrentConviction.name)))
  }
}

fun validateDateOfCurrentConvictionAgainstAgeOfFirstSanction(request: RiskScoreRequest, errors: MutableList<ValidationError>) {
  // dateOfCurrentConviction must be after ageAtFirstSanction
  if (request.dateOfCurrentConviction != null && request.ageAtFirstSanction != null && request.dateOfBirth != null) {
    val ageAtCurrentConviction =
      getAgeAtDate(request.dateOfBirth, request.dateOfCurrentConviction, RiskScoreRequest::dateOfCurrentConviction.name)
    if (ageAtCurrentConviction < request.ageAtFirstSanction) {
      errors.add(
        ValidationErrorType.AGE_AT_FIRST_SANCTION_AFTER_AGE_AT_CURRENT_CONVICTION.asError(
          listOf(
            RiskScoreRequest::dateOfCurrentConviction.name,
            RiskScoreRequest::ageAtFirstSanction.name
          )
        )
      )
    }
  }
}

fun validateDateOfCurrentConvictionAgainstAssessmentDate(request: RiskScoreRequest, errors: MutableList<ValidationError>) {
  // dateOfCurrentConviction must be no more than 3 months after the assessment date (i.e. 3 months in the future)
  if (request.dateOfCurrentConviction != null) {
    if (request.dateOfCurrentConviction.minusMonths(3).isAfter(request.assessmentDate)) {
      errors.add(ValidationErrorType.DATE_OF_CURRENT_CONVICTION_TOO_FAR_IN_FUTURE.asError(listOf(RiskScoreRequest::dateOfCurrentConviction.name)))
    }
  }
}

fun validateAgeAtFirstSanction(request: RiskScoreRequest, errors: MutableList<ValidationError>) {
  // ageAtFirstSanction must be between 8-98 (inclusive)
  if (request.ageAtFirstSanction != null) {
    if (request.ageAtFirstSanction !in 8..98) {
      errors.add(ValidationErrorType.AGE_AT_CURRENT_CONVICTION_OUT_OF_RANGE.asError(listOf(RiskScoreRequest::ageAtFirstSanction.name)))
    }
  }
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
