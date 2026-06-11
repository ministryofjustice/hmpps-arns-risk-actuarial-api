package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ovp

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

data class OVPObject(
  val provenViolentTypeReoffendingOneYear: Int?,
  val provenViolentTypeReoffendingTwoYear: Int?,
  val pointScore: Int?,
  val band: RiskBand?,
  val validationError: List<ValidationErrorResponse>?,
)
