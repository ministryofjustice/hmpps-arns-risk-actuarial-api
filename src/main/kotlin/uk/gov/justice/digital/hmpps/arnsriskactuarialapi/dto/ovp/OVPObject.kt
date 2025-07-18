package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ovp

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

data class OVPObject(
  val algorithmVersion: String,
  val provenViolentTypeReoffendingOneYear: Int?,
  val provenViolentTypeReoffendingTwoYear: Int?,
  val band: RiskBand?,
  val validationError: List<ValidationErrorResponse>?,
)
