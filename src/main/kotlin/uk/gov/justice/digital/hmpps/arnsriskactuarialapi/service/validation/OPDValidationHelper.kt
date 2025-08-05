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
      if (request.domesticAbusePartner != null) unexpectedFields.add("Domestic abuse partner")
      if (request.domesticAbuseFamily != null) unexpectedFields.add("Domestic abuse family")
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
  if (request.gender == null) missingFields.add("Gender")
  if (request.overallRiskForAssessment == null) missingFields.add("Overall risk for assessment")
  if (request.highestRiskLevel == null) missingFields.add("Highest risk level")
  if (request.eligibleForMappa == null && request.gender == Gender.FEMALE) missingFields.add("Eligible for mappa")
  if (request.currentOffence == null) missingFields.add("Current offence")
  if (request.custodialSentence == null) missingFields.add("Custodial sentence")
  if (request.domesticAbuse == true) {
    if (request.domesticAbusePartner == null) missingFields.add("Domestic abuse partner")
    if (request.domesticAbuseFamily == null) missingFields.add("Domestic abuse family")
  }
  return addMissingFields(missingFields, errors)
}
