package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.ScoreType

fun getFullRSRScore(snsvScore: Double?, ospdcScore: Double?, ospiicScore: Double?, snsvScoreType: ScoreType?): Double? = when (snsvScoreType) {
  ScoreType.STATIC -> snsvScore
  ScoreType.DYNAMIC -> listOfNotNull(snsvScore, ospdcScore, ospiicScore).sum()
  else -> throw IllegalArgumentException("RSR Score type unknow, must be static or dynamic")
}

fun getRSRBand(rsrScore: Double?): RiskBand {
  if (rsrScore == null) throw IllegalArgumentException("RSR Score is null")
  return when {
    rsrScore >= 0.0 && rsrScore <= 3.0 -> RiskBand.LOW
    rsrScore > 3.0 && rsrScore <= 6.8 -> RiskBand.MEDIUM
    rsrScore >= 6.9 -> RiskBand.HIGH
    else -> throw IllegalArgumentException("RSR Score out of supported range: $rsrScore")
  }
}
