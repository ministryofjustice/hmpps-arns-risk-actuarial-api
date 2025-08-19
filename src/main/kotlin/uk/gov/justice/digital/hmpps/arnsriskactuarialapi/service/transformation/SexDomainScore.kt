package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIRequestValidated

object SexDomainScore : DomainScore {
  override fun getMissingFields(request: PNIRequestValidated) = arrayListOf<String>().apply {
    if (request.sexualPreoccupation == null) {
      add("sexualPreoccupation")
    }
    if (request.offenceRelatedSexualInterests == null) {
      add("offenceRelatedSexualInterests")
    }
    if (request.emotionalCongruenceWithChildren == null) {
      add("emotionalCongruenceWithChildren")
    }
  }

  override fun domainNeeds(request: PNIRequestValidated): Int? {
    val hasNoMissingFields = getMissingFields(request).isEmpty()
    val interimScore = listOfNotNull(
      request.sexualPreoccupation?.score,
      request.offenceRelatedSexualInterests?.score,
      request.emotionalCongruenceWithChildren?.score,
    ).sum()
    return if (interimScore >= 4 || hasNoMissingFields) {
      interimScore
    } else {
      null // cannot be calculated
    }
  }

  override fun projectedNeeds(request: PNIRequestValidated): Int? {
    val interimScore = listOf(
      request.sexualPreoccupation?.score ?: ProblemLevel.SIGNIFICANT_PROBLEMS.score,
      request.offenceRelatedSexualInterests?.score ?: ProblemLevel.SIGNIFICANT_PROBLEMS.score,
      request.emotionalCongruenceWithChildren?.score ?: ProblemLevel.SIGNIFICANT_PROBLEMS.score,
    ).sum()
    return interimScore
  }

  override fun overallDomainScore(request: PNIRequestValidated): Triple<Int, Int, List<String>> {
    val projectedNeeds = projectedNeeds(request)
    val domainNeeds = domainNeeds(request)

    val overallScore = getOverallScore(request, domainNeeds)
    val projectedScore = getOverallScore(request, projectedNeeds)
    val missingFields = getMissingFields(request)
    if (!preCheckValid(request)) {
      return Triple(0, 0, missingFields)
    }
    return Triple(overallScore ?: 0, projectedScore ?: 0, missingFields)
  }

  private fun getOverallScore(
    request: PNIRequestValidated,
    interimScore: Int?,
  ): Int? = when {
    (request.offenceRelatedSexualInterests?.score == 2) -> 2
    interimScore in 0..1 -> 0
    interimScore in 2..3 -> 1
    interimScore in 4..6 -> 2
    else -> null
  }

  // The sex domain will only be calculated if hasEverCommittedSexualOffence or isARiskOfSexualHarm is Yes
  private fun preCheckValid(request: PNIRequestValidated): Boolean = request.hasEverCommittedSexualOffence == true || request.isARiskOfSexualHarm == true
}
