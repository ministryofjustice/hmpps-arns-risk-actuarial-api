package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ospiic

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

data class OSPIICObject(
  val band: RiskBand?,
  val score: Double?,
  val validationError: List<ValidationErrorResponse>?,
)
