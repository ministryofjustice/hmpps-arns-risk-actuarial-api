package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType

@Component
class LDSValidator {
  private val ldsFieldsAtLeast3Of = listOf(
    RiskScoreRequest::workRelatedSkills,
    RiskScoreRequest::problemsWithReadingWritingNumeracy,
    RiskScoreRequest::learningDifficulties,
    RiskScoreRequest::professionalOrVocationalQualifications,
  )

  fun validateLDS(request: RiskScoreRequest): List<ValidationError> {
    val errors = mutableListOf<ValidationError>()
    validateRequiredFields(request, errors)
    validateAtLeastThreeFieldsPresent(request, errors)
    return errors
  }

  private fun validateAtLeastThreeFieldsPresent(request: RiskScoreRequest, errors: MutableList<ValidationError>) {
    ldsFieldsAtLeast3Of.mapNotNull {
      if (it.get(request) == null) it.name else null
    }.let {
      if (it.size > 1) {
        errors += ValidationErrorType.LDS_NOT_ENOUGH_FIELDS_PRESENT.asError(ldsFieldsAtLeast3Of.map { property -> property.name })
      }
    }
  }

  private fun validateRequiredFields(request: RiskScoreRequest, errors: MutableList<ValidationError>) {
    val missingFields = arrayListOf<String>()

    if (request.problemsWithReadingWritingNumeracy == null) {
      addProblemsWithReadingWritingNumeracySubfield(request.hasProblemsWithReading, RiskScoreRequest::hasProblemsWithReading.name)?.let { missingFields.add(it) }
      addProblemsWithReadingWritingNumeracySubfield(request.hasProblemsWithNumeracy, RiskScoreRequest::hasProblemsWithNumeracy.name)?.let { missingFields.add(it) }
    }

    if (missingFields.isNotEmpty()) {
      errors += ValidationErrorType.MISSING_MANDATORY_INPUT.asError(missingFields)
    }
  }

  private fun addProblemsWithReadingWritingNumeracySubfield(
    difficulties: Boolean?,
    message: String,
  ): String? = if (difficulties != null) {
    "${RiskScoreRequest::problemsWithReadingWritingNumeracy.name} Field Not Present But $message Present"
  } else {
    null
  }
}
