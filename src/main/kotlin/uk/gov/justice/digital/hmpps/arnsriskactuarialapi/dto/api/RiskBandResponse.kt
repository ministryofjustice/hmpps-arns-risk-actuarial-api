package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.RiskBandResponse.HIGH
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.RiskBandResponse.LOW
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.RiskBandResponse.MEDIUM
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.RiskBandResponse.NOT_APPLICABLE
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.RiskBandResponse.VERY_HIGH

enum class RiskBandResponse {
  LOW,
  MEDIUM,
  HIGH,
  VERY_HIGH,
  NOT_APPLICABLE,
}

fun RiskBand?.toRiskBandResponse(): RiskBandResponse? = when (this) {
  RiskBand.LOW -> LOW
  RiskBand.MEDIUM -> MEDIUM
  RiskBand.HIGH -> HIGH
  RiskBand.VERY_HIGH -> VERY_HIGH
  RiskBand.NOT_APPLICABLE -> NOT_APPLICABLE
  null -> null
}
