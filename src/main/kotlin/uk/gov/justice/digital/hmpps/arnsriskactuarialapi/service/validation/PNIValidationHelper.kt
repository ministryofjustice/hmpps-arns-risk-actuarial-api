package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError

val PNI_REQUIRED_FIELDS = listOf(
  RiskScoreRequest::supervisionStatus,
)

fun validatePNI(request: RiskScoreRequest): List<ValidationError> {
  val errors = mutableListOf<ValidationError>()
  validateRequiredFields(request, errors, PNI_REQUIRED_FIELDS)
  return errors
}
