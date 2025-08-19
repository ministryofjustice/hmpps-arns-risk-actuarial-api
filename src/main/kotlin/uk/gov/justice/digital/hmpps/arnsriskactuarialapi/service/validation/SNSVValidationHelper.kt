package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

val SNSV_STATIC_REQUIRED_PROPERTIES = listOf(
  RiskScoreRequest::gender,
  RiskScoreRequest::dateOfBirth,
  RiskScoreRequest::dateOfCurrentConviction,
  RiskScoreRequest::currentOffenceCode,
  RiskScoreRequest::totalNumberOfSanctionsForAllOffences,
  RiskScoreRequest::ageAtFirstSanction,
  RiskScoreRequest::inCustodyOrCommunity,
  RiskScoreRequest::dateAtStartOfFollowup,
  RiskScoreRequest::totalNumberOfViolentSanctions,
)

fun snsvInitialValidation(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val errors = arrayListOf<ValidationErrorResponse>()
  return getMissingSNSVFieldsValidation(request, errors)
}

fun getMissingSNSVFieldsValidation(request: RiskScoreRequest, errors: List<ValidationErrorResponse>): List<ValidationErrorResponse> = addMissingFields(getNullValuesFromProperties(request, SNSV_STATIC_REQUIRED_PROPERTIES), errors)
