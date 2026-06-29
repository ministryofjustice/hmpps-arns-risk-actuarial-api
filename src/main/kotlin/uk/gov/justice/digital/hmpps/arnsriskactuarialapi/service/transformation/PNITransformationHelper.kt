package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.NeedScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.SupervisionStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.constants.AllReoffendingPredictorConstant
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.constants.ViolentReoffendingPredictorConstant

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
    request.supervisionStatus,
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
  supervisionStatus: SupervisionStatus,
  isMediumOrHighSara: Boolean,
  anyNullSara: Boolean,
  calculationComplete: Boolean,
  allDomainsAreMissingAnswers: Boolean,
): Pair<NeedScore?, NeedScore?> {
  val overallNeedsLevel = getLevelFromScore(overallNeedsScore)
  val overallNeedsLevelProjected = getLevelFromScore(overallNeedsScoreProjected)

  if (overallNeedsScore >= 6) return Pair(NeedScore.HIGH, overallNeedsLevelProjected)
  if (overallNeedsScore >= 3 && supervisionStatus == SupervisionStatus.COMMUNITY) {
    return Pair(
      NeedScore.MEDIUM,
      overallNeedsLevelProjected,
    )
  }

  if (!calculationComplete) {
    if (overallNeedsLevel == overallNeedsLevelProjected) {
      return Pair(overallNeedsLevel, overallNeedsLevelProjected)
    }
    if (allDomainsAreMissingAnswers && supervisionStatus != SupervisionStatus.COMMUNITY) {
      return Pair(null, overallNeedsLevelProjected)
    }
    if (overallNeedsLevel in setOf(NeedScore.HIGH, NeedScore.MEDIUM) && !(isMediumOrHighSara)) {
      return Pair(null, overallNeedsLevelProjected)
    }
    if (supervisionStatus == SupervisionStatus.COMMUNITY && (isMediumOrHighSara) && !anyNullSara) {
      return Pair(NeedScore.MEDIUM, overallNeedsLevelProjected)
    }
    if (supervisionStatus != SupervisionStatus.COMMUNITY && (isMediumOrHighSara) && !anyNullSara) {
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

fun isHighAllReoffendingPredictor(requestValidated: PNIRequestValidated) = requestValidated.allReoffendingPredictorStaticScore?.let { it >= AllReoffendingPredictorConstant.HIGH_BAND_LOWER_BOUND } == true

fun isHighViolentReoffendingPredictor(requestValidated: PNIRequestValidated) = requestValidated.violentReoffendingPredictorStaticScore?.let { it >= ViolentReoffendingPredictorConstant.HIGH_BAND_LOWER_BOUND } == true

fun isDirectContactSexualReoffendingPredictorHigh(requestValidated: PNIRequestValidated): Boolean = requestValidated.directContactSexualReoffendingPredictorBand == RiskBand.HIGH || requestValidated.directContactSexualReoffendingPredictorBand == RiskBand.VERY_HIGH

fun isOspIicHigh(requestValidated: PNIRequestValidated): Boolean = requestValidated.imagesAndIndirectContactSexualReoffendingPredictorBand == RiskBand.HIGH || requestValidated.imagesAndIndirectContactSexualReoffendingPredictorBand == RiskBand.VERY_HIGH

fun isDirectContactSexualReoffendingPredictorMedium(requestValidated: PNIRequestValidated): Boolean = requestValidated.directContactSexualReoffendingPredictorBand == RiskBand.MEDIUM

fun isOspIicMedium(requestValidated: PNIRequestValidated): Boolean = requestValidated.imagesAndIndirectContactSexualReoffendingPredictorBand == RiskBand.MEDIUM

fun isRsrMedium(request: PNIRequestValidated): Boolean {
  val rsrIsMedium = request.rsr in 1..2
  return rsrIsMedium && isNullOrNa(request.directContactSexualReoffendingPredictorBand) && isNullOrNa(request.imagesAndIndirectContactSexualReoffendingPredictorBand)
}

fun isRsrHigh(requestValidated: PNIRequestValidated): Boolean {
  val isHighRsr = requestValidated.rsr?.let { it >= 3 } == true
  return isHighRsr && isNullOrNa(requestValidated.directContactSexualReoffendingPredictorBand) && isNullOrNa(requestValidated.imagesAndIndirectContactSexualReoffendingPredictorBand)
}

fun isNullOrNa(band: RiskBand?): Boolean = band == null || band == RiskBand.NOT_APPLICABLE

fun isAllReoffendingPredictorMedium(requestValidated: PNIRequestValidated) = requestValidated.allReoffendingPredictorStaticScore?.let { it in AllReoffendingPredictorConstant.MEDIUM_BAND_LOWER_BOUND..74.99 } == true

fun isMediumViolentReoffendingPredictor(requestValidated: PNIRequestValidated) = requestValidated.violentReoffendingPredictorStaticScore?.let { it in ViolentReoffendingPredictorConstant.MEDIUM_BAND_LOWER_BOUND..59.99 } == true

fun isHighSara(requestValidated: PNIRequestValidated) = requestValidated.saraRiskToOthers == RiskBand.HIGH ||
  requestValidated.saraRiskToPartner == RiskBand.HIGH

fun isMediumSara(requestValidated: PNIRequestValidated) = requestValidated.saraRiskToOthers == RiskBand.MEDIUM ||
  requestValidated.saraRiskToPartner == RiskBand.MEDIUM

fun anyNullSara(requestValidated: PNIRequestValidated) = requestValidated.saraRiskToOthers == null ||
  requestValidated.saraRiskToPartner == null

fun bothNullSara(requestValidated: PNIRequestValidated) = requestValidated.saraRiskToOthers == null &&
  requestValidated.saraRiskToPartner == null
