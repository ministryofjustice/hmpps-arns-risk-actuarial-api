package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType

class LDSValidationHelper {
  companion object {

    const val ERR_LESS_THAN_THREE_FIELDS = "ERR - Less than three fields set"
    val ELIGIBLE_FIELDS = listOf(
      "transferableSkills",
      "educationDifficulties",
      "learningDifficulties",
      "professionalOrVocationalQualifications",
    )

    fun ldsInitialValidation(request: RiskScoreRequest): MutableList<ValidationErrorResponse> {
      val missingFields = getMissingLDSFieldsValidation(request)
      return addMissingFields(missingFields, mutableListOf())
        .addEnoughFieldsPresent(request)
    }

    fun getMissingLDSFieldsValidation(request: RiskScoreRequest): MutableList<String> {
      val missingFields = mutableListOf<String>()
      if (request.educationDifficulties == null) {
        missingFields
          .addEducationDifficultiesSubfield(request.readingDifficulties, "Reading Difficulties")
          .addEducationDifficultiesSubfield(request.numeracyDifficulties, "Numeracy Difficulties")
      }
      return missingFields
    }

    private fun MutableList<String>.addEducationDifficultiesSubfield(
      difficulties: Boolean?,
      message: String,
    ): MutableList<String> {
      if (difficulties != null) {
        this.add("Education Difficulties Field Not Present But $message Present")
      }
      return this
    }

    fun MutableList<ValidationErrorResponse>.addEnoughFieldsPresent(request: RiskScoreRequest): MutableList<ValidationErrorResponse> {
      if (ELIGIBLE_FIELDS
          .mapNotNull { field -> readInstanceProperty(request, field) as Any? }
          .size < 3
      ) {
        this.add(
          ValidationErrorResponse(
            type = ValidationErrorType.NOT_APPLICABLE,
            message = ERR_LESS_THAN_THREE_FIELDS,
            fields = ELIGIBLE_FIELDS,
          ),
        )
      }
      return this
    }
  }
}
