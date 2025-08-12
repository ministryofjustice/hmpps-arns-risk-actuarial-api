package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIRequestValidated


object SelfManagementDomainScore : DomainScore {
  override fun getMissingFields(request: PNIRequestValidated) = arrayListOf<String>().apply {
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

  override fun domainNeeds(request: PNIRequestValidated): Int? =
    listOfNotNull(
      request.impulsivityBehaviour?.score,
      request.temperControl?.score,
      request.problemSolvingSkills?.score,
      request.difficultiesCoping?.score,
    ).sum()

  override fun projectedNeeds(request: PNIRequestValidated): Int? =
    listOf(
      request.impulsivityBehaviour?.score ?: ProblemLevel.SIGNIFICANT_PROBLEMS.score,
      request.temperControl?.score ?: ProblemLevel.SIGNIFICANT_PROBLEMS.score,
      request.problemSolvingSkills?.score ?: ProblemLevel.SIGNIFICANT_PROBLEMS.score,
      request.difficultiesCoping?.score ?: ProblemLevel.SIGNIFICANT_PROBLEMS.score,
    ).sum()

  override fun overallDomainScore(request: PNIRequestValidated): Triple<Int, Int, List<String>> {
    val domainNeeds = domainNeeds(request)
    val projectedNeeds = projectedNeeds(request)
    val overallScore = getOverallScore(domainNeeds)
    val projectedScore = getOverallScore(projectedNeeds)
    val missingFields = getMissingFields(request)
    return Triple(overallScore ?: 0, projectedScore ?: 0, missingFields)
  }

  private fun getOverallScore(totalScore: Int?): Int? = when (totalScore) {
    in 0..1 -> 0
    in 2..4 -> 1
    in 5..8 -> 2
    else -> null
  }
}