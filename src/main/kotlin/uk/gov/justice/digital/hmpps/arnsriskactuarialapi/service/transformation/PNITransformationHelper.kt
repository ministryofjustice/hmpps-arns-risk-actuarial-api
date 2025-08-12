package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CustodyOrCommunity
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.NeedScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.anyNullSara
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.isHighSara
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.isMediumSara


fun overallNeedsGroupingCalculation(request: PNIRequestValidated): Triple<NeedScore?, NeedScore?, List<String>> {
    val (overallSexDomainScore, projectedSexDomainScore, missingSexDomainScore) = SexDomainScore.overallDomainScore(
        request,
    )
    val (overallThinkingDomainScore, projectedThinkingDomainScore, missingThinkingDomainScore) = ThinkingDomainScore.overallDomainScore(
        request,
    )
    val (overallRelationshipDomainScore, projectedRelationshipDomainScore, missingRelationshipDomain) = RelationshipDomainScore.overallDomainScore(
        request,
    )
    val (overallSelfManagementDomainScore, projectedSelfManagementDomainScore, missingSelfManagementDomain) = SelfManagementDomainScore.overallDomainScore(
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
        overallSexDomainScore,
        overallThinkingDomainScore,
        overallRelationshipDomainScore,
        overallSelfManagementDomainScore,
    ).sum()

    val overallNeedsScoreProjected = listOfNotNull(
        projectedSexDomainScore,
        projectedThinkingDomainScore,
        projectedRelationshipDomainScore,
        projectedSelfManagementDomainScore,
    ).sum()

    val classifications = getOverallNeedClassification(
        overallSexDomainScore,
        overallNeedsScore,
        overallNeedsScoreProjected,
        request.inCustodyOrCommunity,
        isMediumSara(request) || isHighSara(request),
        anyNullSara(request),
        calculationComplete = allMissingFields.isEmpty(),
        allDomainsAreMissingAnswers = allDomainsAreMissingAnswers
    )

    return Triple(
        classifications.first,
        classifications.second,
        allMissingFields
    )
}

fun getOverallNeedClassification(
    overallSexDomainScore: Int?,
    overallNeedsScore: Int,
    overallNeedsScoreProjected: Int,
    inCustodyOrCommunity: CustodyOrCommunity,
    isMediumOrHighSara: Boolean,
    anyNullSara: Boolean,
    calculationComplete: Boolean,
    allDomainsAreMissingAnswers: Boolean
): Pair<NeedScore?, NeedScore?> {
    val overallNeedsLevel = getLevelFromScore(overallNeedsScore)
    val overallNeedsLevelProjected = getLevelFromScore(overallNeedsScoreProjected)

    if (overallNeedsScore >= 6) return Pair(NeedScore.HIGH,overallNeedsLevelProjected)
    if (overallNeedsScore >= 3 && inCustodyOrCommunity == CustodyOrCommunity.COMMUNITY) return Pair(NeedScore.MEDIUM,overallNeedsLevelProjected)

    if (!calculationComplete) {
        if (overallNeedsLevel == overallNeedsLevelProjected) {
            return Pair(overallNeedsLevel,overallNeedsLevelProjected)
        }

        val isOutcomeOmission = isOutcomeOmission(
            overallSexDomainScore,
            overallNeedsLevel,
            allDomainsAreMissingAnswers,
            isCommunity = inCustodyOrCommunity != CustodyOrCommunity.COMMUNITY
        )
        if (isOutcomeOmission) {
            return Pair(null,overallNeedsLevelProjected)
        }
        if (inCustodyOrCommunity == CustodyOrCommunity.COMMUNITY && (isMediumOrHighSara) && !anyNullSara) {
            return Pair(NeedScore.MEDIUM,overallNeedsLevelProjected)
        }
        if (inCustodyOrCommunity != CustodyOrCommunity.COMMUNITY && (isMediumOrHighSara) && !anyNullSara) {
            return Pair(NeedScore.HIGH,overallNeedsLevelProjected)
        }
    }
    return Pair(overallNeedsLevel,overallNeedsLevelProjected)
}

fun isOutcomeOmission(
    sexDomain: Int?,
    overallNeedsLevel: NeedScore?,
    allDomainsAreMissingAnswers: Boolean,
    isCommunity: Boolean
): Boolean {
    if (allDomainsAreMissingAnswers && isCommunity) return true
    if (sexDomain != null && sexDomain != 0) return false
    if (overallNeedsLevel !in setOf(NeedScore.HIGH, NeedScore.MEDIUM)) return false
    return true
}

private fun getLevelFromScore(overallNeedsScore: Int): NeedScore? =
    when (overallNeedsScore) {
        in 0..2 -> NeedScore.LOW
        in 3..5 -> NeedScore.MEDIUM
        in 6..9 -> NeedScore.HIGH
        else -> null
    }