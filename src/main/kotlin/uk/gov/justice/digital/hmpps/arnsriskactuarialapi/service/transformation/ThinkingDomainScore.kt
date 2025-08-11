package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIRequestValidated


object ThinkingDomainScore {
  private fun getMissingFields(request: PNIRequestValidated) = arrayListOf<String>().apply {
    if (request.proCriminalAttitudes == null) {
      add("proCriminalAttitudes")
    }
    if (request.hostileOrientation == null) {
      add("hostileOrientation")
    }
  }

  private fun projectedNeeds(request: PNIRequestValidated): Int? =
    listOf(
      request.proCriminalAttitudes?.score ?: ProblemLevel.SIGNIFICANT_PROBLEMS.score,
      request.hostileOrientation?.score ?: ProblemLevel.SIGNIFICANT_PROBLEMS.score,
    ).sum()

  private fun domainNeeds(request: PNIRequestValidated): Int? {
    val hasNoMissingFields = getMissingFields(request).isEmpty()
    val interimScore = listOfNotNull(
      request.proCriminalAttitudes?.score,
      request.hostileOrientation?.score,
    ).sum()
    return if ( true ) { // hasNoMissingFieldsrequest.proCriminalAttitudes == ProblemLevel.SIGNIFICANT_PROBLEMS ||
      interimScore
    } else {
      null // cannot be calculated
    }
  }

  fun overallDomainScore(request: PNIRequestValidated): Triple<Int, Int, List<String>> {
    val projectedNeeds = projectedNeeds(request)
    val domainNeeds = domainNeeds(request)

    val overallScore = getOverallScore(request, domainNeeds)
    val projectedScore = getOverallScore(request, projectedNeeds)

    val missingFields = if (overallScore == null) getMissingFields(request) else emptyList<String>()
    return Triple(overallScore ?: 0, projectedScore ?: 0, missingFields)
  }

  private fun getOverallScore(
    request: PNIRequestValidated,
    interimScore: Int?,
  ): Int? = when {
    (request.proCriminalAttitudes?.score == 2) -> 2
    interimScore == 0 -> 0
    interimScore in 3..4 -> 2
    interimScore in 1..2 -> 1
    else -> null
  }
}