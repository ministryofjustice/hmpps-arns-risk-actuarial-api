package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.violentreoffendingpredictor.ViolentReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.violentreoffendingpredictor.ViolentReoffendingPredictorRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.calculatePercentageScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.get2YearInterceptWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getAgeGenderPolynomialWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getBenzodiazepinesUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getBingeDrinkingWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getCannabisUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getChronicDrinkingWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getCopasVWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getCopasViolentOffencesWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getCrackCocaineUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getDomesticViolenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getDrugMotivationWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getFirstSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getGapBetweenFirstAndSecondSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getGenderWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getImpulsivityWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getLiveInRelationshipWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getMethadoneUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getMisusedPrescriptionDrugUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getMultiplicativeRelationshipWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getOffenceFreeMonthsPolynomialWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getOtherDrugsUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getOtherOpiateUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getPowderCocaineUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getRegularOffendingActivitiesWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getRelationshipQualityWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getRiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getSecondSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getSteroidsUsageWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getSuitableAccommodationWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getTemperControlWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getTotalSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getTotalViolentSanctionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ViolentReoffendingPredictorTransformationHelper.getUnemployedWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validateViolentReoffendingPredictorDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validateViolentReoffendingPredictorStatic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.getAgeAtDate
import java.math.BigDecimal

@Service
class ViolentReoffendingPredictorRiskProducerService : BaseRiskScoreProducer() {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val staticValidationErrors = validateViolentReoffendingPredictorStatic(request)
    val dynamicValidationErrors = validateViolentReoffendingPredictorDynamic(request)

    if (staticValidationErrors.isNotEmpty()) {
      return applyErrorsToContext(context, staticValidationErrors + dynamicValidationErrors)
    }

    val validStaticRequest = ViolentReoffendingPredictorRequestValidated.Static(
      request.assessmentDate,
      request.dateOfBirth!!,
      request.dateOfCurrentConviction!!,
      request.ageAtFirstSanction!!,
      request.gender!!,
      request.currentOffenceCode!!,
      request.totalNumberOfSanctionsForAllOffences!!,
      request.totalNumberOfViolentSanctions!!,
      request.dateAtStartOfFollowupCalculated!!,
    )

    if (dynamicValidationErrors.isNotEmpty()) {
      return context.apply {
        violentReoffendingPredictor =
          calculateAndBuildPredictor(validStaticRequest, staticValidationErrors + dynamicValidationErrors)
      }
    }

    val validDynamicRequest = ViolentReoffendingPredictorRequestValidated.Dynamic(
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
      request.hasMethadoneUsage!!,
      request.currentAlcoholUseProblems!!,
      request.excessiveAlcoholUse!!,
      request.impulsivityProblems!!,
      request.temperControl!!,
    )

