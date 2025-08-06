package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

data class SNSVObject(
  val snsvScore: RiskBand?,
  val scoreType: ScoreType?,
  val validationError: List<ValidationErrorResponse>?,
)
