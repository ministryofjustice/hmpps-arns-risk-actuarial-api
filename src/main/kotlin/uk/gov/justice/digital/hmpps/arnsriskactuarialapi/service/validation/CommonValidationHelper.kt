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

fun getTotalNumberOfSanctionsValidation(
  totalNumberOfSanctions: Integer?,
  errors: List<ValidationErrorResponse>,
): List<ValidationErrorResponse> {
  if (totalNumberOfSanctions != null && totalNumberOfSanctions < 1) {
    return errors +
      ValidationErrorResponse(
        type = ValidationErrorType.BELOW_MIN_VALUE,
        message = "ERR2 - Below minimum value",
        fields = listOf(RiskScoreRequest::totalNumberOfSanctions.name),
      )
  }

  return errors
}

fun getCurrentOffenceValidation(
  currentOffence: String?,
  errors: List<ValidationErrorResponse>,
): List<ValidationErrorResponse> {
  if (currentOffence != null && currentOffence.length != 5) {
    return errors +
      ValidationErrorResponse(
        type = ValidationErrorType.NO_MATCHING_INPUT,
        message = "ERR4 - Does not match agreed input",
        fields = listOf(RiskScoreRequest::currentOffence.name),
      )
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
  ValidationErrorResponse(
    type = ValidationErrorType.BELOW_MIN_VALUE,
    message = "ERR2 - Below minimum value",
    fields = listOf("ageAtCurrentConviction"),
  )
} else {
  null
}

private fun validateAgeAtFirstSanction(
  ageAtCurrentConviction: Int,
  ageAtFirstSanction: Int,
): ValidationErrorResponse? = if (ageAtFirstSanction > ageAtCurrentConviction) {
  ValidationErrorResponse(
    type = ValidationErrorType.BELOW_MIN_VALUE,
    message = "ERR2 - Below minimum value",
    fields = listOf("ageAtCurrentConviction", RiskScoreRequest::ageAtFirstSanction.name),
  )
} else {
  null
}

fun addMissingFields(
  missingFields: List<String>,
  errors: List<ValidationErrorResponse>,
): List<ValidationErrorResponse> = if (missingFields.isNotEmpty()) {
  errors +
    ValidationErrorResponse(
      type = ValidationErrorType.MISSING_INPUT,
      message = "ERR5 - Field is Null",
      fields = missingFields,
    )
} else {
  errors
}

fun addUnexpectedFields(
  unexpectedFields: List<String>,
  errors: List<ValidationErrorResponse>,
): List<ValidationErrorResponse> = if (unexpectedFields.isNotEmpty()) {
  errors +
    ValidationErrorResponse(
      type = ValidationErrorType.UNEXPECTED_VALUE,
      message = "ERR - Field is unexpected",
      fields = unexpectedFields,
    )
} else {
  errors
}

fun addMissingCriteriaValidation(
  criteriaFields: List<String>,
  errors: List<ValidationErrorResponse>,
): List<ValidationErrorResponse> = if (criteriaFields.isNotEmpty()) {
  errors + ValidationErrorResponse(
    type = ValidationErrorType.NOT_APPLICABLE,
    message = "ERR - Does not meet eligibility criteria",
    fields = criteriaFields,
  )
} else {
  errors
}

fun getMissingPropertiesErrorStrings(
  request: RiskScoreRequest,
  properties: List<String>,
): List<String> = properties
  .fold(arrayListOf<String>()) { acc, propertyName ->
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