    val test = calculateAndBuildPredictor(validDynamicRequest, staticValidationErrors + dynamicValidationErrors)
    return context.apply {
      violentReoffendingPredictor = test

    }
  }

  override fun applyErrorsToContext(
    context: RiskScoreContext,
    validationErrors: List<ValidationError>,
  ): RiskScoreContext = context.apply {
    violentReoffendingPredictor = ViolentReoffendingPredictorObject(
      null,
      null,
      null,
      validationErrors,
      null,
    )
  }

  private fun calculateAndBuildPredictor(
    request: ViolentReoffendingPredictorRequestValidated,
    validationErrors: List<ValidationError>,
  ): ViolentReoffendingPredictorObject {
    val staticOrDynamic: StaticOrDynamic = when (request) {
      is ViolentReoffendingPredictorRequestValidated.Static -> StaticOrDynamic.STATIC
      is ViolentReoffendingPredictorRequestValidated.Dynamic -> StaticOrDynamic.DYNAMIC
    }

    val featureValues = buildFeatureValuesMap(
      staticOrDynamic = staticOrDynamic,
      request = request,
    )

    val score = calculatePercentageScore(featureValues[FeatureValue.TOTAL_WEIGHT.outputName]!!)
    val band = getRiskBand(score)

    return ViolentReoffendingPredictorObject(
      score,
      band,
      staticOrDynamic,
      validationErrors,
      featureValues,
    )
  }

  private fun buildFeatureValuesMap(
    staticOrDynamic: StaticOrDynamic,
    request: ViolentReoffendingPredictorRequestValidated,
  ): Map<String, BigDecimal> {
    val staticData: ViolentReoffendingPredictorRequestValidated.Static = when (request) {
      is ViolentReoffendingPredictorRequestValidated.Static -> request
      is ViolentReoffendingPredictorRequestValidated.Dynamic -> request.staticData
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
      FeatureValue.TOTAL_NUMBER_OF_VIOLENT_SANCTIONS_WEIGHT.set(
        getTotalViolentSanctionsWeight(
          staticOrDynamic
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
        getCopasVWeight(
          staticOrDynamic,
          staticData.totalNumberOfSanctionsForAllOffences,
          staticData.gender,
          staticData.ageAtFirstSanction,
          ageAtCurrentSanction,
        ),
      )
      FeatureValue.COPAS_VIOLENT_OFFENCES_SCORE.set(
        getCopasViolentOffencesWeight(
          staticOrDynamic,
          staticData.totalNumberOfSanctionsForAllOffences,
          staticData.ageAtFirstSanction,
          ageAtCurrentSanction,
        ),
      )

      if (request is ViolentReoffendingPredictorRequestValidated.Dynamic) {
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
        FeatureValue.REGULAR_OFFENDING_ACTIVITIES.set(getRegularOffendingActivitiesWeight(request.regularOffendingActivities))
        FeatureValue.DRUG_MOTIVATION_WEIGHT.set(getDrugMotivationWeight(request.motivationToTackleDrugMisuse))
        FeatureValue.CHRONIC_DRINKING_PROBLEMS_WEIGHT.set(getChronicDrinkingWeight(request.currentAlcoholUseProblems))
        FeatureValue.BINGE_DRINKING_PROBLEMS_WEIGHT.set(getBingeDrinkingWeight(request.excessiveAlcoholUse))
        FeatureValue.IMPULSIVITY_PROBLEMS_WEIGHT.set(getImpulsivityWeight(request.impulsivityProblems))
        FeatureValue.TEMPER_CONTROL_WEIGHT.set(getTemperControlWeight(request.impulsivityProblems))
        FeatureValue.METHADONE_USAGE_WEIGHT.set(getMethadoneUsageWeight(request.hasHeroinUsage))
        FeatureValue.OTHER_OPIATE_USAGE_WEIGHT.set(getOtherOpiateUsageWeight(request.hasOtherOpiateUsage))
        FeatureValue.CRACK_COCAINE_USAGE_WEIGHT.set(getCrackCocaineUsageWeight(request.hasCrackCocaineUsage))
        FeatureValue.POWDER_COCAINE_USAGE_WEIGHT.set(getPowderCocaineUsageWeight(request.hasPowderCocaineUsage))
        FeatureValue.MISUSED_PRESCRIPTION_DRUG_USAGE_WEIGHT.set(getMisusedPrescriptionDrugUsageWeight(request.hasMisusedPrescriptionDrugUsage))
        FeatureValue.BENZODIAZEPINES_USAGE_WEIGHT.set(getBenzodiazepinesUsageWeight(request.hasBenzodiazepinesUsage))
        FeatureValue.CANNABIS_USAGE_WEIGHT.set(getCannabisUsageWeight(request.hasCannabisUsage))
        FeatureValue.STEROID_USAGE_WEIGHT.set(getSteroidsUsageWeight(request.hasSteroidsUsage))
        FeatureValue.OTHER_DRUG_USAGE_WEIGHT.set(getOtherDrugsUsageWeight(request.hasOtherDrugsUsage))
      }

      val totalWeight = values.fold(BigDecimal.ZERO, BigDecimal::add)
      FeatureValue.TOTAL_WEIGHT.set(totalWeight)
    }
  }
}
