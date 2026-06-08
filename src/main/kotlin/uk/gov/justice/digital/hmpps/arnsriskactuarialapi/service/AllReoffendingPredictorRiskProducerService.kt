package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.allreoffendingpredictor.AllReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.allreoffendingpredictor.AllReoffendingPredictorRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.calculateTwoYearPercentageScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validateAllReoffendingPredictorStatic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validateAllReoffendingPredictorDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getAgeGenderPolynomial
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getBenzodiazepinesUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getBingeDrinkingWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getCannabisUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getChronicDrinkingWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getCopasScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getCopasScoreSquared
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getCrackCocaineUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getCriminalAttitudeWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getDomesticViolenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getDrugMotivationWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getFemaleWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getFirstSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getGapBetweenFirstAndSecondSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getHeroinUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getImpulsivityWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getLiveInRelationshipWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getMisusedPrescriptionDrugUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getMultiplicativeRelationshipWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getOffenceFreeMonthsPolynomial
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getOtherDrugsUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getOtherOpiateUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getPowderCocaineUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getRegularOffendingActivitiesWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getRelationshipQualityWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getRiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getSecondSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getSteroidsUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getSuitableAccommodationWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getTotalSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getUnemployedWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getYearScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.getAgeAtDate
import java.math.BigDecimal

@Service
class AllReoffendingPredictorRiskProducerService : BaseRiskScoreProducer() {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {

    val staticErrors = validateAllReoffendingPredictorStatic(request)

    if (staticErrors.isNotEmpty()) {
      return applyErrorsToContextAndReturn(context, staticErrors)
    }

    val validStaticRequest = AllReoffendingPredictorRequestValidated.Static(
      request.assessmentDate,
      request.dateOfBirth!!,
      request.dateOfCurrentConviction!!,
      request.ageAtFirstSanction!!,
      request.gender!!,
      request.currentOffenceCode!!,
      request.totalNumberOfSanctionsForAllOffences!!,
      request.dateAtStartOfFollowupCalculated!!,
    )

    val dynamicErrors = validateAllReoffendingPredictorDynamic(request)

    if (dynamicErrors.isNotEmpty()) {
      context.apply { AllReoffendingPredictor = getAllReoffendingPredictorObject(validStaticRequest) }

      return applyErrorsToContextAndReturn(context, dynamicErrors)
    }

    val validDynamicRequest = AllReoffendingPredictorRequestValidated.Dynamic(
      validStaticRequest,
      request.suitabilityOfAccommodation!!,
      request.isUnemployed!!,
      request.currentRelationshipWithPartner!!,
      request.evidenceOfDomesticAbuse!!,
      request.currentRelationshipStatus!!,
      request.regularOffendingActivities!!,
      request.motivationToTackleDrugMisuse!!,
      request.hasHeroinUsage!!,
      request.hasOtherOpiateUsage!!,
      request.hasCrackCocaineUsage!!,
      request.hasPowderCocaineUsage!!,
      request.hasMisusedPrescriptionDrugUsage!!,
      request.hasBenzodiazepinesUsage!!,
      request.hasCannabisUsage!!,
      request.hasSteroidsUsage!!,
      request.hasOtherDrugsUsage!!,
      request.hasKetamineUsage!!,
      request.hasSpiceUsage!!,
      request.hasHallucinogensUsage!!,
      request.hasSolventsUsage!!,
      request.currentAlcoholUseProblems!!,
      request.excessiveAlcoholUse!!,
      request.impulsivityProblems!!,
      request.proCriminalAttitudes!!,
    )

    return context.apply { AllReoffendingPredictor = getAllReoffendingPredictorObject(validDynamicRequest) }
  }

