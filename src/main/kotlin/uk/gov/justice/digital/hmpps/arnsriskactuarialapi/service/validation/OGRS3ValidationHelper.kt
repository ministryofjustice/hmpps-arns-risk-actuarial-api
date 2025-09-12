package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType

fun validateOGRS3(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val errors = mutableListOf<ValidationErrorResponse>()
  validateRequiredFields(request, errors)
  validateTotalNumberOfSanctionsForAllOffences(request, errors)
  validateCurrentOffenceCode(request, errors)
  return errors
}

private fun validateRequiredFields(request: RiskScoreRequest, errors: MutableList<ValidationErrorResponse>) {
  val missingFields = arrayListOf<String>()

  missingFields.addIfNull(request, RiskScoreRequest::gender)
  missingFields.addIfNull(request, RiskScoreRequest::dateOfBirth)
  missingFields.addIfNull(request, RiskScoreRequest::dateOfCurrentConviction)
  missingFields.addIfNull(request, RiskScoreRequest::dateAtStartOfFollowup)
  missingFields.addIfNull(request, RiskScoreRequest::totalNumberOfSanctionsForAllOffences)
  missingFields.addIfNull(request, RiskScoreRequest::ageAtFirstSanction)
  missingFields.addIfNull(request, RiskScoreRequest::currentOffenceCode)

  if (missingFields.isNotEmpty()) {
    errors += ValidationErrorType.MISSING_INPUT.asErrorResponse(missingFields)
  }
}

private fun validateCurrentOffenceCode(request: RiskScoreRequest, errors: MutableList<ValidationErrorResponse>) {
  if (request.currentOffenceCode != null && request.currentOffenceCode.length != 5) {
    errors += ValidationErrorType.NO_MATCHING_INPUT.asErrorResponse(listOf(RiskScoreRequest::currentOffenceCode.name))
  }
}
