package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3.OGRS3Object
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3.OGRS3RequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getAgeAtCurrentConviction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getAgeGenderScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getConvictionStatusScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOffenderConvictionStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOffenderCopasFinalScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOffenderCopasScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOgrs3OneYear
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOgrs3TwoYear
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getRiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.ogrs3InitialValidation
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validateAge
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.asPercentage
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.sanitisePercentage

@Service
class OGRS3RiskProducerService : RiskProducer<OGRS3Object> {

  @Autowired
  lateinit var offenceGroupParametersService: OffenceGroupParametersService

  override fun getRiskScore(riskScoreRequest: RiskScoreRequest): OGRS3Object {
    val errors = ogrs3InitialValidation(riskScoreRequest)

    if (errors.isNotEmpty()) {
      return OGRS3Object(riskScoreRequest.version, null, null, null, errors)
    }

    val validRequest = OGRS3RequestValidated(
      riskScoreRequest.version,
      riskScoreRequest.gender!!,
      riskScoreRequest.dateOfBirth!!,
      riskScoreRequest.dateOfCurrentConviction!!,
      riskScoreRequest.dateAtStartOfFollowup!!,
      riskScoreRequest.totalNumberOfSanctions!!.toInt(),
      riskScoreRequest.ageAtFirstSanction!!.toInt(),
      riskScoreRequest.currentOffence!!,
    )
    return getOGRS3Object(validRequest, errors)
  }

  private fun getOGRS3Object(
    request: OGRS3RequestValidated,
    errors: MutableList<ValidationErrorResponse>,
  ): OGRS3Object = runCatching {
    val ageAtCurrentConviction = getAgeAtCurrentConviction(
      request.dateOfBirth,
      request.dateOfCurrentConviction,
    )

    errors.addAll(validateAge(ageAtCurrentConviction, request.ageAtFirstSanction, errors))

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
