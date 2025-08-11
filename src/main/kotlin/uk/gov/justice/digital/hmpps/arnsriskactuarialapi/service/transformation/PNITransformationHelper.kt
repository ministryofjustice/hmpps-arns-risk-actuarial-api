package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CustodyOrCommunity
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.NeedScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIRequestValidated
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
            overallNeedsScore,
            overallNeedsScoreProjected,
            request.inCustodyOrCommunity,
            isMediumSara(request),
            isHighSara(request),
            allMissingFields,
        ),
        allMissingFields,
    )
}

fun getOverallNeedClassification(
    overallNeedsScore: Int,
    overallNeedsScoreProjected: Int,
    inCustodyOrCommunity: CustodyOrCommunity,
    isMediumSara: Boolean,
    isHighSara: Boolean,
    allMissingFields: List<String>,
): NeedScore? {

    if (overallNeedsScore >= 6) return NeedScore.HIGH
    if (overallNeedsScore >= 3 && inCustodyOrCommunity == CustodyOrCommunity.COMMUNITY) return NeedScore.MEDIUM


    val overallNeedsLevel = getLevelFromScore(overallNeedsScore)
    val overallNeedsLevelProjected = getLevelFromScore(overallNeedsScoreProjected)

    // calculation complete: Yes
    if(allMissingFields.isEmpty()){
        return overallNeedsLevel
    }

    // calculation complete: No
    if (overallNeedsLevel == overallNeedsLevelProjected) {
        return overallNeedsLevel
    }
    if (inCustodyOrCommunity == CustodyOrCommunity.COMMUNITY && (isMediumSara || isHighSara)) {
        return NeedScore.MEDIUM
    }
    if (inCustodyOrCommunity != CustodyOrCommunity.COMMUNITY && (isMediumSara || isHighSara)) {
        return NeedScore.HIGH
    }

    return null
}

private fun getLevelFromScore(overallNeedsScore: Int): NeedScore? =
    when (overallNeedsScore) {
        in 0..2 -> NeedScore.LOW
        in 3..5 -> NeedScore.MEDIUM
        in 6..9 -> NeedScore.HIGH
        else -> null
    }


