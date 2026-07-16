package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.ActuarialCategory
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.OffenceCodeCacheService
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.getAgeAtDate
import kotlin.reflect.KProperty1

@Component
class CommonValidator(val offenceCodeCacheService: OffenceCodeCacheService) {

  val sexualPredictorsSecondaryFields = listOf(
    RiskScoreRequest::totalIndecentImageSanctions,
    RiskScoreRequest::totalContactAdultSexualSanctions,
    RiskScoreRequest::totalContactChildSexualSanctions,
    RiskScoreRequest::totalNonContactSexualOffences,
  )

  fun validateRequiredFields(
    request: RiskScoreRequest,
    requiredFields: List<KProperty1<RiskScoreRequest, Any?>>,
    staticOrDynamic: StaticOrDynamic = StaticOrDynamic.STATIC,
  ): ValidationError? {
    val missingFields = arrayListOf<String>()

    requiredFields.forEach { missingFields.addIfNull(request, it) }

    return if (missingFields.isNotEmpty()) {
      when (staticOrDynamic) {
        StaticOrDynamic.STATIC -> ValidationErrorType.MISSING_MANDATORY_INPUT.asError(missingFields)
        StaticOrDynamic.DYNAMIC -> ValidationErrorType.MISSING_DYNAMIC_INPUT.asError(missingFields)
      }
    } else {
      null
    }
  }

  fun validateTotalNumberOfSanctionsForAllOffences(request: RiskScoreRequest): ValidationError? {
    if (request.totalNumberOfSanctionsForAllOffences != null && request.totalNumberOfSanctionsForAllOffences !in 1..999) {
      return ValidationErrorType.TOTAL_NUMBER_OF_SANCTIONS_OUT_OF_RANGE.asError(
        listOf(RiskScoreRequest::totalNumberOfSanctionsForAllOffences.name),
      )
    }
    return null
  }

  fun validateTotalNumberOfViolentSanctions(request: RiskScoreRequest): ValidationError? {
    if (request.totalNumberOfViolentSanctions != null &&
      request.totalNumberOfSanctionsForAllOffences != null &&
      (request.totalNumberOfViolentSanctions < 0 || request.totalNumberOfViolentSanctions > request.totalNumberOfSanctionsForAllOffences)
    ) {
      return ValidationErrorType.VIOLENT_SANCTION_OUT_OF_RANGE.asError(
        listOf(
          RiskScoreRequest::totalNumberOfViolentSanctions.name,
          RiskScoreRequest::totalNumberOfSanctionsForAllOffences.name,
        ),
      )
    }
    return null
  }

  fun validateCurrentOffenceCode(request: RiskScoreRequest): ValidationError? {
    if (request.currentOffenceCode != null) {
      if (!request.currentOffenceCode.matches(Regex("^\\d{5}$"))) {
        return ValidationErrorType.OFFENCE_CODE_INCORRECT_FORMAT.asError(listOf(RiskScoreRequest::currentOffenceCode.name))
      }
      val actuarialCategory = offenceCodeCacheService.getActuarialCategory(request.currentOffenceCode)
      return when (actuarialCategory) {
        null, ActuarialCategory.UNKNOWN -> ValidationErrorType.OFFENCE_CODE_MAPPING_NOT_FOUND.asError(listOf(RiskScoreRequest::currentOffenceCode.name))
        ActuarialCategory.NEED_DETAILS_OF_EXACT_OFFENCE -> ValidationErrorType.NEED_DETAILS_OF_EXACT_OFFENCE.asError(listOf(RiskScoreRequest::currentOffenceCode.name))
        else -> null
      }
    }
    return null
  }

  fun validateAgeAtFirstSanction(request: RiskScoreRequest): ValidationError? {
    // ageAtFirstSanction must be between 8-98 (inclusive)
    if (request.ageAtFirstSanction != null) {
      if (request.ageAtFirstSanction !in 0..1000) {
        return ValidationErrorType.AGE_AT_FIRST_SANCTION_OUT_OF_RANGE.asError(listOf(RiskScoreRequest::ageAtFirstSanction.name))
      }
    }
    return null
  }

  fun validateDateOfCurrentConvictionAgainstDateOfBirth(request: RiskScoreRequest): ValidationError? {
    // dateOfCurrentConviction must be after dateOfBirth
    if (request.dateOfCurrentConviction != null && request.dateOfBirth != null && request.dateOfCurrentConviction <= request.dateOfBirth) {
      return ValidationErrorType.DATE_OF_CURRENT_CONVICTION_BEFORE_DATE_OF_BIRTH.asError(listOf(RiskScoreRequest::dateOfCurrentConviction.name))
    }
    return null
  }

