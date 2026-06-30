package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.getAgeAtDate
import kotlin.reflect.KProperty1

@Component
class CommonValidator {

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
    if (request.currentOffenceCode != null && request.currentOffenceCode.length != 5) {
      return ValidationErrorType.OFFENCE_CODE_INCORRECT_FORMAT.asError(listOf(RiskScoreRequest::currentOffenceCode.name))
    }
    // TODO - extra validation once offence code to actuarial category work is done to check
    // - that we have a mapping for that offence code
    // - that the category is not NEED_DETAILS_OF_EXACT_OFFENCE meaning we need the user to use a more specific code
    return null
  }

  fun validateAgeAtFirstSanction(request: RiskScoreRequest): ValidationError? {
    // ageAtFirstSanction must be between 8-98 (inclusive)
    if (request.ageAtFirstSanction != null) {
      if (request.ageAtFirstSanction !in 8..98) {
        return ValidationErrorType.AGE_AT_FIRST_SANCTION_OUT_OF_RANGE.asError(listOf(RiskScoreRequest::ageAtFirstSanction.name))
      }
    }
    return null
  }

  fun validateDateOfCurrentConvictionAgainstDateOfBirth(request: RiskScoreRequest): ValidationError?  {
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
          RiskScoreRequest::dateOfCurrentConviction.name
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
    if (request.dateAtStartOfFollowupCalculated == null && request.dateOfCurrentConviction == null) {
      return ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_REQUIRED.asError(listOf(RiskScoreRequest::dateAtStartOfFollowupCalculated.name))
    }
    return null
  }

  fun validateDateAtStartOfFollowupAgainstDateOfBirth(request: RiskScoreRequest): ValidationError? {
    // dateAtStartOfFollowupCalculated must be after dateOfBirth
    if (request.dateAtStartOfFollowupCalculated != null && request.dateOfBirth != null && request.dateAtStartOfFollowupCalculated <= request.dateOfBirth) {
      return ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_BEFORE_DATE_OF_BIRTH.asError(listOf(RiskScoreRequest::dateAtStartOfFollowupCalculated.name))
    }
    return null
  }

  fun validateDateAtStartOfFollowupAge(request: RiskScoreRequest): ValidationError? {
    if (request.dateAtStartOfFollowupCalculated != null && request.dateOfBirth != null && request.dateAtStartOfFollowupCalculated > request.dateOfBirth) {
      val ageAtStartOfFollowup = getAgeAtDate(
        request.dateOfBirth,
        request.dateAtStartOfFollowupCalculated,
        RiskScoreRequest::dateAtStartOfFollowupCalculated.name
      )
      if (ageAtStartOfFollowup >= 110) {
        return ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_OUT_OF_RANGE.asError(listOf(RiskScoreRequest::dateAtStartOfFollowupCalculated.name))
      }
    }
    return null
  }
}
