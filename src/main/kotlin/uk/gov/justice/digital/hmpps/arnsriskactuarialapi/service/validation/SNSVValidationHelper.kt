package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

fun snsvInitialValidation(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val errors = arrayListOf<ValidationErrorResponse>()
  return getMissingSNSVFieldsValidation(request, errors)
}

fun getMissingSNSVFieldsValidation(request: RiskScoreRequest, errors: List<ValidationErrorResponse>): List<ValidationErrorResponse> {
  val missingFields = arrayListOf<String>()

  missingFields.addIfNull(request, RiskScoreRequest::gender)
  missingFields.addIfNull(request, RiskScoreRequest::dateOfBirth)
  missingFields.addIfNull(request, RiskScoreRequest::dateOfCurrentConviction)
  missingFields.addIfNull(request, RiskScoreRequest::currentOffence)
  missingFields.addIfNull(request, RiskScoreRequest::totalNumberOfSanctions)
  missingFields.addIfNull(request, RiskScoreRequest::ageAtFirstSanction)
  missingFields.addIfNull(request, RiskScoreRequest::inCustodyOrCommunity)
  missingFields.addIfNull(request, RiskScoreRequest::dateAtStartOfFollowup)
  missingFields.addIfNull(request, RiskScoreRequest::totalNumberOfViolentSanctions)

  return addMissingFields(missingFields, errors)
}
