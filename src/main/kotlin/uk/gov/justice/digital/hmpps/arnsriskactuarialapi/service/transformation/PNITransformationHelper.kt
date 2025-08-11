package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CustodyOrCommunity
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.NeedScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
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

  if (inCustodyOrCommunity == CustodyOrCommunity.COMMUNITY && isMediumSara) {
    return NeedScore.MEDIUM
  }
  if (inCustodyOrCommunity != CustodyOrCommunity.COMMUNITY && (isMediumSara || isHighSara)) {
    return NeedScore.HIGH
  }
  return null
}


object SexDomainScore {
  private fun getMissingFields(request: PNIRequestValidated) = arrayListOf<String>().apply {
    if (request.sexualPreoccupation == null) {
      add("sexualPreoccupation")
    }
    if (request.sexualInterestsOffenceRelated == null) {
      add("sexualInterestsOffenceRelated")
    }
    if (request.emotionalCongruence == null) {
      add("emotionalCongruence")
    }
  }

  private fun totalScore(request: PNIRequestValidated): Int? {
    val hasNoMissingFields = getMissingFields(request).isEmpty()
    val interimScore = listOfNotNull(
      request.sexualPreoccupation?.score,
      request.sexualInterestsOffenceRelated?.score,
      request.emotionalCongruence?.score,
    ).sum()
    return if (interimScore >= 4 || hasNoMissingFields) {
      interimScore
    } else {
      null // cannot be calculated
    }
  }

  private fun adjustedScore(request: PNIRequestValidated): Int? {
    // Return null if any fields are missing
    if (getMissingFields(request).isNotEmpty()) return null

    val g = request.sexualPreoccupation?.score ?: return null
    val hRaw = request.sexualInterestsOffenceRelated?.score ?: return null
    val h = if (hRaw == 2) 4 else hRaw  // Special adjustment
    val i = request.emotionalCongruence?.score ?: return null

    return g + h + i
  }

  fun overallDomainScore(request: PNIRequestValidated): Pair<Int?, List<String>> {
    if (!preCheckValid(request)) {
      return Pair(0, emptyList<String>())
    }
    val totalScore = totalScore(request)
    val overallScore = when {
      (request.sexualInterestsOffenceRelated?.score == 2) -> 2
      totalScore in 0..1 -> 0
      totalScore in 2..3 -> 1
      totalScore in 4..6 -> 2
      else -> null
    }
    val missingFields = if (overallScore == null) getMissingFields(request) else emptyList<String>()
    return Pair(overallScore, missingFields)
  }

  // The sex domain will only be calculated if hasCommittedSexualOffence or riskSexualHarm is Yes
  private fun preCheckValid(request: PNIRequestValidated): Boolean =
    request.hasCommittedSexualOffence == true || request.riskSexualHarm == true
}

object ThinkingDomainScore {
  private fun getMissingFields(request: PNIRequestValidated) = arrayListOf<String>().apply {
    if (request.proCriminalAttitudes == null) {
      add("proCriminalAttitudes")
    }
    if (request.hostileOrientation == null) {
      add("hostileOrientation")
    }
  }

  private fun totalScore(request: PNIRequestValidated): Int? {
    val hasNoMissingFields = getMissingFields(request).isEmpty()
    val interimScore = listOfNotNull(
      request.proCriminalAttitudes?.score,
      request.hostileOrientation?.score,
    ).sum()
    return if (request.proCriminalAttitudes == ProblemLevel.SIGNIFICANT_PROBLEMS || hasNoMissingFields) {
      interimScore
    } else {
      null // cannot be calculated
    }
  }

  fun overallDomainScore(request: PNIRequestValidated): Pair<Int?, List<String>> {
    val totalScore = totalScore(request)
    val overallScore = when {
      (request.proCriminalAttitudes?.score == 2) -> 2
      totalScore == 0 -> 0
      totalScore in 3..4 -> 2
      totalScore in 1..2 -> 1
      else -> null
    }
    val missingFields = if (overallScore == null) getMissingFields(request) else emptyList<String>()
    return Pair(overallScore, missingFields)
  }
}

object RelationshipDomainScore {
  private fun getMissingFields(request: PNIRequestValidated) = arrayListOf<String>().apply {
    if (request.currentRelationshipFamilyMembers == null) {
      add("currentRelationshipFamilyMembers")
    }
    if (request.previousCloseRelationships == null) {
      add("previousCloseRelationships")
    }
    if (request.easilyInfluencedByCriminals == null) {
      add("easilyInfluencedByCriminals")
    }
    if (request.controllingBehaviour == null) {
      add("controllingBehaviour")
    }
  }

  private fun totalScore(request: PNIRequestValidated): Int? {
    val hasNoMissingFields = getMissingFields(request).isEmpty()
    val interimScore = listOfNotNull(
      request.currentRelationshipFamilyMembers?.score,
      request.previousCloseRelationships?.score,
      request.easilyInfluencedByCriminals?.score,
      request.controllingBehaviour?.score,
    ).sum()
    return if (interimScore >= 5 || hasNoMissingFields) {
      interimScore
    } else {
      null // cannot be calculated
    }
  }

  fun overallDomainScore(request: PNIRequestValidated): Pair<Int?, List<String>> {
    val totalScore = totalScore(request)
    val overallScore = when (totalScore) {
      in 0..1 -> 0
      in 2..4 -> 1
      in 5..8 -> 2
      else -> null
    }
    val missingFields = if (overallScore == null) getMissingFields(request) else emptyList<String>()
    return Pair(overallScore, missingFields)
  }
}

object SelfManagementDomainScore {
  private fun getMissingFields(request: PNIRequestValidated) = arrayListOf<String>().apply {
    if (request.impulsivityBehaviour == null) {
      add("impulsivityBehaviour")
    }
    if (request.temperControl == null) {
      add("temperControl")
    }
    if (request.problemSolvingSkills == null) {
      add("problemSolvingSkills")
    }
    if (request.difficultiesCoping == null) {
      add("difficultiesCoping")
    }
  }

  private fun totalScore(request: PNIRequestValidated): Int? {
    val hasNoMissingFields = getMissingFields(request).isEmpty()
    val interimScore = listOfNotNull(
      request.impulsivityBehaviour?.score,
      request.temperControl?.score,
      request.problemSolvingSkills?.score,
      request.difficultiesCoping?.score,
    ).sum()
    return if (interimScore >= 5 || hasNoMissingFields) {
      interimScore
    } else {
      null // cannot be calculated
    }
  }

  fun overallDomainScore(request: PNIRequestValidated): Pair<Int?, List<String>> {
    val totalScore = totalScore(request)
    val overallScore = when (totalScore) {
      in 0..1 -> 0
      in 2..4 -> 1
      in 5..8 -> 2
      else -> null
    }
    val missingFields = if (overallScore == null) getMissingFields(request) else emptyList<String>()
    return Pair(overallScore, missingFields)
  }
}
