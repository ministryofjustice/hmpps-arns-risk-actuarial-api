package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

fun pniInitialValidation(request: RiskScoreRequest): MutableList<ValidationErrorResponse> = getMissingPNIFieldsValidation(request)

private fun getMissingPNIFieldsValidation(request: RiskScoreRequest): MutableList<ValidationErrorResponse> {
  val errors = mutableListOf<ValidationErrorResponse>()

  val missingFields = mutableListOf<String>()

  if (request.gender == null) missingFields.add("Gender")
  if (request.inCustodyOrCommunity == null) missingFields.add("inCustodyOrCommunity")

  return addMissingFields(missingFields, errors)
}
