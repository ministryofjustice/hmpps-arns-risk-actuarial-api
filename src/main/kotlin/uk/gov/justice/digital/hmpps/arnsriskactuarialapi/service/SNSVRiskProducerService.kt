package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.SNSVDynamicRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.SNSVObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.SNSVStaticRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.ScoreType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.bingeDrinkingProblemWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.chronicDrinkingProblemsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.currentRelationshipWithPartnerWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.didOffenceInvolveCarryingOrUsingWeaponWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.domesticViolenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.get2YearInterceptWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getAgeGenderPolynomialWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getDomesticViolencePerpetrator
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getGenderWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getMonthsSinceLastSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getNumberOfSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getSecondSanctionCasesOnlyWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getThreePlusSanctionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getTotalSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getViolenceRateWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getViolentHistoryWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getViolentSanctionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.impulsivityProblemsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.isUnemployedWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.previousConvictionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.proCriminalAttitudesWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.suitabilityOfAccommodationWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.temperControlWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.isValidDynamicSnsv
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.snsvDynamicValidation
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validateSNSV
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.roundToNDecimals
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.sigmoid

@Service
class SNSVRiskProducerService : BaseRiskScoreProducer() {

  private val log = LoggerFactory.getLogger(this::class.java)

  @Autowired
  lateinit var offenceGroupParametersService: OffenceGroupParametersService

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val errors = validateSNSV(request)
    if (errors.isNotEmpty()) {
      return applyErrorsToContextAndReturn(context, errors)
    }
    return context.apply {
      SNSV =
        run {
          val scoreType = isSNSVDynamic(request)
          getSNSVObject(scoreType, request)
        }
    }
  }

  override fun applyErrorsToContextAndReturn(
    context: RiskScoreContext,
    validationErrorResponses: List<ValidationErrorResponse>,
  ): RiskScoreContext = context.apply { SNSV = SNSVObject(null, null, validationErrorResponses) }

  fun getSNSVObject(
    scoreType: ScoreType,
    request: RiskScoreRequest,
  ): SNSVObject {
    val errors = mutableListOf<ValidationErrorResponse>()
    val weightingSNSV = retrieveWeightingSNSV(scoreType, request)
    val weightingSNSVVATP = retrieveWeightingSNSVVATP(scoreType, request)
    if (weightingSNSV == null || weightingSNSVVATP == null) {
      log.warn("No offence code to actuarial weighting mapping found for ${request.currentOffenceCode}")
      errors += ValidationErrorType.OFFENCE_CODE_MAPPING_NOT_FOUND.asErrorResponseForOffenceCodeMappingNotFound(request.currentOffenceCode, listOf(RiskScoreRequest::currentOffenceCode.name))
    }
    if (errors.isNotEmpty()) {
      return SNSVObject(null, scoreType, errors)
    }
    snsvDynamicValidation(request, errors)
    return when (scoreType) {
      ScoreType.STATIC -> snvsStaticSum(request.toSNSVStaticRequestValidated(weightingSNSVVATP!!, weightingSNSV!!))
      ScoreType.DYNAMIC -> snvsDynamicSum(request.toSNSVDynamicRequestValidated(weightingSNSVVATP!!, weightingSNSV!!))
    }.let { SNSVObject(it.sigmoid().roundToNDecimals(16), scoreType, errors) }
  }

  private fun retrieveWeightingSNSV(scoreType: ScoreType, request: RiskScoreRequest): Double? = when (scoreType) {
    ScoreType.STATIC -> offenceGroupParametersService.getSNSVStaticWeighting(request.currentOffenceCode!!)
    ScoreType.DYNAMIC -> offenceGroupParametersService.getSNSVDynamicWeighting(request.currentOffenceCode!!)
  }

  private fun retrieveWeightingSNSVVATP(scoreType: ScoreType, request: RiskScoreRequest): Double? = when (scoreType) {
    ScoreType.STATIC -> offenceGroupParametersService.getSNSVVATPStaticWeighting(request.currentOffenceCode!!)
    ScoreType.DYNAMIC -> offenceGroupParametersService.getSNSVVATPDynamicWeighting(request.currentOffenceCode!!)
  }

  fun isSNSVDynamic(request: RiskScoreRequest): ScoreType = if (isValidDynamicSnsv(request)) {
    ScoreType.DYNAMIC
  } else {
    ScoreType.STATIC
  }

  private fun RiskScoreRequest.toSNSVDynamicRequestValidated(weightingSNSVVATP: Double, weightingSNSV: Double): SNSVDynamicRequestValidated = SNSVDynamicRequestValidated(
    this.gender!!,
    this.dateOfBirth!!,
    this.assessmentDate,
    this.dateOfCurrentConviction!!,
    this.currentOffenceCode!!,
    this.totalNumberOfSanctionsForAllOffences!!.toInt(),
    this.ageAtFirstSanction!!.toInt(),
    this.supervisionStatus!!,
    this.dateAtStartOfFollowupUserInput!!,
    this.totalNumberOfViolentSanctions!!.toInt(),
    this.didOffenceInvolveCarryingOrUsingWeapon!!,
    this.suitabilityOfAccommodation!!,
    this.isUnemployed!!,
    this.currentRelationshipWithPartner!!,
    this.currentAlcoholUseProblems!!,
    this.excessiveAlcoholUse!!,
    this.impulsivityProblems!!,
    this.temperControl!!,
    this.proCriminalAttitudes!!,
    getDomesticViolencePerpetrator(this.evidenceOfDomesticAbuse, this.domesticAbuseAgainstPartner)!!,
    this.previousConvictions!!,
    weightingSNSVVATP,
    weightingSNSV,
  )

  private fun RiskScoreRequest.toSNSVStaticRequestValidated(weightingSNSVVATP: Double, weightingSNSV: Double) = SNSVStaticRequestValidated(
    this.gender!!,
    this.dateOfBirth!!,
    this.assessmentDate,
    this.dateOfCurrentConviction!!,
    this.currentOffenceCode!!,
    this.totalNumberOfSanctionsForAllOffences!!.toInt(),
    this.ageAtFirstSanction!!.toInt(),
    this.supervisionStatus!!,
    this.dateAtStartOfFollowupUserInput!!,
    this.totalNumberOfViolentSanctions!!.toInt(),
    weightingSNSVVATP,
    weightingSNSV,
  )

  private fun snvsStaticSum(request: SNSVStaticRequestValidated): Double = listOf(
    get2YearInterceptWeight(false),
    getGenderWeight(request.gender, false),
    getAgeGenderPolynomialWeight(request.gender, request.dateOfBirth, request.dateAtStartOfFollowup, false),
    request.snsvWeighting,
    getNumberOfSanctionWeight(request.totalNumberOfSanctionsForAllOffences, false),
    getTotalSanctionWeight(request.totalNumberOfSanctionsForAllOffences, false),

    getSecondSanctionCasesOnlyWeight(
      request.totalNumberOfSanctionsForAllOffences,
      request.gender,
      request.dateOfBirth,
      request.dateOfCurrentConviction,
      request.ageAtFirstSanction,
      false,
    ),
    getMonthsSinceLastSanctionWeight(
      request.supervisionStatus,
      request.dateAtStartOfFollowup,
      request.assessmentDate,
      false,
    ),
    getThreePlusSanctionsWeight(
      request.gender,
      request.totalNumberOfSanctionsForAllOffences,
      request.ageAtFirstSanction,
      request.dateOfBirth,
      request.dateOfCurrentConviction,
      false,
    ),
    getViolentHistoryWeight(request.totalNumberOfViolentSanctions, request.gender, false),
    getViolentSanctionsWeight(request.totalNumberOfViolentSanctions, false), //
    getViolenceRateWeight(
      request.dateOfBirth,
      request.dateOfCurrentConviction,
      request.ageAtFirstSanction,
      request.totalNumberOfViolentSanctions,
      false,
    ), //
    request.snsvvatpWeighting,
  ).sum()

  private fun snvsDynamicSum(request: SNSVDynamicRequestValidated): Double = listOf(
    get2YearInterceptWeight(true),
    getGenderWeight(request.gender, true),
    getAgeGenderPolynomialWeight(request.gender, request.dateOfBirth, request.dateAtStartOfFollowup, true),
    request.snsvWeighting,
    getNumberOfSanctionWeight(request.totalNumberOfSanctionsForAllOffences, true),
    getTotalSanctionWeight(request.totalNumberOfSanctionsForAllOffences, true),
    getSecondSanctionCasesOnlyWeight(
      request.totalNumberOfSanctionsForAllOffences,
      request.gender,
      request.dateOfBirth,
      request.dateOfCurrentConviction,
      request.ageAtFirstSanction,
      true,
    ),
    getMonthsSinceLastSanctionWeight(
      request.supervisionStatus,
      request.dateAtStartOfFollowup,
      request.assessmentDate,
      true,
    ),
    getThreePlusSanctionsWeight(
      request.gender,
      request.totalNumberOfSanctionsForAllOffences,
      request.ageAtFirstSanction,
      request.dateOfBirth,
      request.dateOfCurrentConviction,
      true,
    ),
    getViolentHistoryWeight(request.totalNumberOfViolentSanctions, request.gender, true),
    getViolentSanctionsWeight(request.totalNumberOfViolentSanctions, true),
    getViolenceRateWeight(
      request.dateOfBirth,
      request.dateOfCurrentConviction,
      request.ageAtFirstSanction,
      request.totalNumberOfViolentSanctions,
      true,
    ),
    request.snsvvatpWeighting,
    // Dynamic Additions
    didOffenceInvolveCarryingOrUsingWeaponWeight(request.didOffenceInvolveCarryingOrUsingWeapon),
    suitabilityOfAccommodationWeight(request.suitabilityOfAccommodation),
    isUnemployedWeight(request.isUnemployed),
    currentRelationshipWithPartnerWeight(request.currentRelationshipWithPartner),
    chronicDrinkingProblemsWeight(request.currentAlcoholUseProblems),
    bingeDrinkingProblemWeight(request.excessiveAlcoholUse),
    impulsivityProblemsWeight(request.impulsivityProblems),
    temperControlWeight(request.temperControl),
    proCriminalAttitudesWeight(request.proCriminalAttitudes),
    domesticViolenceWeight(request.domesticViolencePerpetrator),
    previousConvictionsWeight(request.previousConvictions),
  ).sum()
}
