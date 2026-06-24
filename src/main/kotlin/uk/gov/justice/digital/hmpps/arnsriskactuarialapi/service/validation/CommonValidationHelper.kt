package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import kotlin.reflect.KProperty1

fun ArrayList<String>.addIfNull(request: RiskScoreRequest, prop: KProperty1<RiskScoreRequest, Any?>) {
  if (prop.get(request) == null) this.add(prop.name)
}

fun ArrayList<String>.addIfNotNull(request: RiskScoreRequest, prop: KProperty1<RiskScoreRequest, Any?>) {
  if (prop.get(request) != null) this.add(prop.name)
}

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

fun validateCurrentOffenceCode(request: RiskScoreRequest, errors: MutableList<ValidationError>) {
  if (request.currentOffenceCode != null && request.currentOffenceCode.length != 5) {
    errors += ValidationErrorType.OFFENCE_CODE_INCORRECT_FORMAT.asError(listOf(RiskScoreRequest::currentOffenceCode.name))
  }
  // TODO - extra validation once offence code to actuarial category work is done to check
  // - that we have a mapping for that offence code
  // - that the category is not NEED_DETAILS_OF_EXACT_OFFENCE meaning we need the user to use a more specific code
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
