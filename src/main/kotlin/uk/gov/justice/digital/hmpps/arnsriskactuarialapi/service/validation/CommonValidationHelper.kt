package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import java.util.Objects.isNull
import kotlin.reflect.KProperty1

private const val MIN_CONVICTION_AGE = 10

fun getTotalNumberOfSanctionsValidation(
  totalNumberOfSanctions: Integer?,
  errors: MutableList<ValidationErrorResponse>,
): MutableList<ValidationErrorResponse> {
  if (totalNumberOfSanctions != null) {
    if (totalNumberOfSanctions < 1) {
      errors.add(
        ValidationErrorResponse(
          type = ValidationErrorType.BELOW_MIN_VALUE,
          message = "ERR2 - Below minimum value",
          fields = listOf("Total number of sanctions"),
        ),
      )
    }
  }
  return errors
}

fun getCurrentOffenceValidation(
  currentOffence: String?,
  errors: MutableList<ValidationErrorResponse>,
): MutableList<ValidationErrorResponse> {
  if (currentOffence != null) {
    if (currentOffence.length != 5) {
      errors.add(
        ValidationErrorResponse(
          type = ValidationErrorType.NO_MATCHING_INPUT,
          message = "ERR4 - Does not match agreed input",
          fields = listOf("Current offence"),
        ),
      )
    }
  }
  return errors
}

fun validateAge(
  ageAtCurrentConviction: Int,
  ageAtFirstSanction: Int,
  errors: MutableList<ValidationErrorResponse>,
): MutableList<ValidationErrorResponse> {
  if (ageAtCurrentConviction < MIN_CONVICTION_AGE) {
    errors.add(
      ValidationErrorResponse(
        type = ValidationErrorType.BELOW_MIN_VALUE,
        message = "ERR2 - Below minimum value",
        fields = listOf("Age at current conviction"),
      ),
    )
  }

  if (ageAtFirstSanction > ageAtCurrentConviction) {
    errors.add(
      ValidationErrorResponse(
        type = ValidationErrorType.BELOW_MIN_VALUE,
        message = "ERR2 - Below minimum value",
        fields = listOf("Age at current conviction", "Age at first sanction"),
      ),
    )
  }
  return errors
}

fun addMissingFields(
  missingFields: MutableList<String>,
  errors: MutableList<ValidationErrorResponse>,
): MutableList<ValidationErrorResponse> {
  if (missingFields.isNotEmpty()) {
    errors.add(
      ValidationErrorResponse(
        type = ValidationErrorType.MISSING_INPUT,
        message = "ERR5 - Field is Null",
        fields = missingFields,
      ),
    )
  }
  return errors
}

fun addMissingCriteriaValidation(
  criteriaFields: MutableList<String>,
  errors: MutableList<ValidationErrorResponse>,
): MutableList<ValidationErrorResponse> {
  if (criteriaFields.isNotEmpty()) {
    errors.add(
      ValidationErrorResponse(
        type = ValidationErrorType.NOT_APPLICABLE,
        message = "ERR - Does not meet eligibility criteria",
        fields = criteriaFields,
      ),
    )
  }
  return errors
}

fun getMissingPropertiesErrorStrings(
  request: RiskScoreRequest,
  propertyToErrors: Map<String, String>,
): MutableList<String> {
  val missingFields = propertyToErrors.keys
    .fold(mutableListOf<String>()) { acc, propertyName ->
      acc.apply {
        val value = readInstanceProperty<Object>(request, propertyName)
        if (isNull(value)) {
          acc.add(propertyToErrors[propertyName]!!)
        }
      }
    }
  return missingFields
}

@Suppress("UNCHECKED_CAST")
fun <R> readInstanceProperty(instance: Any, propertyName: String): R {
  val property = instance::class.members
    // don't cast here to <Any, R>, it would succeed silently
    .first { it.name == propertyName } as KProperty1<Any, *>
  // force a invalid cast exception if incorrect type here
  return property.get(instance) as R
}
