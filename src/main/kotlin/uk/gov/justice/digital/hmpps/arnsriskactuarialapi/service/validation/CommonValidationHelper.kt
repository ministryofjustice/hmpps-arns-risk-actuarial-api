package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.getAgeAtDate
import kotlin.reflect.KProperty1

fun ArrayList<String>.addIfNull(request: RiskScoreRequest, prop: KProperty1<RiskScoreRequest, Any?>) {
  if (prop.get(request) == null) this.add(prop.name)
}

fun ArrayList<String>.addIfNotNull(request: RiskScoreRequest, prop: KProperty1<RiskScoreRequest, Any?>) {
  if (prop.get(request) != null) this.add(prop.name)
}

fun List<KProperty1<RiskScoreRequest, Any?>>.names(): List<String> = this.map { it.name }

fun validateRequiredFields(
  request: RiskScoreRequest,
  errors: MutableList<ValidationError>,
  requiredFields: List<KProperty1<RiskScoreRequest, Any?>>,
  staticOrDynamic: StaticOrDynamic = StaticOrDynamic.STATIC,
) {
  val missingFields = arrayListOf<String>()

  requiredFields.forEach { missingFields.addIfNull(request, it) }

  if (missingFields.isNotEmpty()) {
    errors += when (staticOrDynamic) {
      StaticOrDynamic.STATIC -> ValidationErrorType.MISSING_MANDATORY_INPUT.asError(missingFields)
      StaticOrDynamic.DYNAMIC -> ValidationErrorType.MISSING_DYNAMIC_INPUT.asError(missingFields)
    }
  }
}

fun validateTotalNumberOfSanctionsForAllOffences(request: RiskScoreRequest, errors: MutableList<ValidationError>) {
  if (request.totalNumberOfSanctionsForAllOffences != null && request.totalNumberOfSanctionsForAllOffences !in 1..999) {
    errors += ValidationErrorType.TOTAL_NUMBER_OF_SANCTIONS_OUT_OF_RANGE.asError(
      listOf(RiskScoreRequest::totalNumberOfSanctionsForAllOffences.name),
    )
  }
}

fun validateTotalNumberOfViolentSanctions(
  request: RiskScoreRequest,
  errors: MutableList<ValidationError>,
) {
  if (request.totalNumberOfViolentSanctions != null &&
    request.totalNumberOfSanctionsForAllOffences != null &&
    (request.totalNumberOfViolentSanctions < 0 || request.totalNumberOfViolentSanctions > request.totalNumberOfSanctionsForAllOffences)
  ) {
    errors += ValidationErrorType.VIOLENT_SANCTION_OUT_OF_RANGE.asError(
      listOf(
        RiskScoreRequest::totalNumberOfViolentSanctions.name,
        RiskScoreRequest::totalNumberOfSanctionsForAllOffences.name,
      ),
    )
  }
}

fun validateCurrentOffenceCode(request: RiskScoreRequest, errors: MutableList<ValidationError>) {
  if (request.currentOffenceCode != null && request.currentOffenceCode.length != 5) {
    errors += ValidationErrorType.OFFENCE_CODE_INCORRECT_FORMAT.asError(listOf(RiskScoreRequest::currentOffenceCode.name))
  }
}

fun validateAgeAtFirstSanction(request: RiskScoreRequest, errors: MutableList<ValidationError>) {
  // ageAtFirstSanction must be between 8-98 (inclusive)
  if (request.ageAtFirstSanction != null) {
    if (request.ageAtFirstSanction !in 8..98) {
      errors.add(ValidationErrorType.AGE_AT_FIRST_SANCTION_OUT_OF_RANGE.asError(listOf(RiskScoreRequest::ageAtFirstSanction.name)))
    }
  }
}

fun validateDateOfCurrentConvictionAgainstDateOfBirth(request: RiskScoreRequest, errors: MutableList<ValidationError>) {
  // dateOfCurrentConviction must be after dateOfBirth
  if (request.dateOfCurrentConviction != null && request.dateOfBirth != null && request.dateOfCurrentConviction <= request.dateOfBirth) {
    errors.add(ValidationErrorType.DATE_OF_CURRENT_CONVICTION_BEFORE_DATE_OF_BIRTH.asError(listOf(RiskScoreRequest::dateOfCurrentConviction.name)))
  }
}

fun validateDateOfCurrentConvictionAgainstAgeAtFirstSanction(
  request: RiskScoreRequest,
  errors: MutableList<ValidationError>,
) {
  // dateOfCurrentConviction must be after ageAtFirstSanction
  if (request.dateOfCurrentConviction != null && request.ageAtFirstSanction != null && request.dateOfBirth != null) {
    val ageAtCurrentConviction =
      getAgeAtDate(request.dateOfBirth, request.dateOfCurrentConviction, RiskScoreRequest::dateOfCurrentConviction.name)
    if (ageAtCurrentConviction < request.ageAtFirstSanction) {
      errors.add(
        ValidationErrorType.AGE_AT_FIRST_SANCTION_AFTER_AGE_AT_CURRENT_CONVICTION.asError(
          listOf(
            RiskScoreRequest::dateOfCurrentConviction.name,
            RiskScoreRequest::ageAtFirstSanction.name,
          ),
        ),
      )
    }
  }
}

fun validateDateOfCurrentConvictionAgainstAssessmentDate(
  request: RiskScoreRequest,
  errors: MutableList<ValidationError>,
) {
  // dateOfCurrentConviction must be less than 3 months after assessmentDate
  if (request.dateOfCurrentConviction != null &&
    request.dateOfCurrentConviction.isAfter(
      request.assessmentDate.plusMonths(
        3,
      ),
    )
  ) {
    errors.add(
      ValidationErrorType.DATE_OF_CURRENT_CONVICTION_WITHIN_THREE_MONTHS_OF_ASSESSMENT_DATE.asError(
        listOf(
          RiskScoreRequest::dateOfCurrentConviction.name,
          RiskScoreRequest::assessmentDate.name,
        ),
      ),
    )
  }
}

fun addMissingFields(
  missingFields: List<String>,
  errors: List<ValidationError>,
  isDynamic: Boolean = false,
): List<ValidationError> = addValidationErrorResponse(
  missingFields,
  errors,
  if (isDynamic) ValidationErrorType.MISSING_DYNAMIC_INPUT else ValidationErrorType.MISSING_MANDATORY_INPUT,
)

fun addValidationErrorResponse(
  fields: List<String>,
  errors: List<ValidationError>,
  error: ValidationErrorType,
): List<ValidationError> = if (fields.isNotEmpty()) {
  errors + error.asError(fields)
} else {
  errors
}

fun getNullValuesFromProperties(
  request: RiskScoreRequest,
  properties: List<KProperty1<RiskScoreRequest, Any?>>,
): List<String> = properties.fold(arrayListOf()) { acc, property ->
  acc.apply { property.get(request) ?: acc.add(property.name) }
}
