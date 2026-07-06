package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.MotivationLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.allreoffendingpredictor.AllReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.allreoffendingpredictor.AllReoffendingPredictorRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.calculatePercentageScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.get2YearInterceptWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getAgeGenderPolynomialWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getBenzodiazepinesUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getBingeDrinkingWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getCannabisUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getChronicDrinkingWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getCopasSquaredWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getCopasWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getCrackCocaineUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getDomesticViolenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getDrugMotivationWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getFirstSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getGapBetweenFirstAndSecondSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getGenderWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getHeroinUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getImpulsivityWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getLiveInRelationshipWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getMisusedPrescriptionDrugUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getMultiplicativeRelationshipWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getOffenceFreeMonthsPolynomialWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getOffenceGroupWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getOtherDrugsUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getOtherOpiateUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getPowderCocaineUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getProCriminalAttitudeWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getRegularOffendingActivitiesWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getRelationshipQualityWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getRiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getSecondSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getSteroidsUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getSuitableAccommodationWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getTotalSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.AllReoffendingPredictorTransformationHelper.getUnemployedWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.AllReoffendingPredictorValidator
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.getAgeAtDate
import java.math.BigDecimal

@Service
class AllReoffendingPredictorRiskProducerService(val inputValidator: AllReoffendingPredictorValidator) : BaseRiskScoreProducer() {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val staticValidationErrors = inputValidator.validateStatic(request)
    val dynamicValidationErrors = inputValidator.validateDynamic(request)

    if (staticValidationErrors.isNotEmpty()) {
      return applyErrorsToContext(context, staticValidationErrors + dynamicValidationErrors)
    }

    val validStaticRequest = AllReoffendingPredictorRequestValidated.Static(
      request.assessmentDate,
      request.dateOfBirth!!,
      request.dateOfCurrentConviction!!,
      request.ageAtFirstSanction!!,
      request.gender!!,
      request.currentOffenceCode!!,
      request.totalNumberOfSanctionsForAllOffences!!,
      request.dateAtStartOfFollowupCalculated ?: request.dateOfCurrentConviction,
    )

    if (dynamicValidationErrors.isNotEmpty()) {
      return context.apply {
        allReoffendingPredictor =
          calculateAndBuildPredictor(validStaticRequest, staticValidationErrors + dynamicValidationErrors)
      }
    }

    val validDynamicRequest = AllReoffendingPredictorRequestValidated.Dynamic(
      validStaticRequest,
      request.suitabilityOfAccommodation!!,
      request.isUnemployed!!,
      request.currentRelationshipWithPartner!!,
      request.evidenceOfDomesticAbuse!!,
      request.currentRelationshipStatus!!,
      request.regularOffendingActivities!!,
      // The drug misuse questions are optional - if not answered then set them to FULL_MOTIVATION/false
      // so drug misuse is not factored into the score calculation
      request.motivationToTackleDrugMisuse ?: MotivationLevel.FULL_MOTIVATION,
      request.hasHeroinUsage ?: false,
      request.hasOtherOpiateUsage ?: false,
      request.hasCrackCocaineUsage ?: false,
      request.hasPowderCocaineUsage ?: false,
      request.hasMisusedPrescriptionDrugUsage ?: false,
      request.hasBenzodiazepinesUsage ?: false,
      request.hasCannabisUsage ?: false,
      request.hasSteroidsUsage ?: false,
      request.hasOtherDrugsUsage ?: false,
      request.hasKetamineUsage ?: false,
      request.hasSpiceUsage ?: false,
      request.hasHallucinogensUsage ?: false,
      request.hasSolventsUsage ?: false,
      request.currentAlcoholUseProblems!!,
      request.excessiveAlcoholUse!!,
      request.impulsivityProblems!!,
      request.proCriminalAttitudes!!,
    )

