package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.OGRS3Object
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.OGRS3RequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getAgeAtCurrentConviction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getAgeGenderScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getConvictionStatusScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOffenderConvictionStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOffenderCopasFinalScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOffenderCopasScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOgrs3OneYear
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOgrs3TwoYear
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getRiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.asPercentage
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.sanitisePercentage

@Service
class OGRS3RiskProducerService : RiskProducer<OGRS3Object> {

  @Autowired
  lateinit var offenceGroupParametersService: OffenceGroupParametersService

  override fun getRiskScore(riskScoreRequest: RiskScoreRequest): OGRS3Object {
    // TODO: real validation
    val errors = mutableListOf<ValidationErrorResponse>()
    val validRequest = OGRS3RequestValidated(
      riskScoreRequest.version,
      riskScoreRequest.gender!!,
      riskScoreRequest.dateOfBirth!!,
      riskScoreRequest.dateOfCurrentConviction!!,
      riskScoreRequest.dateAtStartOfFollowup!!,
      riskScoreRequest.totalNumberOfSanctions!!,
      riskScoreRequest.ageAtFirstSanction!!,
      riskScoreRequest.currentOffence!!,
    )
    return getOGRS3Object(validRequest, errors)
  }

<<<<<<< Updated upstream
  private fun getOGRS3Object(request: RiskScoreRequestValidated, errors: MutableList<ValidationErrorResponse>): OGRS3Object = runCatching {
=======
  private fun getOGRS3Object(riskScoreRequest: OGRS3RequestValidated, errors: MutableList<ValidationErrorResponse>): OGRS3Object = runCatching {
>>>>>>> Stashed changes
    val ageAtCurrentConviction = getAgeAtCurrentConviction(
      request.dateOfBirth,
      request.dateOfCurrentConviction,
      request.ageAtFirstSanction,
    )
    val offenderConvictionStatus = getOffenderConvictionStatus(request.totalNumberOfSanctions)

    listOf(
      getAgeGenderScore(ageAtCurrentConviction, request.gender),
      getConvictionStatusScore(offenderConvictionStatus),
      getOffenderCopasFinalScore(
        getOffenderCopasScore(
          request.totalNumberOfSanctions,
          ageAtCurrentConviction,
          request.ageAtFirstSanction,
        ),
      ),
      offenceGroupParametersService.getOGRS3Weighting(request.currentOffence),
    ).sum()
      .let { totalScore ->
        val oneYear = getOgrs3OneYear(totalScore).asPercentage().sanitisePercentage()
        val twoYear = getOgrs3TwoYear(totalScore).asPercentage().sanitisePercentage()
        val riskBand = getRiskBand(twoYear)

        OGRS3Object(request.version, oneYear, twoYear, riskBand, emptyList())
      }
  }.getOrElse {
    errors.add(
      ValidationErrorResponse(
        type = ValidationErrorType.NO_MATCHING_INPUT,
        message = "Error: ${it.message}",
        fields = null,
      ),
    )

    OGRS3Object(request.version, null, null, null, errors)
  }
}
