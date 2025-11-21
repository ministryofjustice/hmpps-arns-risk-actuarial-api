package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.SNSVObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.SNSVRequestValidated
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
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getNumberOfSanctionsWeight
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
  lateinit var offenceCodeCacheService: OffenceCodeCacheService

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val errors = validateSNSV(request)
    if (errors.isNotEmpty()) {
      return applyErrorsToContextAndReturn(context, errors)
    }
    return context.apply {
      SNSV =
        run {
          val scoreType = getSNSVScoreType(request)
          getSNSVObject(scoreType, request)
        }
    }
  }

  override fun applyErrorsToContextAndReturn(
    context: RiskScoreContext,
    validationErrorResponses: List<ValidationErrorResponse>,
  ): RiskScoreContext = context.apply { SNSV = SNSVObject(null, null, validationErrorResponses, null) }

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
      return SNSVObject(null, scoreType, errors, null)
    }

    if (scoreType == ScoreType.STATIC_WITH_VALIDATION_ERRORS) {
      snsvDynamicValidation(request, errors)
    }
    return snsvSum(request.toSNSVRequestValidated(weightingSNSVVATP!!, weightingSNSV!!, scoreType))
      .let { SNSVObject(it.first.sigmoid().roundToNDecimals(16), scoreType, errors, it.second) }
  }

  private fun retrieveWeightingSNSV(scoreType: ScoreType, request: RiskScoreRequest): Double? = when (scoreType) {
    ScoreType.STATIC, ScoreType.STATIC_WITH_VALIDATION_ERRORS -> offenceCodeCacheService.getSnsvStaticWeightingValue(request.currentOffenceCode!!)
    ScoreType.DYNAMIC -> offenceCodeCacheService.getSnsvDynamicWeightingValue(request.currentOffenceCode!!)
  }

  private fun retrieveWeightingSNSVVATP(scoreType: ScoreType, request: RiskScoreRequest): Double? = when (scoreType) {
    ScoreType.STATIC, ScoreType.STATIC_WITH_VALIDATION_ERRORS -> offenceCodeCacheService.getSnsvVatpStaticWeightingValue(request.currentOffenceCode!!)
    ScoreType.DYNAMIC -> offenceCodeCacheService.getSnsvVatpDynamicWeightingValue(request.currentOffenceCode!!)
  }

  fun getSNSVScoreType(request: RiskScoreRequest): ScoreType = if (request.snsvStaticOrDynamic == StaticOrDynamic.STATIC) {
    ScoreType.STATIC
  } else if (request.snsvStaticOrDynamic == StaticOrDynamic.DYNAMIC || isValidDynamicSnsv(request)) {
    ScoreType.DYNAMIC
  } else {
    ScoreType.STATIC_WITH_VALIDATION_ERRORS
  }

  private fun RiskScoreRequest.toSNSVRequestValidated(weightingSNSVVATP: Double, weightingSNSV: Double, scoreType: ScoreType): SNSVRequestValidated = SNSVRequestValidated(
    scoreType,
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
    this.didOffenceInvolveCarryingOrUsingWeapon,
    this.suitabilityOfAccommodation,
    this.isUnemployed,
    this.currentRelationshipWithPartner,
    this.currentAlcoholUseProblems,
    this.excessiveAlcoholUse,
    this.impulsivityProblems,
    this.temperControl,
    this.proCriminalAttitudes,
    getDomesticViolencePerpetrator(this.evidenceOfDomesticAbuse, this.domesticAbuseAgainstPartner),
    this.previousConvictions,
  )

  private fun snsvSum(request: SNSVRequestValidated): Pair<Double, Map<String, Double>> {
    val isSNSVDynamic = request.scoreType == ScoreType.DYNAMIC

    val staticMap = mapOf(
      FeatureValue.TWO_YEAR_INTERCEPT_WEIGHT.asPair(get2YearInterceptWeight(isSNSVDynamic)),
      FeatureValue.GENDER_WEIGHT.asPair(getGenderWeight(request.gender, isSNSVDynamic)),
      FeatureValue.AGE_GENDER_POLYNOMIAL_WEIGHT.asPair(getAgeGenderPolynomialWeight(request.gender, request.dateOfBirth, request.dateAtStartOfFollowup, isSNSVDynamic)),
      FeatureValue.SNSV_WEIGHT.asPair(request.snsvWeighting),
      FeatureValue.SECOND_SANCTION_CASES_ONLY_WEIGHT.asPair(
        getSecondSanctionCasesOnlyWeight(
          request.totalNumberOfSanctionsForAllOffences,
          request.gender,
          request.dateOfBirth,
          request.dateOfCurrentConviction,
          request.ageAtFirstSanction,
          isSNSVDynamic,
        ),
      ),
      FeatureValue.MONTHS_SINCE_LAST_SANCTION_WEIGHT.asPair(
        getMonthsSinceLastSanctionWeight(
          request.dateAtStartOfFollowup,
          request.assessmentDate,
          isSNSVDynamic,
        ),
      ),
      FeatureValue.THREE_PLUS_SANCTIONS_WEIGHT.asPair(
        getThreePlusSanctionsWeight(
          request.gender,
          request.totalNumberOfSanctionsForAllOffences,
          request.ageAtFirstSanction,
          request.dateOfBirth,
          request.dateOfCurrentConviction,
          isSNSVDynamic,
        ),
      ),
      FeatureValue.VIOLENT_HISTORY_WEIGHT.asPair(getViolentHistoryWeight(request.totalNumberOfViolentSanctions, request.gender, isSNSVDynamic)),
      FeatureValue.VIOLENT_SANCTIONS_WEIGHT.asPair(getViolentSanctionsWeight(request.totalNumberOfViolentSanctions, isSNSVDynamic)),
      // TODO: Remove this rounding in ACT-224 - Added due to floating point precision differences across envs
      FeatureValue.VIOLENCE_RATE_WEIGHT.asPair(
        getViolenceRateWeight(
          request.dateOfBirth,
          request.dateOfCurrentConviction,
          request.ageAtFirstSanction,
          request.totalNumberOfViolentSanctions,
          isSNSVDynamic,
        ).roundToNDecimals(16),
      ),
      FeatureValue.SNSV_VATP_WEIGHT.asPair(request.snsvvatpWeighting),
      FeatureValue.TOTAL_NUMBER_OF_SANCTIONS_FOR_ALL_OFFENCES_WEIGHT.asPair(getTotalSanctionWeight(request.totalNumberOfSanctionsForAllOffences, isSNSVDynamic)),
      FeatureValue.NUMBER_OF_SANCTIONS_WEIGHT.asPair(getNumberOfSanctionsWeight(request.totalNumberOfSanctionsForAllOffences, isSNSVDynamic)),
    )

    val dynamicMap = if (isSNSVDynamic) {
      mapOf(
        FeatureValue.OFFENCE_INVOLVE_CARRYING_OR_USING_WEAPONS_WEIGHT.asPair(didOffenceInvolveCarryingOrUsingWeaponWeight(request.didOffenceInvolveCarryingOrUsingWeapon!!)),
        FeatureValue.SUITABILITY_OF_ACCOMMODATION_WEIGHT.asPair(suitabilityOfAccommodationWeight(request.suitabilityOfAccommodation!!)),
        FeatureValue.UNEMPLOYED_WEIGHT.asPair(isUnemployedWeight(request.isUnemployed!!)),
        FeatureValue.CURRENT_RELATIONSHIP_WITH_PARTNER.asPair(currentRelationshipWithPartnerWeight(request.currentRelationshipWithPartner!!)),
        FeatureValue.CHRONIC_DRINKING_PROBLEMS_WEIGHT.asPair(chronicDrinkingProblemsWeight(request.currentAlcoholUseProblems!!)),
        FeatureValue.BINGE_DRINKING_PROBLEMS_WEIGHT.asPair(bingeDrinkingProblemWeight(request.excessiveAlcoholUse!!)),
        FeatureValue.IMPULSIVITY_PROBLEMS_WEIGHT.asPair(impulsivityProblemsWeight(request.impulsivityProblems!!)),
        FeatureValue.TEMPER_CONTROL_WEIGHT.asPair(temperControlWeight(request.temperControl!!)),
        FeatureValue.PRO_CRIMINAL_ATTITUDES_WEIGHT.asPair(proCriminalAttitudesWeight(request.proCriminalAttitudes!!)),
        FeatureValue.DOMESTIC_VIOLENCE_WEIGHT.asPair(domesticViolenceWeight(request.domesticViolencePerpetrator!!)),
        FeatureValue.PREVIOUS_CONVICTIONS_WEIGHT.asPair(previousConvictionsWeight(request.previousConvictions!!)),
      )
    } else {
      mapOf()
    }

    val combinedMap = staticMap + dynamicMap
    val sum = combinedMap.values.sum()
    return Pair(sum, combinedMap)
  }
}
