package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIRequestValidated

object SexDomainScore {
  private fun getMissingFields(request: PNIRequestValidated) = mutableListOf<String>().apply {
    if (request.sexualPreoccupation == null) {
      add("sexualPreoccupation in SexDomainScore is null")
    }
    if (request.sexualInterestsOffenceRelated == null) {
      add("sexualInterestsOffenceRelated in SexDomainScore is null")
    }
    if (request.emotionalCongruence == null) {
      add("emotionalCongruence in SexDomainScore is null")
    }
  }

  private fun totalScore(request: PNIRequestValidated): Int? {
    val interimScore = listOfNotNull(
      request.sexualPreoccupation?.score,
      request.sexualInterestsOffenceRelated?.score,
      request.emotionalCongruence?.score,
    ).sum()
    return if (interimScore >= 4) {
      interimScore
    } else {
      null // cannot be calculated
    }
  }

  fun overallDomainScore(request: PNIRequestValidated): Pair<Int?, List<String>> {
    val totalScore = totalScore(request)
    val overallScore = when {
      totalScore in 0..1 -> 0
      totalScore in 2..3 -> 1
      totalScore in 4..6 || (request.sexualInterestsOffenceRelated?.score == 2) -> 2
      else -> null
    }
    val missingFields = if (overallScore == null) getMissingFields(request) else emptyList<String>()
    return Pair(overallScore, missingFields)
  }
}

object ThinkingDomainScore {
  private fun getMissingFields(request: PNIRequestValidated) = mutableListOf<String>().apply {
    if (request.proCriminalAttitudes == null) {
      add("proCriminalAttitudes in SelfManagementDomainScore is null")
    }
    if (request.hostileOrientation == null) {
      add("hostileOrientation in SelfManagementDomainScore is null")
    }
  }

  private fun totalScore(request: PNIRequestValidated): Int? {
    val interimScore = listOfNotNull(
      request.proCriminalAttitudes?.score,
      request.hostileOrientation?.score,
    ).sum()
    return if (request.proCriminalAttitudes == ProblemLevel.SIGNIFICANT_PROBLEMS) {
      interimScore
    } else {
      null // cannot be calculated
    }
  }

  fun overallDomainScore(request: PNIRequestValidated): Pair<Int?, List<String>> {
    val totalScore = totalScore(request)
    val overallScore = when {
      totalScore == 0 -> 0
      totalScore in 3..4 || (request.proCriminalAttitudes?.score == 2) -> 2
      totalScore in 1..2 -> 1
      else -> null
    }
    val missingFields = if (overallScore == null) getMissingFields(request) else emptyList<String>()
    return Pair(overallScore, missingFields)
  }
}

object RelationshipDomainScore {
  private fun getMissingFields(request: PNIRequestValidated) = mutableListOf<String>().apply {
    if (request.currentRelationshipFamilyMembers == null) {
      add("currentRelationshipFamilyMembers in RelationshipDomainScore is null")
    }
    if (request.previousCloseRelationships == null) {
      add("previousCloseRelationships in RelationshipDomainScore is null")
    }
    if (request.easilyInfluencedByCriminals == null) {
      add("easilyInfluencedByCriminals in RelationshipDomainScore is null")
    }
    if (request.controllingBehaviour == null) {
      add("controllingBehaviour in RelationshipDomainScore is null")
    }
  }

  private fun totalScore(request: PNIRequestValidated): Int? {
    val interimScore = listOfNotNull(
      request.currentRelationshipFamilyMembers?.score,
      request.previousCloseRelationships?.score,
      request.easilyInfluencedByCriminals?.score,
      request.controllingBehaviour?.score,
    ).sum()
    return if (interimScore >= 5) {
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
  private fun getMissingFields(request: PNIRequestValidated) = mutableListOf<String>().apply {
    if (request.impulsivityBehaviour == null) {
      add("impulsivityBehaviour in SelfManagementDomainScore is null")
    }
    if (request.temperControl == null) {
      add("temperControl in SelfManagementDomainScore is null")
    }
    if (request.problemSolvingSkills == null) {
      add("problemSolvingSkills in SelfManagementDomainScore is null")
    }
    if (request.difficultiesCoping == null) {
      add("difficultiesCoping in SelfManagementDomainScore is null")
    }
  }

  private fun totalScore(request: PNIRequestValidated): Int? {
    val interimScore = listOfNotNull(
      request.impulsivityBehaviour?.score,
      request.temperControl?.score,
      request.problemSolvingSkills?.score,
      request.difficultiesCoping?.score,
    ).sum()
    return if (interimScore >= 5) {
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
