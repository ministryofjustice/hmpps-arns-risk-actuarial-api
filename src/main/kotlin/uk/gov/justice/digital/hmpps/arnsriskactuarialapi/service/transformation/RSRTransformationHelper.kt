package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand

fun getRSRBand(rsrScore: Double?): RiskBand? {
  if (rsrScore == null) return null
  return when {
    rsrScore >= 0.0 && rsrScore <= 3.0 -> RiskBand.LOW
    rsrScore > 3.0 && rsrScore < 6.9 -> RiskBand.MEDIUM
    rsrScore >= 6.9 -> RiskBand.HIGH
    else -> throw IllegalArgumentException("RSR Score out of supported range: $rsrScore")
  }
}