    return context.apply {
      allReoffendingPredictor =
        calculateAndBuildPredictor(validDynamicRequest, staticValidationErrors + dynamicValidationErrors)
    }
  }

  override fun applyErrorsToContext(
    context: RiskScoreContext,
    validationErrors: List<ValidationError>,
  ): RiskScoreContext = context.apply {
    allReoffendingPredictor = AllReoffendingPredictorObject(
      null,
      null,
      null,
      validationErrors,
      null,
    )
  }

  private fun calculateAndBuildPredictor(
    request: AllReoffendingPredictorRequestValidated,
    validationErrors: List<ValidationError>,
  ): AllReoffendingPredictorObject {
    val staticOrDynamic: StaticOrDynamic = when (request) {
      is AllReoffendingPredictorRequestValidated.Static -> StaticOrDynamic.STATIC
      is AllReoffendingPredictorRequestValidated.Dynamic -> StaticOrDynamic.DYNAMIC
    }

    val featureValues = buildFeatureValuesMap(
      staticOrDynamic = staticOrDynamic,
      request = request,
    )

    val score = calculatePercentageScore(featureValues[FeatureValue.TOTAL_WEIGHT.outputName]!!)
    val band = getRiskBand(score)

    return AllReoffendingPredictorObject(
      score,
      band,
      staticOrDynamic,
      validationErrors,
      featureValues,
    )
  }

  private fun buildFeatureValuesMap(
    staticOrDynamic: StaticOrDynamic,
    request: AllReoffendingPredictorRequestValidated,
  ): Map<String, BigDecimal> {
    val staticData: AllReoffendingPredictorRequestValidated.Static = when (request) {
      is AllReoffendingPredictorRequestValidated.Static -> request
      is AllReoffendingPredictorRequestValidated.Dynamic -> request.staticData
    }

    val ageAtCurrentSanction =
      getAgeAtDate(staticData.dateOfBirth, staticData.dateOfCurrentConviction, "dateOfCurrentConviction")
    val ageAtStartOfFollowup = getAgeAtDate(
      staticData.dateOfBirth,
      staticData.dateAtStartOfFollowupCalculated,
      "Date at start of followup calculated",
    )

    return buildMap {
      fun FeatureValue.set(weight: BigDecimal) = put(this.outputName, weight)

      FeatureValue.TWO_YEAR_INTERCEPT_WEIGHT.set(get2YearInterceptWeight(staticOrDynamic))
      FeatureValue.AGE_GENDER_POLYNOMIAL_WEIGHT.set(
        getAgeGenderPolynomialWeight(
          staticOrDynamic,
          staticData.gender,
          ageAtStartOfFollowup,
        ),
      )
      FeatureValue.GENDER_WEIGHT.set(getGenderWeight(staticOrDynamic, staticData.gender))
      FeatureValue.OFFENCE_GROUP_WEIGHT.set(getOffenceGroupWeight(staticOrDynamic, staticData.currentOffenceCode))
      FeatureValue.FIRST_SANCTION_WEIGHT.set(
        getFirstSanctionWeight(
          staticOrDynamic,
          staticData.totalNumberOfSanctionsForAllOffences,
        ),
      )
      FeatureValue.SECOND_SANCTION_WEIGHT.set(
        getSecondSanctionWeight(
          staticOrDynamic,
          staticData.totalNumberOfSanctionsForAllOffences,
        ),
      )
      FeatureValue.TOTAL_NUMBER_OF_SANCTIONS_FOR_ALL_OFFENCES_WEIGHT.set(
        getTotalSanctionWeight(
          staticOrDynamic,
          staticData.totalNumberOfSanctionsForAllOffences,
        ),
      )
      FeatureValue.SECOND_SANCTION_GAP_WEIGHT.set(
        getGapBetweenFirstAndSecondSanctionWeight(
          staticOrDynamic,
          staticData.gender,
          staticData.ageAtFirstSanction,
          ageAtCurrentSanction,
          staticData.totalNumberOfSanctionsForAllOffences,
        ),
      )
      FeatureValue.OFFENCE_FREE_MONTHS_WEIGHT.set(
        getOffenceFreeMonthsPolynomialWeight(
          staticOrDynamic,
          staticData.assessmentDate,
          staticData.dateAtStartOfFollowupCalculated,
        ),
      )
      FeatureValue.COPAS_SCORE.set(
        getCopasWeight(
          staticOrDynamic,
          staticData.totalNumberOfSanctionsForAllOffences,
          staticData.gender,
          staticData.ageAtFirstSanction,
          ageAtCurrentSanction,
        ),
      )
      FeatureValue.COPAS_SCORE_SQUARED.set(
        getCopasSquaredWeight(
          staticOrDynamic,
          staticData.totalNumberOfSanctionsForAllOffences,
          staticData.gender,
          staticData.ageAtFirstSanction,
          ageAtCurrentSanction,
        ),
      )

      if (request is AllReoffendingPredictorRequestValidated.Dynamic) {
        FeatureValue.SUITABLE_ACCOMMODATION_WEIGHT.set(getSuitableAccommodationWeight(request.suitabilityOfAccommodation))
        FeatureValue.UNEMPLOYED_WEIGHT.set(getUnemployedWeight(request.isUnemployed))
        FeatureValue.LIVE_IN_RELATIONSHIP_WEIGHT.set(getLiveInRelationshipWeight(request.currentRelationshipStatus))
        FeatureValue.RELATIONSHIP_QUALITY_WEIGHT.set(getRelationshipQualityWeight(request.currentRelationshipWithPartner))
        FeatureValue.MULTIPLICATIVE_RELATIONSHIP_WEIGHT.set(
          getMultiplicativeRelationshipWeight(
            request.currentRelationshipStatus,
            request.currentRelationshipWithPartner,
          ),
        )
        FeatureValue.DOMESTIC_VIOLENCE_WEIGHT.set(getDomesticViolenceWeight(request.evidenceOfDomesticAbuse))
        FeatureValue.REGULAR_OFFENDING_ACTIVITIES_WEIGHT.set(getRegularOffendingActivitiesWeight(request.regularOffendingActivities))
        FeatureValue.DRUG_MOTIVATION_WEIGHT.set(getDrugMotivationWeight(request.motivationToTackleDrugMisuse))
        FeatureValue.CHRONIC_DRINKING_PROBLEMS_WEIGHT.set(getChronicDrinkingWeight(request.currentAlcoholUseProblems))
        FeatureValue.BINGE_DRINKING_PROBLEMS_WEIGHT.set(getBingeDrinkingWeight(request.excessiveAlcoholUse))
        FeatureValue.IMPULSIVITY_PROBLEMS_WEIGHT.set(getImpulsivityWeight(request.impulsivityProblems))
        FeatureValue.PRO_CRIMINAL_ATTITUDES_WEIGHT.set(getProCriminalAttitudeWeight(request.proCriminalAttitudes))
        FeatureValue.HEROIN_USAGE_WEIGHT.set(getHeroinUsageWeight(request.hasHeroinUsage))
        FeatureValue.OTHER_OPIATE_USAGE_WEIGHT.set(getOtherOpiateUsageWeight(request.hasOtherOpiateUsage))
        FeatureValue.CRACK_COCAINE_USAGE_WEIGHT.set(getCrackCocaineUsageWeight(request.hasCrackCocaineUsage))
        FeatureValue.POWDER_COCAINE_USAGE_WEIGHT.set(getPowderCocaineUsageWeight(request.hasPowderCocaineUsage))
        FeatureValue.MISUSED_PRESCRIPTION_DRUG_USAGE_WEIGHT.set(getMisusedPrescriptionDrugUsageWeight(request.hasMisusedPrescriptionDrugUsage))
        FeatureValue.BENZODIAZEPINES_USAGE_WEIGHT.set(getBenzodiazepinesUsageWeight(request.hasBenzodiazepinesUsage))
        FeatureValue.CANNABIS_USAGE_WEIGHT.set(getCannabisUsageWeight(request.hasCannabisUsage))
        FeatureValue.STEROID_USAGE_WEIGHT.set(getSteroidsUsageWeight(request.hasSteroidsUsage))
        FeatureValue.OTHER_DRUG_USAGE_WEIGHT.set(
          getOtherDrugsUsageWeight(
            request.hasOtherDrugsUsage,
            request.hasKetamineUsage,
            request.hasSpiceUsage,
            request.hasHallucinogensUsage,
            request.hasSolventsUsage,
          ),
        )
      }

      val totalWeight = values.fold(BigDecimal.ZERO, BigDecimal::add)
      FeatureValue.TOTAL_WEIGHT.set(totalWeight)
    }
  }
}
