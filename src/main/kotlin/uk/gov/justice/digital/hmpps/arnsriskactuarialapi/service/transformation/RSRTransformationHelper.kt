package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CustodyOrCommunity
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.ScoreType
import java.time.LocalDate
import java.time.Period

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

fun getOSPDCRiskReduction(gender: Gender, inCustodyOrCommunity: CustodyOrCommunity, mostRecentOffenceDate: LocalDate, dateOfMostRecentSexualOffence: LocalDate, dateAtStartOfFollowup: LocalDate, assessmentDate: LocalDate, riskBand: RiskBand?): Boolean {
  if (riskBand == null) return false
  return !(
    gender == Gender.FEMALE ||
      inCustodyOrCommunity == CustodyOrCommunity.CUSTODY ||
      Period.between(mostRecentOffenceDate, assessmentDate).years < 5 ||
      Period.between(dateOfMostRecentSexualOffence, assessmentDate).years < 5 ||
      Period.between(dateAtStartOfFollowup, assessmentDate).years < 5 ||
      riskBand == RiskBand.LOW ||
      riskBand == RiskBand.NOT_APPLICABLE
    )
}

fun getOSPDCRiskBandReduction(ospRiskReduction: Boolean, riskBand: RiskBand?): RiskBand? {
  if (!ospRiskReduction || riskBand == null) return riskBand
  return when (riskBand) {
    RiskBand.VERY_HIGH -> RiskBand.HIGH
    RiskBand.HIGH -> RiskBand.MEDIUM
    RiskBand.MEDIUM -> RiskBand.LOW
    else -> throw IllegalArgumentException("Unsupported risk band reduction for value: $riskBand")
  }
}
