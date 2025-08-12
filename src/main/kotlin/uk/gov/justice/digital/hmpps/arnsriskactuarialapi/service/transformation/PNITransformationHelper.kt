package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CustodyOrCommunity
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.NeedScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.anyNullSara
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.isHighSara
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.isMediumSara


fun overallNeedsGroupingCalculation(request: PNIRequestValidated): Pair<NeedScore?, List<String>> {
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

    return Pair(
        getOverallNeedClassification(
            overallSexDomainScore,
            overallNeedsScore,
            overallNeedsScoreProjected,
            request.inCustodyOrCommunity,
            isMediumSara(request),
            isHighSara(request),
            anyNullSara(request),
            calculationComplete = allMissingFields.isEmpty(),
            allDomainsAreMissingAnswers = allDomainsAreMissingAnswers
        ),
        allMissingFields,
    )
}

fun getOverallNeedClassification(
    overallSexDomainScore: Int?,
    overallNeedsScore: Int,
    overallNeedsScoreProjected: Int,
    inCustodyOrCommunity: CustodyOrCommunity,
    isMediumSara: Boolean,
    isHighSara: Boolean,
    anyNullSara: Boolean,
    calculationComplete: Boolean,
    allDomainsAreMissingAnswers: Boolean
): NeedScore? {

    if (overallNeedsScore >= 6) return NeedScore.HIGH
    if (overallNeedsScore >= 3 && inCustodyOrCommunity == CustodyOrCommunity.COMMUNITY) return NeedScore.MEDIUM


    val overallNeedsLevel = getLevelFromScore(overallNeedsScore)
    val overallNeedsLevelProjected = getLevelFromScore(overallNeedsScoreProjected)

    if (calculationComplete) {
        return overallNeedsLevel
    }

    if (overallNeedsLevel == overallNeedsLevelProjected) {
        return overallNeedsLevel
    }

    val isOutcomeOmission = isOutcomeOmission(
        overallSexDomainScore,
        overallNeedsLevel,
        allDomainsAreMissingAnswers,
        isCommunity = inCustodyOrCommunity != CustodyOrCommunity.COMMUNITY
    )

    if (isOutcomeOmission) {
        return null
    }
    if (inCustodyOrCommunity == CustodyOrCommunity.COMMUNITY && (isMediumSara || isHighSara) && !anyNullSara) {
        return NeedScore.MEDIUM
    }
    if (inCustodyOrCommunity != CustodyOrCommunity.COMMUNITY && (isMediumSara || isHighSara) && !anyNullSara) {
        return NeedScore.HIGH
    }

    return overallNeedsLevel
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


