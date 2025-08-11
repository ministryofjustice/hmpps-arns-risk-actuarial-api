package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIRequestValidated


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

  private fun domainNeeds(request: PNIRequestValidated): Int? {
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

  private fun projectedNeeds(request: PNIRequestValidated): Int? =
    listOf(
      request.impulsivityBehaviour?.score ?: ProblemLevel.SIGNIFICANT_PROBLEMS.score,
      request.temperControl?.score ?: ProblemLevel.SIGNIFICANT_PROBLEMS.score,
      request.problemSolvingSkills?.score ?: ProblemLevel.SIGNIFICANT_PROBLEMS.score,
      request.difficultiesCoping?.score ?: ProblemLevel.SIGNIFICANT_PROBLEMS.score,
    ).sum()

  fun overallDomainScore(request: PNIRequestValidated): Triple<Int, Int, List<String>> {
    val domainNeeds = domainNeeds(request)
    val projectedNeeds = projectedNeeds(request)
    val overallScore = getOverallScore(domainNeeds)
    val projectedScore = getOverallScore(projectedNeeds)
    val missingFields = if (overallScore == null) getMissingFields(request) else emptyList<String>()
    return Triple(overallScore ?: 0, projectedScore ?: 0, missingFields)
  }

  private fun getOverallScore(totalScore: Int?): Int? = when (totalScore) {
    in 0..1 -> 0
    in 2..4 -> 1
    in 5..8 -> 2
    else -> null
  }
}