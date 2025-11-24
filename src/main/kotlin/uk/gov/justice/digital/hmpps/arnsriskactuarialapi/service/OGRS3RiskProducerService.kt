package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3.OGRS3Object
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3.OGRS3RequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.FeatureValue.AGE_GENDER_SCORE
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.FeatureValue.CONVICTION_STATUS_SCORE
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.FeatureValue.COPAS_SCORE
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.FeatureValue.OFFENCE_GROUP_PARAMETER
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getAgeGenderScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getConvictionStatusScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOffenderConvictionStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOffenderCopasFinalScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOffenderCopasScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOgrs3OneYear
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOgrs3TwoYear
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getRiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validateAgeAtCurrentConviction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validateAgeAtFirstSanction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validateOGRS3
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.asPercentage
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.getAgeAtDate
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.sanitisePercentage

@Service
class OGRS3RiskProducerService : BaseRiskScoreProducer() {

  private val log = LoggerFactory.getLogger(this::class.java)

  @Autowired
  lateinit var offenceCodeCacheService: OffenceCodeCacheService

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val errors = validateOGRS3(request)

    if (errors.isNotEmpty()) {
      return applyErrorsToContextAndReturn(context, errors)
    }

    val validRequest = OGRS3RequestValidated(
      request.gender!!,
      request.dateOfBirth!!,
      request.assessmentDate,
      request.dateOfCurrentConviction!!,
      request.dateAtStartOfFollowupCalculated!!,
      request.totalNumberOfSanctionsForAllOffences!!.toInt(),
      request.ageAtFirstSanction!!.toInt(),
      request.currentOffenceCode!!,

    )
    return context.apply { OGRS3 = getOGRS3Object(validRequest) }
  }

  private fun getOGRS3Object(
    request: OGRS3RequestValidated,
  ): OGRS3Object {
    val ageAtCurrentConviction = getAgeAtDate(
      request.dateOfBirth,
      request.dateOfCurrentConviction,
      "dateOfCurrentConviction",
    )
    val ageAtStartOfFollowup = getAgeAtDate(
      request.dateOfBirth,
      request.dateAtStartOfFollowup,
      "dateAtStartOfFollowup",
    )

    val errors = mutableListOf<ValidationErrorResponse>()
    validateAgeAtCurrentConviction(ageAtCurrentConviction, errors)
    validateAgeAtFirstSanction(request.ageAtFirstSanction, ageAtCurrentConviction, errors)
    val ogrS3Weighting = validateAndRetrieveOGRS3Weighting(request, errors)
    if (errors.isNotEmpty()) {
      return OGRS3Object(null, null, null, errors, null)
    }

    val offenderConvictionStatus = getOffenderConvictionStatus(request.totalNumberOfSanctionsForAllOffences)

    val ageGenderScore = getAgeGenderScore(ageAtStartOfFollowup, request.gender)
    val convictionStatusScore = getConvictionStatusScore(offenderConvictionStatus)
    val offenderCopasFinalScore = getOffenderCopasFinalScore(
      getOffenderCopasScore(
        request.totalNumberOfSanctionsForAllOffences - 1,
        ageAtCurrentConviction,
        request.ageAtFirstSanction,
      ),
    )
    listOf(
      ageGenderScore,
      convictionStatusScore,
      offenderCopasFinalScore,
      ogrS3Weighting!!,
    ).sum()
      .let { totalScore ->
        val oneYear = getOgrs3OneYear(totalScore).asPercentage().sanitisePercentage()
        val twoYear = getOgrs3TwoYear(totalScore).asPercentage().sanitisePercentage()
        val riskBand = getRiskBand(twoYear)

        return OGRS3Object(
          oneYear,
          twoYear,
          riskBand,
          emptyList(),
          mapOf(
            AGE_GENDER_SCORE.asPair(ageGenderScore),
            CONVICTION_STATUS_SCORE.asPair(convictionStatusScore),
            COPAS_SCORE.asPair(offenderCopasFinalScore),
            OFFENCE_GROUP_PARAMETER.asPair(ogrS3Weighting),
          ),
        )
      }
  }

  private fun validateAndRetrieveOGRS3Weighting(request: OGRS3RequestValidated, errors: MutableList<ValidationErrorResponse>): Double? {
    val ogrs3Weighting = offenceCodeCacheService.getOgrs3Weighting(request.currentOffenceCode)
    if (ogrs3Weighting?.value == null) {
      if (ogrs3Weighting?.error == OffenceCodeError.NEED_DETAILS_OF_EXACT_OFFENCE) {
        errors += ValidationErrorType.NEED_DETAILS_OF_EXACT_OFFENCE.asErrorResponse(listOf(RiskScoreRequest::currentOffenceCode.name))
      } else {
        log.warn("No offence code to actuarial weighting mapping found for ${request.currentOffenceCode}")
        errors += ValidationErrorType.OFFENCE_CODE_MAPPING_NOT_FOUND.asErrorResponseForOffenceCodeMappingNotFound(
          request.currentOffenceCode,
          listOf(RiskScoreRequest::currentOffenceCode.name),
        )
      }
    }
    return ogrs3Weighting?.value
  }

  override fun applyErrorsToContextAndReturn(context: RiskScoreContext, validationErrorResponses: List<ValidationErrorResponse>): RiskScoreContext = context.apply { OGRS3 = buildErrorObject(validationErrorResponses) }

  private fun buildErrorObject(validationErrorResponse: List<ValidationErrorResponse>): OGRS3Object = OGRS3Object(
    null,
    null,
    null,
    validationErrorResponse,
    null,
  )
}
