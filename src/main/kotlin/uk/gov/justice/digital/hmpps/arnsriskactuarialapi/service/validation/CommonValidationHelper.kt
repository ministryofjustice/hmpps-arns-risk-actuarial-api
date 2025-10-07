package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import kotlin.reflect.KProperty1

fun ArrayList<String>.addIfNull(request: RiskScoreRequest, prop: KProperty1<RiskScoreRequest, Any?>) {
  if (prop.get(request) == null) this.add(prop.name)
}

fun ArrayList<String>.addIfNotNull(request: RiskScoreRequest, prop: KProperty1<RiskScoreRequest, Any?>) {
  if (prop.get(request) != null) this.add(prop.name)
}

fun validateRequiredFields(request: RiskScoreRequest, errors: MutableList<ValidationErrorResponse>, requiredFields: List<KProperty1<RiskScoreRequest, Any?>>) {
  val missingFields = arrayListOf<String>()

  requiredFields.forEach { missingFields.addIfNull(request, it) }

  if (missingFields.isNotEmpty()) {
    errors += ValidationErrorType.MISSING_MANDATORY_INPUT.asErrorResponse(missingFields)
  }
}

fun validateTotalNumberOfSanctionsForAllOffences(request: RiskScoreRequest, errors: MutableList<ValidationErrorResponse>) {
  if (request.totalNumberOfSanctionsForAllOffences != null && request.totalNumberOfSanctionsForAllOffences < 1) {
    errors += ValidationErrorType.TOTAL_NUMBER_OF_SANCTIONS_LESS_THAN_ONE.asErrorResponse(
      listOf(RiskScoreRequest::totalNumberOfSanctionsForAllOffences.name),
    )
  }
}

fun validateCurrentOffenceCode(request: RiskScoreRequest, errors: MutableList<ValidationErrorResponse>) {
  if (request.currentOffenceCode != null && request.currentOffenceCode.length != 5) {
    errors += ValidationErrorType.OFFENCE_CODE_INCORRECT_FORMAT.asErrorResponse(listOf(RiskScoreRequest::currentOffenceCode.name))
  }
}

fun addMissingFields(
  missingFields: List<String>,
  errors: List<ValidationErrorResponse>,
  isDynamic: Boolean = false
): List<ValidationErrorResponse> = addValidationErrorResponse(
  missingFields,
  errors,
  if (isDynamic) ValidationErrorType.MISSING_DYNAMIC_INPUT else ValidationErrorType.MISSING_INPUT
)

fun addMissingCriteriaValidation(
  criteriaFields: List<String>,
  errors: List<ValidationErrorResponse>,
): List<ValidationErrorResponse> = addValidationErrorResponse(
  criteriaFields,
  errors,
  ValidationErrorType.NOT_APPLICABLE,
)

fun addValidationErrorResponse(
  fields: List<String>,
  errors: List<ValidationErrorResponse>,
  error: ValidationErrorType,
): List<ValidationErrorResponse> = if (fields.isNotEmpty()) {
  errors + error.asErrorResponse(fields)
} else {
  errors
}

fun getNullValuesFromProperties(
  request: RiskScoreRequest,
  properties: List<KProperty1<RiskScoreRequest, Any?>>,
): List<String> = properties.fold(arrayListOf()) { acc, property ->
  acc.apply { property.get(request) ?: acc.add(property.name) }
}
