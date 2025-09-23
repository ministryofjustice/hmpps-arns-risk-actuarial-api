package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import kotlin.reflect.KProperty1

val OPD_REQUIRED_FIELDS: List<KProperty1<RiskScoreRequest, Any?>> = listOf(
  RiskScoreRequest::gender,
  RiskScoreRequest::overallRiskForAssessment,
  RiskScoreRequest::highestRiskLevelOverAllAssessments,
  RiskScoreRequest::currentOffenceCode,
  RiskScoreRequest::hasCustodialSentence,
)

fun validateOPD(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val errors = mutableListOf<ValidationErrorResponse>()
  validateRequiredFields(request, errors)
  validateDomesticAbuse(request, errors)
  validateCurrentOffenceCode(request, errors)
  return errors
}

private fun validateDomesticAbuse(
  request: RiskScoreRequest,
  errors: MutableList<ValidationErrorResponse>,
) {
  val hasNoEvidenceOfDomesticAbuse = request.evidenceOfDomesticAbuse == null || !request.evidenceOfDomesticAbuse
  val hasDomesticAbuseFieldsNotNull = request.domesticAbuseAgainstPartner != null || request.domesticAbuseAgainstFamily != null
  if (hasNoEvidenceOfDomesticAbuse && hasDomesticAbuseFieldsNotNull) {
    errors += ValidationErrorType.DOMESTIC_ABUSE_INCONSISTENT_INPUT.asErrorResponse(
      listOf(
        RiskScoreRequest::evidenceOfDomesticAbuse.name,
        RiskScoreRequest::domesticAbuseAgainstFamily.name,
        RiskScoreRequest::domesticAbuseAgainstPartner.name,
      ),
    )
  }
}

private fun validateRequiredFields(
  request: RiskScoreRequest,
  errors: MutableList<ValidationErrorResponse>,
) {
  val missingFields = arrayListOf<String>()
  OPD_REQUIRED_FIELDS.forEach { missingFields.addIfNull(request, it) }
  if (request.isEligibleForMappa == null && request.gender == Gender.FEMALE) missingFields.add(RiskScoreRequest::isEligibleForMappa.name)
  if (request.evidenceOfDomesticAbuse == true) {
    missingFields.addIfNull(request, RiskScoreRequest::domesticAbuseAgainstPartner)
    missingFields.addIfNull(request, RiskScoreRequest::domesticAbuseAgainstFamily)
  }

  if (missingFields.isNotEmpty()) {
    errors += ValidationErrorType.MISSING_MANDATORY_INPUT.asErrorResponse(missingFields)
  }
}
