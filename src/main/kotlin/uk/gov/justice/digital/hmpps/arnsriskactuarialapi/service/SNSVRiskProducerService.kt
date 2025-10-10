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
    snsvDynamicValidation(request, errors)
    return when (scoreType) {
      ScoreType.STATIC -> snvsStaticSum(request.toSNSVStaticRequestValidated(weightingSNSVVATP!!, weightingSNSV!!))
      ScoreType.DYNAMIC -> snvsDynamicSum(request.toSNSVDynamicRequestValidated(weightingSNSVVATP!!, weightingSNSV!!))
    }.let { SNSVObject(it.first.sigmoid().roundToNDecimals(16), scoreType, errors, it.second) }
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

  private fun snvsStaticSum(request: SNSVStaticRequestValidated): Pair<Double, Map<String, String>> {
    val get2YearInterceptWeight = get2YearInterceptWeight(false)
    val genderWeight = getGenderWeight(request.gender, false)
    val ageGenderPolynomialWeight =
      getAgeGenderPolynomialWeight(request.gender, request.dateOfBirth, request.dateAtStartOfFollowup, false)
    val snsvWeighting = request.snsvWeighting
    val numberOfSanctionWeight = getNumberOfSanctionWeight(request.totalNumberOfSanctionsForAllOffences, false)
    val totalSanctionWeight = getTotalSanctionWeight(request.totalNumberOfSanctionsForAllOffences, false)
    val secondSanctionCasesOnlyWeight = getSecondSanctionCasesOnlyWeight(
      request.totalNumberOfSanctionsForAllOffences,
      request.gender,
      request.dateOfBirth,
      request.dateOfCurrentConviction,
      request.ageAtFirstSanction,
      false,
    )
    val monthsSinceLastSanctionWeight = getMonthsSinceLastSanctionWeight(
      request.supervisionStatus,
      request.dateAtStartOfFollowup,
      request.assessmentDate,
      false,
    )
    val threePlusSanctionsWeight = getThreePlusSanctionsWeight(
      request.gender,
      request.totalNumberOfSanctionsForAllOffences,
      request.ageAtFirstSanction,
      request.dateOfBirth,
      request.dateOfCurrentConviction,
      false,
    )
    val violentHistoryWeight = getViolentHistoryWeight(request.totalNumberOfViolentSanctions, request.gender, false)
    val violentSanctionsWeight = getViolentSanctionsWeight(request.totalNumberOfViolentSanctions, false)
    val violenceRateWeight = getViolenceRateWeight(
      request.dateOfBirth,
      request.dateOfCurrentConviction,
      request.ageAtFirstSanction,
      request.totalNumberOfViolentSanctions,
      false,
    )
    val snsvvatpWeighting = request.snsvvatpWeighting

    val sum = listOf(
      get2YearInterceptWeight,
      genderWeight,
      ageGenderPolynomialWeight,
      snsvWeighting,
      numberOfSanctionWeight,
      totalSanctionWeight,
      secondSanctionCasesOnlyWeight,
      monthsSinceLastSanctionWeight,
      threePlusSanctionsWeight,
      violentHistoryWeight,
      violentSanctionsWeight,
      violenceRateWeight,
      snsvvatpWeighting,
    ).sum()

    return Pair(
      sum,
      mapOf(
        FeatureValue.TWO_YEAR_INTERCEPT_WEIGHT.asPair(get2YearInterceptWeight.toString()),
        FeatureValue.GENDER_WEIGHT.asPair(genderWeight.toString()),
        FeatureValue.AGE_GENDER_POLYNOMIAL_WEIGHT.asPair(ageGenderPolynomialWeight.toString()),
        FeatureValue.SNSV_WEIGHT.asPair(snsvWeighting.toString()),
        FeatureValue.SECOND_SANCTION_CASES_ONLY_WEIGHT.asPair(secondSanctionCasesOnlyWeight.toString()),
        FeatureValue.MONTHS_SINCE_LAST_SANCTION_WEIGHT.asPair(monthsSinceLastSanctionWeight.toString()),
        FeatureValue.THREE_PLUS_SANCTIONS_WEIGHT.asPair(threePlusSanctionsWeight.toString()),
        FeatureValue.VIOLENT_HISTORY_WEIGHT.asPair(violentHistoryWeight.toString()),
        FeatureValue.VIOLENT_SANCTIONS_WEIGHT.asPair(violentSanctionsWeight.toString()),
        FeatureValue.VIOLENCE_RATE_WEIGHT.asPair(violenceRateWeight.toString()),
        FeatureValue.SNSV_VATP_WEIGHT.asPair(snsvvatpWeighting.toString()),
        // Looking at where this comes from it states OGRS3 so i've used the same FeatureValue enum
        FeatureValue.TOTAL_NUMBER_OF_SANCTIONS_FOR_ALL_OFFENCES_WEIGHT.asPair(totalSanctionWeight.toString()),
        FeatureValue.NUMBER_OF_SANCTIONS_WEIGHT.asPair(numberOfSanctionWeight.toString()),
      ),
    )
  }

  private fun snvsDynamicSum(request: SNSVDynamicRequestValidated): Pair<Double, Map<String, String>> {
    val get2YearInterceptWeight = get2YearInterceptWeight(true)
    val genderWeight = getGenderWeight(request.gender, true)
    val ageGenderPolynomialWeight =
      getAgeGenderPolynomialWeight(request.gender, request.dateOfBirth, request.dateAtStartOfFollowup, true)
    val snsvWeighting = request.snsvWeighting
    val numberOfSanctionWeight = getNumberOfSanctionWeight(request.totalNumberOfSanctionsForAllOffences, true)
    val totalSanctionWeight = getTotalSanctionWeight(request.totalNumberOfSanctionsForAllOffences, true)
    val secondSanctionCasesOnlyWeight = getSecondSanctionCasesOnlyWeight(
      request.totalNumberOfSanctionsForAllOffences,
      request.gender,
      request.dateOfBirth,
      request.dateOfCurrentConviction,
      request.ageAtFirstSanction,
      true,
    )
    val monthsSinceLastSanctionWeight = getMonthsSinceLastSanctionWeight(
      request.supervisionStatus,
      request.dateAtStartOfFollowup,
      request.assessmentDate,
      true,
    )
    val threePlusSanctionsWeight = getThreePlusSanctionsWeight(
      request.gender,
      request.totalNumberOfSanctionsForAllOffences,
      request.ageAtFirstSanction,
      request.dateOfBirth,
      request.dateOfCurrentConviction,
      true,
    )
    val violenceRateWeight = getViolenceRateWeight(
      request.dateOfBirth,
      request.dateOfCurrentConviction,
      request.ageAtFirstSanction,
      request.totalNumberOfViolentSanctions,
      true,
    )
    val violentHistoryWeight = getViolentHistoryWeight(request.totalNumberOfViolentSanctions, request.gender, true)
    val violentSanctionsWeight = getViolentSanctionsWeight(request.totalNumberOfViolentSanctions, true)
    val snsvvatpWeighting = request.snsvvatpWeighting
    val offenceInvolveCarryingOrUsingWeaponWeight = didOffenceInvolveCarryingOrUsingWeaponWeight(request.didOffenceInvolveCarryingOrUsingWeapon)
    val suitabilityOfAccommodationWeight = suitabilityOfAccommodationWeight(request.suitabilityOfAccommodation)
    val unemployedWeight = isUnemployedWeight(request.isUnemployed)
    val currentRelationshipWithPartnerWeight = currentRelationshipWithPartnerWeight(request.currentRelationshipWithPartner)
    val chronicDrinkingProblemsWeight = chronicDrinkingProblemsWeight(request.currentAlcoholUseProblems)
    val bingeDrinkingProblemWeight = bingeDrinkingProblemWeight(request.excessiveAlcoholUse)
    val impulsivityProblemsWeight = impulsivityProblemsWeight(request.impulsivityProblems)
    val temperControlWeight = temperControlWeight(request.temperControl)
    val proCriminalAttitudesWeight = proCriminalAttitudesWeight(request.proCriminalAttitudes)
    val domesticViolenceWeight = domesticViolenceWeight(request.domesticViolencePerpetrator)
    val previousConvictionsWeight = previousConvictionsWeight(request.previousConvictions)
    val sum = listOf(
      get2YearInterceptWeight,
      genderWeight,
      ageGenderPolynomialWeight,
      snsvWeighting,
      numberOfSanctionWeight,
      totalSanctionWeight,
      secondSanctionCasesOnlyWeight,
      monthsSinceLastSanctionWeight,
      threePlusSanctionsWeight,
      violentHistoryWeight,
      violentSanctionsWeight,
      violenceRateWeight,
      snsvvatpWeighting,
      // Dynamic Additions
      offenceInvolveCarryingOrUsingWeaponWeight,
      suitabilityOfAccommodationWeight,
      unemployedWeight,
      currentRelationshipWithPartnerWeight,
      chronicDrinkingProblemsWeight,
      bingeDrinkingProblemWeight,
      impulsivityProblemsWeight,
      temperControlWeight,
      proCriminalAttitudesWeight,
      domesticViolenceWeight,
      previousConvictionsWeight,
    ).sum()
    return Pair(
      sum,
      mapOf(
        FeatureValue.TWO_YEAR_INTERCEPT_WEIGHT.asPair(get2YearInterceptWeight.toString()),
        FeatureValue.GENDER_WEIGHT.asPair(genderWeight.toString()),
        FeatureValue.AGE_GENDER_POLYNOMIAL_WEIGHT.asPair(ageGenderPolynomialWeight.toString()),
        FeatureValue.SNSV_WEIGHT.asPair(snsvWeighting.toString()),
        FeatureValue.SECOND_SANCTION_CASES_ONLY_WEIGHT.asPair(secondSanctionCasesOnlyWeight.toString()),
        FeatureValue.MONTHS_SINCE_LAST_SANCTION_WEIGHT.asPair(monthsSinceLastSanctionWeight.toString()),
        FeatureValue.THREE_PLUS_SANCTIONS_WEIGHT.asPair(threePlusSanctionsWeight.toString()),
        FeatureValue.VIOLENT_HISTORY_WEIGHT.asPair(violentHistoryWeight.toString()),
        FeatureValue.VIOLENT_SANCTIONS_WEIGHT.asPair(violentSanctionsWeight.toString()),
        FeatureValue.VIOLENCE_RATE_WEIGHT.asPair(violenceRateWeight.toString()),
        FeatureValue.SNSV_VATP_WEIGHT.asPair(snsvvatpWeighting.toString()),
        // Looking at where this comes from it states OGRS3 so i've used the same FeatureValue enum
        FeatureValue.TOTAL_NUMBER_OF_SANCTIONS_FOR_ALL_OFFENCES_WEIGHT.asPair(totalSanctionWeight.toString()),
        FeatureValue.NUMBER_OF_SANCTIONS_WEIGHT.asPair(numberOfSanctionWeight.toString()),
        FeatureValue.OFFENCE_INVOLVE_CARRYING_OR_USING_WEAPONS_WEIGHT.asPair(offenceInvolveCarryingOrUsingWeaponWeight.toString()),
        FeatureValue.SUITABILITY_OF_ACCOMMODATION_WEIGHT.asPair(suitabilityOfAccommodationWeight.toString()),
        FeatureValue.UNEMPLOYED_WEIGHT.asPair(unemployedWeight.toString()),
        FeatureValue.CURRENT_RELATIONSHIP_WITH_PARTNER.asPair(currentRelationshipWithPartnerWeight.toString()),
        FeatureValue.CHRONIC_DRINKING_PROBLEMS_WEIGHT.asPair(chronicDrinkingProblemsWeight.toString()),
        FeatureValue.BINGE_DRINKING_PROBLEMS_WEIGHT.asPair(bingeDrinkingProblemWeight.toString()),
        FeatureValue.IMPULSIVITY_PROBLEMS_WEIGHT.asPair(impulsivityProblemsWeight.toString()),
        FeatureValue.TEMPER_CONTROL_WEIGHT.asPair(temperControlWeight.toString()),
        FeatureValue.PRO_CRIMINAL_ATTITUDES_WEIGHT.asPair(proCriminalAttitudesWeight.toString()),
        FeatureValue.DOMESTIC_VIOLENCE_WEIGHT.asPair(domesticViolenceWeight.toString()),
        FeatureValue.PREVIOUS_CONVICTIONS_WEIGHT.asPair(previousConvictionsWeight.toString()),
      ),
    )
  }
}
