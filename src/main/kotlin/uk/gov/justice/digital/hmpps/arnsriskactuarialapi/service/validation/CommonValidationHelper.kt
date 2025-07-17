package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType


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
    errors: MutableList<ValidationErrorResponse>
): MutableList<ValidationErrorResponse> {
    if (ageAtCurrentConviction < MIN_CONVICTION_AGE) {
        errors.add(
            ValidationErrorResponse(
                type = ValidationErrorType.BELOW_MIN_VALUE,
                message = "ERR6 - Age at current conviction must be at least $MIN_CONVICTION_AGE.",
                fields = listOf("Age at current conviction"),
            ),
        )
    }

    if (ageAtFirstSanction > ageAtCurrentConviction) {
        errors.add(
            ValidationErrorResponse(
                type = ValidationErrorType.BELOW_MIN_VALUE,
                message = "ERR7 - Age at first sanction cannot be greater than age at current conviction.",
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
