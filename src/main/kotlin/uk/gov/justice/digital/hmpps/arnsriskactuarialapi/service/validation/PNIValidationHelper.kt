package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

fun pniInitialValidation(request: RiskScoreRequest): List<ValidationErrorResponse> = getMissingPNIFieldsValidation(request)

private fun getMissingPNIFieldsValidation(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val errors = arrayListOf<ValidationErrorResponse>()
  val missingFields = arrayListOf<String>()

  missingFields.addIfNull(request, RiskScoreRequest::gender)
  missingFields.addIfNull(request, RiskScoreRequest::inCustodyOrCommunity)

  return addMissingFields(missingFields, errors)
}
