package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType

class LDSValidationHelper {
  companion object {

    const val ERR_LESS_THAN_THREE_FIELDS = "ERR - Less than three fields set"
    val ELIGIBLE_FIELDS = listOf(
      RiskScoreRequest::workRelatedSkills.name,
      RiskScoreRequest::problemsWithReadingWritingNumeracy.name,
      RiskScoreRequest::learningDifficulties.name,
      RiskScoreRequest::professionalOrVocationalQualifications.name,
    )

    fun ldsInitialValidation(request: RiskScoreRequest): List<ValidationErrorResponse> {
      val missingFields = getMissingLDSFieldsValidation(request)
      return addMissingFields(missingFields, mutableListOf())
        .addEnoughFieldsPresent(request)
    }

    fun getMissingLDSFieldsValidation(request: RiskScoreRequest): List<String> = if (request.problemsWithReadingWritingNumeracy == null) {
      arrayListOf<String>()
        .addProblemsWithReadingWritingNumeracySubfield(request.hasProblemsWithReading, RiskScoreRequest::hasProblemsWithReading.name)
        .addProblemsWithReadingWritingNumeracySubfield(request.hasProblemsWithNumeracy, RiskScoreRequest::hasProblemsWithNumeracy.name)
    } else {
      emptyList()
    }

    private fun List<String>.addProblemsWithReadingWritingNumeracySubfield(
      difficulties: Boolean?,
      message: String,
    ): List<String> = if (difficulties != null) {
      this + "${RiskScoreRequest::problemsWithReadingWritingNumeracy.name} Field Not Present But $message Present"
    } else {
      this
    }

    fun List<ValidationErrorResponse>.addEnoughFieldsPresent(request: RiskScoreRequest): List<ValidationErrorResponse> = if (ELIGIBLE_FIELDS
        .mapNotNull { field -> readInstanceProperty(request, field) as Any? }
        .size < 3
    ) {
      this +
        ValidationErrorResponse(
          type = ValidationErrorType.NOT_APPLICABLE,
          message = ERR_LESS_THAN_THREE_FIELDS,
          fields = ELIGIBLE_FIELDS,
        )
    } else {
      this
    }
  }
}
