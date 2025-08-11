package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CustodyOrCommunity
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.NeedScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIRequestValidated

fun getOverallNeedClassification(
  overallNeedsScore: Int,
  inCustodyOrCommunity: CustodyOrCommunity,
  isMediumSara: Boolean,
  isHighSara: Boolean,
  allMissingFields: List<String>,
): NeedScore? {

  if (overallNeedsScore >= 6) return NeedScore.HIGH
  if (overallNeedsScore >= 3 && inCustodyOrCommunity == CustodyOrCommunity.COMMUNITY) return NeedScore.MEDIUM

  if (allMissingFields.isEmpty()) {
    val validScore = when (overallNeedsScore) {
      in 0..2 -> NeedScore.LOW
      in 3..5 -> NeedScore.MEDIUM
      //in 6..8 -> NeedScore.HIGH
      else -> null
    }
    return validScore
  }

  if (inCustodyOrCommunity == CustodyOrCommunity.COMMUNITY && (isMediumSara || isHighSara)) {
    return NeedScore.MEDIUM
  }
  if (inCustodyOrCommunity != CustodyOrCommunity.COMMUNITY && (isMediumSara || isHighSara)) {
    return NeedScore.HIGH
  }
  return null
}


