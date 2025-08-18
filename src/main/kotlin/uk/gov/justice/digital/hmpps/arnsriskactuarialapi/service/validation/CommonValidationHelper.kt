package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import java.util.Objects.isNull
import kotlin.reflect.KProperty1

private const val MIN_CONVICTION_AGE = 10

fun ArrayList<String>.addIfNull(request: RiskScoreRequest, prop: KProperty1<RiskScoreRequest, Any?>) {
  if (prop.get(request) == null) this.add(prop.name)
}

fun getTotalNumberOfSanctionsForAllOffencesValidation(
  totalNumberOfSanctionsForAllOffences: Integer?,
  errors: List<ValidationErrorResponse>,
): List<ValidationErrorResponse> {
  if (totalNumberOfSanctionsForAllOffences != null && totalNumberOfSanctionsForAllOffences < 1) {
    return errors +
      ValidationErrorType.BELOW_MIN_VALUE.asErrorResponse(listOf(RiskScoreRequest::totalNumberOfSanctionsForAllOffences.name))
  }

  return errors
}

fun getCurrentOffenceCodeValidation(
  currentOffenceCode: String?,
  errors: List<ValidationErrorResponse>,
): List<ValidationErrorResponse> {
  if (currentOffenceCode != null && currentOffenceCode.length != 5) {
    return errors +
      ValidationErrorType.NO_MATCHING_INPUT.asErrorResponse(listOf(RiskScoreRequest::currentOffenceCode.name))
  }
  return errors
}

fun validateAge(
  ageAtCurrentConviction: Int,
  ageAtFirstSanction: Int,
  errors: List<ValidationErrorResponse>,
): List<ValidationErrorResponse> = errors + listOfNotNull(
  validateAgeAtCurrentConviction(ageAtCurrentConviction),
  validateAgeAtFirstSanction(ageAtCurrentConviction, ageAtFirstSanction),
)

private fun validateAgeAtCurrentConviction(
  ageAtCurrentConviction: Int,
): ValidationErrorResponse? = if (ageAtCurrentConviction < MIN_CONVICTION_AGE) {
  ValidationErrorType.BELOW_MIN_VALUE.asErrorResponse(listOf(RiskScoreRequest::dateOfBirth.name))
} else {
  null
}

private fun validateAgeAtFirstSanction(
  ageAtCurrentConviction: Int,
  ageAtFirstSanction: Int,
): ValidationErrorResponse? = if (ageAtFirstSanction > ageAtCurrentConviction) {
  ValidationErrorType.BELOW_MIN_VALUE.asErrorResponse(
    listOf(
      RiskScoreRequest::dateOfBirth.name,
      RiskScoreRequest::ageAtFirstSanction.name,
    ),
  )
} else {
  null
}

fun addMissingFields(
  missingFields: List<String>,
  errors: List<ValidationErrorResponse>,
): List<ValidationErrorResponse> = addValidationErrorResponse(
  missingFields,
  errors,
  ValidationErrorType.MISSING_INPUT,
)

fun addUnexpectedFields(
  unexpectedFields: List<String>,
  errors: List<ValidationErrorResponse>,
): List<ValidationErrorResponse> = addValidationErrorResponse(
  unexpectedFields,
  errors,
  ValidationErrorType.UNEXPECTED_VALUE,
)

fun addMissingCriteriaValidation(
  criteriaFields: List<String>,
  errors: List<ValidationErrorResponse>,
): List<ValidationErrorResponse> = addValidationErrorResponse(
  criteriaFields,
  errors,
  ValidationErrorType.NOT_APPLICABLE,
)

private fun addValidationErrorResponse(
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

fun getMissingPropertiesErrorStrings(
  request: RiskScoreRequest,
  properties: List<String>,
): List<String> = properties
  .fold(arrayListOf()) { acc, propertyName ->
    acc.apply {
      val value = readInstanceProperty<Object>(request, propertyName)
      if (isNull(value)) {
        acc.add(propertyName)
      }
    }
  }

@Suppress("UNCHECKED_CAST")
fun <R> readInstanceProperty(instance: Any, propertyName: String): R {
  val property = instance::class.members
    // don't cast here to <Any, R>, it would succeed silently
    .first { it.name == propertyName } as KProperty1<Any, *>
  // force a invalid cast exception if incorrect type here
  return property.get(instance) as R
}
