package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

fun opdInitialValidation(request: RiskScoreRequest): List<ValidationErrorResponse> = mutableListOf<ValidationErrorResponse>()
  .let { getMissingOPDFieldsValidation(request, it) }
  .let { validateDomesticAbuse(request, it) }
  .let { getCurrentOffenceValidation(request.currentOffence, it) }

private fun validateDomesticAbuse(
  request: RiskScoreRequest,
  errors: List<ValidationErrorResponse>,
): List<ValidationErrorResponse> {
  val unexpectedFields = arrayListOf<String>()
  when (request.domesticAbuse) {
    null, false -> {
      if (request.domesticAbusePartner != null) unexpectedFields.add("domesticAbusePartner")
      if (request.domesticAbuseFamily != null) unexpectedFields.add("domesticAbuseFamily")
    }

    true -> return errors
  }

  return addUnexpectedFields(unexpectedFields, errors)
}

private fun getMissingOPDFieldsValidation(
  request: RiskScoreRequest,
  errors: List<ValidationErrorResponse>,
): List<ValidationErrorResponse> {
  val missingFields = mutableListOf<String>()
  if (request.gender == null) missingFields.add("gender")
  if (request.overallRiskForAssessment == null) missingFields.add("overallRiskForAssessment")
  if (request.highestRiskLevel == null) missingFields.add("highestRiskLevel")
  if (request.eligibleForMappa == null && request.gender == Gender.FEMALE) missingFields.add("eligibleForMappa")
  if (request.currentOffence == null) missingFields.add("currentOffence")
  if (request.custodialSentence == null) missingFields.add("custodialSentence")
  if (request.domesticAbuse == true) {
    if (request.domesticAbusePartner == null) missingFields.add("domesticAbusePartner")
    if (request.domesticAbuseFamily == null) missingFields.add("domesticAbuseFamily")
  }
  return addMissingFields(missingFields, errors)
}
