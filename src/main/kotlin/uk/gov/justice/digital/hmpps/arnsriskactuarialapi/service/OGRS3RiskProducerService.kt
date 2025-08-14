package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3.OGRS3Object
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3.OGRS3RequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getAgeDiffAtOffenceDate
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getAgeGenderScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getConvictionStatusScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOffenderConvictionStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOffenderCopasFinalScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOffenderCopasScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOgrs3OneYear
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOgrs3TwoYear
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getRiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isWithinLastTwoYears
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.ogrs3InitialValidation
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validateAge
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.asPercentage
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.sanitisePercentage

@Service
class OGRS3RiskProducerService : RiskScoreProducer {

  @Autowired
  lateinit var offenceGroupParametersService: OffenceGroupParametersService

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val errors = ogrs3InitialValidation(request)

    if (errors.isNotEmpty()) {
      return context.apply {
        OGRS3 = OGRS3Object(null, null, null, errors)
      }
    }

    val validRequest = OGRS3RequestValidated(
      request.gender!!,
      request.dateOfBirth!!,
      request.assessmentDate,
      request.dateOfCurrentConviction!!,
      request.dateAtStartOfFollowup!!,
      request.totalNumberOfSanctions!!.toInt(),
      request.ageAtFirstSanction!!.toInt(),
      request.currentOffence!!,

    )
    return context.apply { OGRS3 = getOGRS3Object(validRequest) }
  }

  private fun getOGRS3Object(
    request: OGRS3RequestValidated,
  ): OGRS3Object = runCatching {
    val ageAtCurrentConviction = getAgeDiffAtOffenceDate(
      request.dateOfBirth,
      request.dateOfCurrentConviction,
    )

    val followUpDate = if (isWithinLastTwoYears(request.dateAtStartOfFollowup)) {
      request.dateAtStartOfFollowup
    } else {
      request.assessmentDate
    }
    val ageAtStartOfFollowup = getAgeDiffAtOffenceDate(
      request.dateOfBirth,
      followUpDate,
    )

    val ageValidationErrors = validateAge(ageAtCurrentConviction, request.ageAtFirstSanction, emptyList())
    if (!ageValidationErrors.isEmpty()) {
      OGRS3Object(null, null, null, ageValidationErrors)
    }

    val offenderConvictionStatus = getOffenderConvictionStatus(request.totalNumberOfSanctions)

    listOf(
      getAgeGenderScore(ageAtStartOfFollowup, request.gender),
      getConvictionStatusScore(offenderConvictionStatus),
      getOffenderCopasFinalScore(
        getOffenderCopasScore(
          request.totalNumberOfSanctions - 1,
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

        OGRS3Object(oneYear, twoYear, riskBand, emptyList())
      }
  }.getOrElse {
    OGRS3Object(
      null,
      null,
      null,
      arrayListOf(
        ValidationErrorResponse(
          type = ValidationErrorType.NO_MATCHING_INPUT,
          message = "Error: ${it.message}",
          fields = null,
        ),
      ),
    )
  }
}
