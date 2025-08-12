package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CustodyOrCommunity
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.NeedScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.anyNullSara
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.isHighSara
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.isMediumSara

fun overallNeedsGroupingCalculation(request: PNIRequestValidated): Triple<NeedScore?, NeedScore?, List<String>> {
  val (sexDomainScore, projectedSexDomainScore, missingSexDomainScore) = SexDomainScore.overallDomainScore(
    request,
  )
  val (thinkingDomainScore, projectedThinkingDomainScore, missingThinkingDomainScore) = ThinkingDomainScore.overallDomainScore(
    request,
  )
  val (relationshipDomainScore, projectedRelationshipDomainScore, missingRelationshipDomain) = RelationshipDomainScore.overallDomainScore(
    request,
  )
  val (selfManagementDomainScore, projectedSelfManagementDomainScore, missingSelfManagementDomain) = SelfManagementDomainScore.overallDomainScore(
    request,
  )

  val allDomainsAreMissingAnswers = missingSexDomainScore.isNotEmpty() &&
    missingThinkingDomainScore.isNotEmpty() &&
    missingRelationshipDomain.isNotEmpty() &&
    missingSelfManagementDomain.isNotEmpty()

  val allMissingFields = listOf(
    missingSexDomainScore,
    missingThinkingDomainScore,
    missingRelationshipDomain,
    missingSelfManagementDomain,
  ).flatten()

  val overallNeedsScore = listOfNotNull(
    sexDomainScore,
    thinkingDomainScore,
    relationshipDomainScore,
    selfManagementDomainScore,
  ).sum()

  val overallNeedsScoreProjected = listOfNotNull(
    projectedSexDomainScore,
    projectedThinkingDomainScore,
    projectedRelationshipDomainScore,
    projectedSelfManagementDomainScore,
  ).sum()

  val classifications = getOverallNeedClassification(
    overallNeedsScore,
    overallNeedsScoreProjected,
    request.inCustodyOrCommunity,
    isMediumSara(request) || isHighSara(request),
    anyNullSara(request),
    calculationComplete = allMissingFields.isEmpty(),
    allDomainsAreMissingAnswers = allDomainsAreMissingAnswers,
  )

  return Triple(
    classifications.first,
    classifications.second,
    allMissingFields,
  )
}

private fun getOverallNeedClassification(
  overallNeedsScore: Int,
  overallNeedsScoreProjected: Int,
  inCustodyOrCommunity: CustodyOrCommunity,
  isMediumOrHighSara: Boolean,
  anyNullSara: Boolean,
  calculationComplete: Boolean,
  allDomainsAreMissingAnswers: Boolean,
): Pair<NeedScore?, NeedScore?> {
  val overallNeedsLevel = getLevelFromScore(overallNeedsScore)
  val overallNeedsLevelProjected = getLevelFromScore(overallNeedsScoreProjected)

  if (overallNeedsScore >= 6) return Pair(NeedScore.HIGH, overallNeedsLevelProjected)
  if (overallNeedsScore >= 3 && inCustodyOrCommunity == CustodyOrCommunity.COMMUNITY) return Pair(NeedScore.MEDIUM, overallNeedsLevelProjected)

  if (!calculationComplete) {
    if (overallNeedsLevel == overallNeedsLevelProjected) {
      return Pair(overallNeedsLevel, overallNeedsLevelProjected)
    }
    if (allDomainsAreMissingAnswers && inCustodyOrCommunity != CustodyOrCommunity.COMMUNITY) {
      return Pair(null, overallNeedsLevelProjected)
    }
    if (overallNeedsLevel in setOf(NeedScore.HIGH, NeedScore.MEDIUM) && !(isMediumOrHighSara)) {
      return Pair(null, overallNeedsLevelProjected)
    }
    if (inCustodyOrCommunity == CustodyOrCommunity.COMMUNITY && (isMediumOrHighSara) && !anyNullSara) {
      return Pair(NeedScore.MEDIUM, overallNeedsLevelProjected)
    }
    if (inCustodyOrCommunity != CustodyOrCommunity.COMMUNITY && (isMediumOrHighSara) && !anyNullSara) {
      return Pair(NeedScore.HIGH, overallNeedsLevelProjected)
    }
  }
  return Pair(overallNeedsLevel, overallNeedsLevelProjected)
}

private fun getLevelFromScore(overallNeedsScore: Int): NeedScore? = when (overallNeedsScore) {
  in 0..2 -> NeedScore.LOW
  in 3..5 -> NeedScore.MEDIUM
  in 6..9 -> NeedScore.HIGH
  else -> null
}