  fun validateDateOfCurrentConvictionAgainstAgeAtFirstSanction(request: RiskScoreRequest): ValidationError? {
    // dateOfCurrentConviction must be after ageAtFirstSanction
    // if dateOfCurrentConviction is before dateOfBirth, we should skip this validation step - it should already have been raised
    if (request.dateOfCurrentConviction != null && request.ageAtFirstSanction != null && request.dateOfBirth != null && request.dateOfCurrentConviction > request.dateOfBirth) {
      val ageAtCurrentConviction =
        getAgeAtDate(
          request.dateOfBirth,
          request.dateOfCurrentConviction,
          RiskScoreRequest::dateOfCurrentConviction.name,
        )
      if (ageAtCurrentConviction < request.ageAtFirstSanction) {
        return ValidationErrorType.AGE_AT_FIRST_SANCTION_AFTER_AGE_AT_CURRENT_CONVICTION.asError(
          listOf(
            RiskScoreRequest::dateOfCurrentConviction.name,
            RiskScoreRequest::ageAtFirstSanction.name,
          ),
        )
      }
    }
    return null
  }

  fun validateDateOfCurrentConvictionAgainstAssessmentDate(request: RiskScoreRequest): ValidationError? {
    // dateOfCurrentConviction must be less than 3 months after assessmentDate
    if (request.dateOfCurrentConviction != null &&
      request.dateOfCurrentConviction.isAfter(
        request.assessmentDate.plusMonths(
          3,
        ),
      )
    ) {
      return ValidationErrorType.DATE_OF_CURRENT_CONVICTION_WITHIN_THREE_MONTHS_OF_ASSESSMENT_DATE.asError(
        listOf(
          RiskScoreRequest::dateOfCurrentConviction.name,
          RiskScoreRequest::assessmentDate.name,
        ),
      )
    }
    return null
  }

  fun validateDateAtStartOfFollowupAgainstDateOfCurrentConviction(request: RiskScoreRequest): ValidationError? {
    // One of dateAtStartOfFollowup and dateOfCurrentConviction must be provided
    // This is not strictly needed but adding to ensure consistency with OASys
    if (request.dateAtStartOfFollowup == null && request.dateOfCurrentConviction == null) {
      return ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_REQUIRED.asError(listOf(RiskScoreRequest::dateAtStartOfFollowup.name))
    }
    return null
  }

  fun validateDateAtStartOfFollowupAgainstDateOfBirth(request: RiskScoreRequest): ValidationError? {
    // dateAtStartOfFollowup must be after dateOfBirth
    if (request.dateAtStartOfFollowup != null && request.dateOfBirth != null && request.dateAtStartOfFollowup <= request.dateOfBirth) {
      return ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_BEFORE_DATE_OF_BIRTH.asError(listOf(RiskScoreRequest::dateAtStartOfFollowup.name))
    }
    return null
  }

  fun validateDateAtStartOfFollowupAge(request: RiskScoreRequest): ValidationError? {
    if (request.dateAtStartOfFollowup != null && request.dateOfBirth != null && request.dateAtStartOfFollowup > request.dateOfBirth) {
      val ageAtStartOfFollowup = getAgeAtDate(
        request.dateOfBirth,
        request.dateAtStartOfFollowup,
        RiskScoreRequest::dateAtStartOfFollowup.name,
      )
      if (ageAtStartOfFollowup >= 110) {
        return ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_OUT_OF_RANGE.asError(listOf(RiskScoreRequest::dateAtStartOfFollowup.name))
      }
    }
    return null
  }

  fun validateDateAtStartOfFollowupAgeForSexualPredictor(request: RiskScoreRequest): ValidationError? {
    if (request.gender == Gender.FEMALE || request.hasEverCommittedSexualOffence == false) return null

    val dateOfBirth = request.dateOfBirth ?: return null
    val dateAtStartOfFollowup = request.dateAtStartOfFollowup ?: return null

    if (dateAtStartOfFollowup > dateOfBirth) {
      val ageAtStartOfFollowup = getAgeAtDate(dateOfBirth, dateAtStartOfFollowup, RiskScoreRequest::dateAtStartOfFollowup.name)
      if (ageAtStartOfFollowup >= 110) {
        return ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_OUT_OF_RANGE.asError(listOf(RiskScoreRequest::dateAtStartOfFollowup.name))
      }
    }

    return null
  }

