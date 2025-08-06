package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

fun pniInitialValidation(request: RiskScoreRequest): List<ValidationErrorResponse> = getMissingPNIFieldsValidation(request)

private fun getMissingPNIFieldsValidation(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val errors = arrayListOf<ValidationErrorResponse>()

  val missingFields = arrayListOf<String>()

  if (request.gender == null) missingFields.add("gender")
  if (request.inCustodyOrCommunity == null) missingFields.add("inCustodyOrCommunity")

  return addMissingFields(missingFields, errors)
}
