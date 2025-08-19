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
  when (request.evidenceOfDomesticAbuse) {
    null, false -> {
      if (request.domesticAbuseAgainstPartner != null) unexpectedFields.add(RiskScoreRequest::domesticAbuseAgainstPartner.name)
      if (request.domesticAbuseAgainstFamily != null) unexpectedFields.add(RiskScoreRequest::domesticAbuseAgainstFamily.name)
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
  missingFields.addIfNull(request, RiskScoreRequest::highestRiskLevelOverAllAssessments)
  missingFields.addIfNull(request, RiskScoreRequest::currentOffenceCode)
  missingFields.addIfNull(request, RiskScoreRequest::hasCustodialSentence)
  if (request.isEligibleForMappa == null && request.gender == Gender.FEMALE) missingFields.add(RiskScoreRequest::isEligibleForMappa.name)
  if (request.evidenceOfDomesticAbuse == true) {
    missingFields.addIfNull(request, RiskScoreRequest::domesticAbuseAgainstPartner)
    missingFields.addIfNull(request, RiskScoreRequest::domesticAbuseAgainstFamily)
  }
  return addMissingFields(missingFields, errors)
}
