package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

fun opdInitialValidation(request: RiskScoreRequest): List<ValidationErrorResponse> = mutableListOf<ValidationErrorResponse>()
  .let { getMissingOPDFieldsValidation(request, it) }
  .let { validateDomesticAbuse(request, it) }
  .let { getCurrentOffenceCodeValidation(request.currentOffenceCode, it) }

private fun validateDomesticAbuse(
  request: RiskScoreRequest,
  errors: List<ValidationErrorResponse>,
): List<ValidationErrorResponse> {
  val unexpectedFields = arrayListOf<String>()
  when (request.domesticAbuse) {
    null, false -> {
      if (request.domesticAbusePartner != null) unexpectedFields.add(RiskScoreRequest::domesticAbusePartner.name)
      if (request.domesticAbuseFamily != null) unexpectedFields.add(RiskScoreRequest::domesticAbuseFamily.name)
    }

    true -> return errors
  }

  return addUnexpectedFields(unexpectedFields, errors)
}

private fun getMissingOPDFieldsValidation(
  request: RiskScoreRequest,
  errors: List<ValidationErrorResponse>,
): List<ValidationErrorResponse> {
  val missingFields = arrayListOf<String>()

  missingFields.addIfNull(request, RiskScoreRequest::gender)
  missingFields.addIfNull(request, RiskScoreRequest::overallRiskForAssessment)
  missingFields.addIfNull(request, RiskScoreRequest::highestRiskLevel)
  missingFields.addIfNull(request, RiskScoreRequest::currentOffenceCode)
  missingFields.addIfNull(request, RiskScoreRequest::custodialSentence)
  if (request.eligibleForMappa == null && request.gender == Gender.FEMALE) missingFields.add(RiskScoreRequest::eligibleForMappa.name)
  if (request.domesticAbuse == true) {
    missingFields.addIfNull(request, RiskScoreRequest::domesticAbusePartner)
    missingFields.addIfNull(request, RiskScoreRequest::domesticAbuseFamily)
  }
  return addMissingFields(missingFields, errors)
}
