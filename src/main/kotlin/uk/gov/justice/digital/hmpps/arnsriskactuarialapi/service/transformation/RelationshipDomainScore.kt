package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIRequestValidated

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

  private fun domainNeeds(request: PNIRequestValidated): Int? {
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


  private fun projectedNeeds(request: PNIRequestValidated): Int? =
    listOf(
      request.currentRelationshipFamilyMembers?.score ?: ProblemLevel.SIGNIFICANT_PROBLEMS.score,
      request.previousCloseRelationships?.score ?: ProblemLevel.SIGNIFICANT_PROBLEMS.score,
      request.easilyInfluencedByCriminals?.score ?: ProblemLevel.SIGNIFICANT_PROBLEMS.score,
      request.controllingBehaviour?.score ?: ProblemLevel.SIGNIFICANT_PROBLEMS.score,
    ).sum()

  fun overallDomainScore(request: PNIRequestValidated): Triple<Int, Int, List<String>> {

    val projectedNeeds = projectedNeeds(request)
    val domainNeeds = domainNeeds(request)

    val overallScore = getOverallScore(domainNeeds)
    val projectedScore = getOverallScore(projectedNeeds)

    val missingFields = getMissingFields(request)
    return Triple(overallScore ?: 0, projectedScore ?: 0, missingFields)
  }

  private fun getOverallScore(interimScore: Int?): Int? = when (interimScore) {
    in 0..1 -> 0
    in 2..4 -> 1
    in 5..8 -> 2
    else -> null
  }
}