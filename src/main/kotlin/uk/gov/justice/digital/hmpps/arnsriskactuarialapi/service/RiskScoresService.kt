package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.OGRS3Request
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.OGRS3Response
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.OffenderConvictionStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.util.getAgeGenderParameter
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.util.getAgeGroup
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.util.getOffenderCopasScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.util.getReoffendingProbability
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.util.getRiskBand
import java.time.temporal.ChronoUnit

@Service
class RiskScoresService {

  fun riskScoreProducer(riskScoreRequest: RiskScoreRequest): RiskScoreResponse {
    // todo
    return RiskScoreResponse(riskScoreRequest.score)
  }

  fun ogrs3Transform(ogrs3Request: OGRS3Request): OGRS3Response {

    val ageAtCurrentConviction = ChronoUnit.YEARS.between(ogrs3Request.dateOfBirth, ogrs3Request.dateOfCurrentConviction)
    val ageAtStartOfFollowup = ChronoUnit.YEARS.between(ogrs3Request.dateOfBirth, ogrs3Request.dateAtStartOfFollowup)
    val offenderAgeGroup = getAgeGroup(ageAtStartOfFollowup.toInt())

    val numberOfPreviousSanctions = ogrs3Request.totalNumberOfSanctions.minus(1)
    val offenderConvictionStatus = if (ogrs3Request.totalNumberOfSanctions == 1) OffenderConvictionStatus.FIRST_TIME_OFFENDER else OffenderConvictionStatus.REPEAT_OFFENDER
    val offenderCopasScore = getOffenderCopasScore(numberOfPreviousSanctions, ageAtCurrentConviction.toInt(), ogrs3Request.ageAtFirstSanction)

    val ageGenderParameter = getAgeGenderParameter(offenderAgeGroup, ogrs3Request.gender)
    val convictionStatusParameter = if (offenderConvictionStatus == OffenderConvictionStatus.REPEAT_OFFENDER) 0.46306 else 0.12614
    val copasParameter = offenderCopasScore*1.25112
    val offenceGroupParameter = getCurrentOffenceWeight(ogrs3Request.currentOffence)
    val totalForAllParameters = ageGenderParameter.plus(convictionStatusParameter).plus(copasParameter).plus(offenceGroupParameter)

    val ogrs3OneYear = getReoffendingProbability(totalForAllParameters, 1.40256)
    val ogrs3TwoYear =  getReoffendingProbability(totalForAllParameters, 2.1217)
    val riskBand = getRiskBand(ogrs3TwoYear)

    return OGRS3Response(ogrs3Request.algorithmVersion, ogrs3OneYear, ogrs3TwoYear, riskBand, listOf())
  }

  fun getCurrentOffenceWeight(offenceCode: String): Double {
    // placeholder map
    val offenceWeightMap: Map<String, Double> = mapOf(
      "00001" to 0.123,
      "00002" to 0.321,
      "00101" to 1.123,
      "00102" to 1.321,
      "00301" to -1.123,
    )

    return offenceWeightMap.getValue(offenceCode)
  }


}
