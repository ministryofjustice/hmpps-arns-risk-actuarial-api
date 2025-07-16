package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.OGRS3Object
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getAgeAtCurrentConviction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getAgeGenderParameter
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getConvictionStatusParameter
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
    val validRequest = RiskScoreRequestValidated(
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

  private fun getOGRS3Object(riskScoreRequest: RiskScoreRequestValidated, errors: MutableList<ValidationErrorResponse>): OGRS3Object = runCatching {
    val ageAtCurrentConviction = getAgeAtCurrentConviction(
      riskScoreRequest.dateOfBirth,
      riskScoreRequest.dateOfCurrentConviction,
      riskScoreRequest.ageAtFirstSanction,
    )
    val offenderConvictionStatus = getOffenderConvictionStatus(riskScoreRequest.totalNumberOfSanctions)

    val ageGenderParameter = getAgeGenderParameter(ageAtCurrentConviction, riskScoreRequest.gender)
    val convictionStatusParameter = getConvictionStatusParameter(offenderConvictionStatus)
    val copasParameter = getOffenderCopasFinalScore(
      getOffenderCopasScore(
        riskScoreRequest.totalNumberOfSanctions,
        ageAtCurrentConviction,
        riskScoreRequest.ageAtFirstSanction,
      ),
    )
    val offenceGroupParameter = offenceGroupParametersService.getOGRS3Weighting(riskScoreRequest.currentOffence)

    val totalForAllParameters =
      ageGenderParameter.plus(convictionStatusParameter).plus(copasParameter).plus(offenceGroupParameter)
    val ogrs3OneYear = getOgrs3OneYear(totalForAllParameters).asPercentage().sanitisePercentage()
    val ogrs3TwoYear = getOgrs3TwoYear(totalForAllParameters).asPercentage().sanitisePercentage()
    val riskBand = getRiskBand(ogrs3TwoYear)

    return OGRS3Object(riskScoreRequest.version, ogrs3OneYear, ogrs3TwoYear, riskBand, emptyList())
  }.getOrElse {
    errors.add(
      ValidationErrorResponse(
        type = ValidationErrorType.NO_MATCHING_INPUT,
        message = "Error: ${it.message}",
        fields = null,
      ),
    )

    OGRS3Object(riskScoreRequest.version, null, null, null, errors)
  }
}