  private fun getAllReoffendingPredictorObject(request: AllReoffendingPredictorRequestValidated): AllReoffendingPredictorObject {

    val staticOrDynamic: StaticOrDynamic
    val staticData: AllReoffendingPredictorRequestValidated.Static

    when (request) {
      is AllReoffendingPredictorRequestValidated.Static -> {
        staticOrDynamic = StaticOrDynamic.STATIC
        staticData = request
      }

      is AllReoffendingPredictorRequestValidated.Dynamic -> {
        staticOrDynamic = StaticOrDynamic.DYNAMIC
        staticData = request.staticData
      }
    }

    val featureValues = mutableMapOf<String, BigDecimal>()

    val ageAtCurrentSanction =
      getAgeAtDate(staticData.dateOfBirth, staticData.dateOfCurrentConviction, "dateOfCurrentConviction")
    val ageAtStartOfFollowup = getAgeAtDate(
      staticData.dateOfBirth,
      staticData.dateAtStartOfFollowupCalculated,
      "Date at start of followup calculated",
    )

    featureValues[FeatureValue.TWO_YEAR_INTERCEPT_WEIGHT.outputName] = getYearScore(staticOrDynamic)
    featureValues[FeatureValue.AGE_GENDER_POLYNOMIAL_WEIGHT.outputName] =
      getAgeGenderPolynomial(staticOrDynamic, staticData.gender, ageAtStartOfFollowup)
    featureValues[FeatureValue.GENDER_WEIGHT.outputName] = getFemaleWeight(staticOrDynamic, staticData.gender)
    featureValues[FeatureValue.FIRST_SANCTION_WEIGHT.outputName] =
      getFirstSanctionWeight(staticOrDynamic, staticData.totalNumberOfSanctionsForAllOffences)
    featureValues[FeatureValue.SECOND_SANCTION_WEIGHT.outputName] =
      getSecondSanctionWeight(staticOrDynamic, staticData.totalNumberOfSanctionsForAllOffences)
    featureValues[FeatureValue.TOTAL_NUMBER_OF_SANCTIONS_FOR_ALL_OFFENCES_WEIGHT.outputName] =
      getTotalSanctionWeight(staticOrDynamic, staticData.totalNumberOfSanctionsForAllOffences)
    featureValues[FeatureValue.TOTAL_NUMBER_OF_SANCTIONS_FOR_ALL_OFFENCES_WEIGHT.outputName] =
      getGapBetweenFirstAndSecondSanctionWeight(
        staticOrDynamic,
        staticData.gender,
        staticData.ageAtFirstSanction,
        ageAtCurrentSanction,
        staticData.totalNumberOfSanctionsForAllOffences,
      )
    featureValues[FeatureValue.OFFENCE_FREE_MONTHS_WEIGHT.outputName] = getOffenceFreeMonthsPolynomial(
      staticOrDynamic,
      staticData.assessmentDate,
      staticData.dateAtStartOfFollowupCalculated,
    )
    featureValues[FeatureValue.COPAS_SCORE.outputName] = getCopasScore(
      staticOrDynamic,
      staticData.totalNumberOfSanctionsForAllOffences,
      staticData.gender,
      staticData.ageAtFirstSanction,
      ageAtCurrentSanction,
    )
    featureValues[FeatureValue.COPAS_SCORE_SQUARED.outputName] = getCopasScoreSquared(
      staticOrDynamic,
      staticData.totalNumberOfSanctionsForAllOffences,
      staticData.gender,
      staticData.ageAtFirstSanction,
      ageAtCurrentSanction,
    )

    if (request is AllReoffendingPredictorRequestValidated.Dynamic) {
      featureValues[FeatureValue.SUITABLE_ACCOMMODATION_WEIGHT.outputName] =
        getSuitableAccommodationWeight(request.suitabilityOfAccommodation)
      featureValues[FeatureValue.UNEMPLOYED_WEIGHT.outputName] = getUnemployedWeight(request.isUnemployed)
      featureValues[FeatureValue.LIVE_IN_RELATIONSHIP_WEIGHT.outputName] =
        getLiveInRelationshipWeight(request.currentRelationshipStatus)
      featureValues[FeatureValue.RELATIONSHIP_QUALITY_WEIGHT.outputName] =
        getRelationshipQualityWeight(request.currentRelationshipWithPartner)
      featureValues[FeatureValue.MULTIPLICATIVE_RELATIONSHIP_WEIGHT.outputName] =
        getMultiplicativeRelationshipWeight(request.currentRelationshipStatus, request.currentRelationshipWithPartner)
      featureValues[FeatureValue.DOMESTIC_VIOLENCE_WEIGHT.outputName] =
        getDomesticViolenceWeight(request.evidenceOfDomesticAbuse)
      featureValues[FeatureValue.REGULAR_OFFENDING_ACTIVITIES.outputName] =
        getRegularOffendingActivitiesWeight(request.regularOffendingActivities)
      featureValues[FeatureValue.DRUG_MOTIVATION_WEIGHT.outputName] =
        getDrugMotivationWeight(request.motivationToTackleDrugMisuse)
      featureValues[FeatureValue.CHRONIC_DRINKING_PROBLEMS_WEIGHT.outputName] =
        getChronicDrinkingWeight(request.currentAlcoholUseProblems)
      featureValues[FeatureValue.BINGE_DRINKING_PROBLEMS_WEIGHT.outputName] =
        getBingeDrinkingWeight(request.excessiveAlcoholUse)
      featureValues[FeatureValue.IMPULSIVITY_PROBLEMS_WEIGHT.outputName] =
        getImpulsivityWeight(request.impulsivityProblems)
      featureValues[FeatureValue.PRO_CRIMINAL_ATTITUDES_WEIGHT.outputName] =
        getCriminalAttitudeWeight(request.proCriminalAttitudes)
      featureValues[FeatureValue.HEROIN_USAGE_WEIGHT.outputName] = getHeroinUsageWeight(request.hasHeroinUsage)
      featureValues[FeatureValue.OTHER_OPIATE_USAGE_WEIGHT.outputName] =
        getOtherOpiateUsageWeight(request.hasOtherOpiateUsage)
      featureValues[FeatureValue.CRACK_COCAINE_USAGE_WEIGHT.outputName] =
        getCrackCocaineUsageWeight(request.hasCrackCocaineUsage)
      featureValues[FeatureValue.POWDER_COCAINE_USAGE_WEIGHT.outputName] =
        getPowderCocaineUsageWeight(request.hasPowderCocaineUsage)
      featureValues[FeatureValue.MISUSED_PRESCRIPTION_DRUG_USAGE_WEIGHT.outputName] =
        getMisusedPrescriptionDrugUsageWeight(request.hasMisusedPrescriptionDrugUsage)
      featureValues[FeatureValue.BENZODIAZEPINES_USAGE_WEIGHT.outputName] =
        getBenzodiazepinesUsageWeight(request.hasBenzodiazepinesUsage)
      featureValues[FeatureValue.CANNABIS_USAGE_WEIGHT.outputName] = getCannabisUsageWeight(request.hasCannabisUsage)
      featureValues[FeatureValue.STEROID_USAGE_WEIGHT.outputName] = getSteroidsUsageWeight(request.hasSteroidsUsage)
      featureValues[FeatureValue.OTHER_DRUG_USAGE_WEIGHT.outputName] = getOtherDrugsUsageWeight(
        request.hasOtherDrugsUsage,
        request.hasKetamineUsage,
        request.hasSpiceUsage,
        request.hasHallucinogensUsage,
        request.hasSolventsUsage,
      )
    }

    val totalWeight = featureValues.values.fold(BigDecimal.ZERO, BigDecimal::add)
    featureValues[FeatureValue.TOTAL_WEIGHT.outputName] = totalWeight

    val twoYearPercentageScore = calculateTwoYearPercentageScore(totalWeight)
    val band = getRiskBand(twoYearPercentageScore)

    return AllReoffendingPredictorObject(
      twoYearPercentageScore,
      band,
      staticOrDynamic,
      null,
      featureValues,
    )
  }

  override fun applyErrorsToContextAndReturn(
    context: RiskScoreContext,
    validationErrorResponses: List<ValidationErrorResponse>,
  ): RiskScoreContext = context.apply {
    AllReoffendingPredictor?.let { it.validationError = validationErrorResponses }
      ?: run { AllReoffendingPredictor = buildErrorObject(validationErrorResponses) }
  }


  private fun buildErrorObject(validationErrorResponse: List<ValidationErrorResponse>): AllReoffendingPredictorObject =
    AllReoffendingPredictorObject(
      null,
      null,
      null,
      validationErrorResponse,
      null,
    )
}