  fun validateTotalNumberOfSanctionsForAllOffencesForSexualPredictor(request: RiskScoreRequest): ValidationError? {
    if (request.gender == Gender.FEMALE || request.hasEverCommittedSexualOffence == false) return null

    if (request.totalNumberOfSanctionsForAllOffences != null && request.totalNumberOfSanctionsForAllOffences !in 1..999) {
      return ValidationErrorType.TOTAL_NUMBER_OF_SANCTIONS_OUT_OF_RANGE.asError(
        listOf(RiskScoreRequest::totalNumberOfSanctionsForAllOffences.name),
      )
    }
    return null
  }

  fun validateDateOfMostRecentSexualOffenceAgainstDateOfBirth(request: RiskScoreRequest): ValidationError? = if (request.gender == Gender.MALE && request.hasEverCommittedSexualOffence == true) {
    val fieldName = RiskScoreRequest::dateOfMostRecentSexualOffence.name
    if (request.dateOfMostRecentSexualOffence == null) {
      return ValidationErrorType.MISSING_MANDATORY_INPUT.asError(listOf(fieldName))
    }

    val dateOfBirth = request.dateOfBirth ?: return null
    val dateOfMostRecentSexualOffence = request.dateOfMostRecentSexualOffence

    if (dateOfMostRecentSexualOffence <= dateOfBirth) {
      return ValidationErrorType.DATE_OF_MOST_RECENT_SEXUAL_OFFENCE_BEFORE_DATE_OF_BIRTH.asError(listOf(fieldName))
    }

    val ageAtDateOfMostRecentSexualOffence = getAgeAtDate(dateOfBirth, dateOfMostRecentSexualOffence, fieldName)
    if (ageAtDateOfMostRecentSexualOffence >= 110) {
      return ValidationErrorType.DATE_OF_MOST_RECENT_SEXUAL_OFFENCE_OUT_OF_RANGE.asError(listOf(fieldName))
    }
    null
  } else {
    null
  }

  fun validateSecondarySexualFields(
    request: RiskScoreRequest,
  ): ValidationError? {
    if (request.gender == Gender.FEMALE || request.hasEverCommittedSexualOffence == false) return null

    return validateRequiredFields(request, sexualPredictorsSecondaryFields)
  }

  fun validateSexualSanctionsCount(
    request: RiskScoreRequest,
  ): ValidationError? {
    if (request.gender == Gender.FEMALE || request.hasEverCommittedSexualOffence == false) return null

    val fieldNames = sexualPredictorsSecondaryFields.names()

    if (request.totalIndecentImageSanctions == 0 && request.totalContactAdultSexualSanctions == 0 && request.totalContactChildSexualSanctions == 0 && request.totalNonContactSexualOffences == 0) {
      return ValidationErrorType.SEXUAL_REOFFENDING_PREDICTOR_NO_SANCTIONS.asError(fieldNames)
    }

    if (request.totalNumberOfSanctionsForAllOffences != null && sexualPredictorsSecondaryFields.sumIntValues(request) > request.totalNumberOfSanctionsForAllOffences) {
      return ValidationErrorType.TOTAL_NUMBER_OF_SEXUAL_SANCTIONS_OUT_OF_RANGE.asError(fieldNames)
    }

    return null
  }

  fun checkForExistingSexualFields(
    request: RiskScoreRequest,
  ): ValidationError? {
    if (request.gender == Gender.FEMALE || request.hasEverCommittedSexualOffence == true) return null

    val existingFields = arrayListOf<String>()
    sexualPredictorsSecondaryFields.forEach {
      existingFields.addIfNotNullAndNotZero(
        request,
        it,
      )
    }

    return if (existingFields.isNotEmpty()) {
      existingFields.addFirst(RiskScoreRequest::hasEverCommittedSexualOffence.name)
      ValidationErrorType.SEXUAL_REOFFENDING_PREDICTOR_INCONSISTENT_INPUT.asError(existingFields)
    } else {
      null
    }
  }

  fun validateDrugMisuse(
    request: RiskScoreRequest,
    drugUsageQuestions: List<KProperty1<RiskScoreRequest, Boolean?>>,
  ): ValidationError? {
    val drugUsageAnswers = drugUsageQuestions.associateWith { it.get(request) }

    // If motivationToTackleDrugMisuse is null, then all the drug usage questions should be set to null or false
    if (request.motivationToTackleDrugMisuse == null) {
      val notNullFields = drugUsageAnswers.getTrueKeys()
      if (notNullFields.isNotEmpty()) {
        return ValidationErrorType.MOTIVATION_TO_TACKLE_DRUG_MISUSE_INCONSISTENT.asError(notNullFields)
      }
    }
    return null
  }
}
