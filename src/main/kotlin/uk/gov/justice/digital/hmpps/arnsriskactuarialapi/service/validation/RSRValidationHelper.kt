package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

fun rsrInitialValidation(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val errors = arrayListOf<ValidationErrorResponse>()
  return getMissingRSRFieldsValidation(request, errors)
}

fun getMissingRSRFieldsValidation(request: RiskScoreRequest, errors: List<ValidationErrorResponse>): List<ValidationErrorResponse> {
  val missingFields = arrayListOf<String>()

  missingFields.addIfNull(request, RiskScoreRequest::gender)
  missingFields.addIfNull(request, RiskScoreRequest::inCustodyOrCommunity)
  missingFields.addIfNull(request, RiskScoreRequest::mostRecentOffenceDate)
  missingFields.addIfNull(request, RiskScoreRequest::dateOfMostRecentSexualOffence)
  missingFields.addIfNull(request, RiskScoreRequest::dateAtStartOfFollowup)
  missingFields.addIfNull(request, RiskScoreRequest::totalNumberOfViolentSanctions)

  return addMissingFields(missingFields, errors)
}
