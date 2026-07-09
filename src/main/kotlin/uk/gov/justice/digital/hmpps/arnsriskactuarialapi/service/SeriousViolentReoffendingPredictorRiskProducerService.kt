package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.seriousviolentreoffendingpredictor.SeriousViolentReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.seriousviolentreoffendingpredictor.SeriousViolentReoffendingPredictorRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.calculatePercentageScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.get2YearInterceptWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getAgeGenderPolynomialWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getChronicDrinkingWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getCopasWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getFirstSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getGapBetweenFirstAndSecondSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getGenderWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getNeverViolentHistoryWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getOffenceFreeMonthsPolynomialWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getOffenceGroupWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getOffenceInvolvedCarryingOrUsingWeaponWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getOnceViolentHistoryWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getPastAggravatedBurglaryOffenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getPastArsonOffenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getPastCriminalDamageOffenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getPastFirearmsOffenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getPastHomicideOffenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getPastKidnappingOffenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getPastNonFirearmWeaponOffenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getPastRobberyOffenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getPastWoundingGrievousBodilyHarmOffenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getProCriminalAttitudeWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getRiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getSecondSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getSuitableAccommodationWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getTemperControlWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getTotalSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getUnemployedWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getViolenceRateWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getViolentSanctionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.SeriousViolentReoffendingPredictorValidator
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.getAgeAtDate
import java.math.BigDecimal

@Service
class SeriousViolentReoffendingPredictorRiskProducerService(val validator: SeriousViolentReoffendingPredictorValidator, val offenceCodeCacheService: OffenceCodeCacheService) : BaseRiskScoreProducer() {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val staticValidationErrors = validator.validateStatic(request)
    val dynamicValidationErrors = validator.validateDynamic(request)

    if (staticValidationErrors.isNotEmpty()) {
      return applyErrorsToContext(context, staticValidationErrors + dynamicValidationErrors)
    }

    val validStaticRequest = SeriousViolentReoffendingPredictorRequestValidated.Static(
      request.assessmentDate,
      request.dateOfBirth!!,
      request.dateOfCurrentConviction!!,
      request.ageAtFirstSanction!!,
      request.gender!!,
      request.currentOffenceCode!!,
      request.totalNumberOfSanctionsForAllOffences!!,
      request.totalNumberOfViolentSanctions!!,
      request.dateAtStartOfFollowupCalculated ?: request.dateOfCurrentConviction,
    )

    if (dynamicValidationErrors.isNotEmpty()) {
      return context.apply {
        seriousViolentReoffendingPredictor =
          calculateAndBuildPredictor(validStaticRequest, staticValidationErrors + dynamicValidationErrors)
      }
    }

    val validDynamicRequest = SeriousViolentReoffendingPredictorRequestValidated.Dynamic(
      validStaticRequest,
      request.didOffenceInvolveCarryingOrUsingWeapon!!,
      request.suitabilityOfAccommodation!!,
      request.isUnemployed!!,
      request.currentAlcoholUseProblems!!,
      request.temperControl!!,
      request.proCriminalAttitudes!!,
      request.previousConvictions!!,
    )

    return context.apply {
      seriousViolentReoffendingPredictor =
        calculateAndBuildPredictor(validDynamicRequest, staticValidationErrors + dynamicValidationErrors)
    }
  }

  override fun applyErrorsToContext(
    context: RiskScoreContext,
    validationErrors: List<ValidationError>,
  ): RiskScoreContext = context.apply {
    seriousViolentReoffendingPredictor = SeriousViolentReoffendingPredictorObject(
      null,
      null,
      null,
      validationErrors,
      null,
    )
  }

  private fun calculateAndBuildPredictor(
    request: SeriousViolentReoffendingPredictorRequestValidated,
    validationErrors: List<ValidationError>,
  ): SeriousViolentReoffendingPredictorObject {
    val staticOrDynamic: StaticOrDynamic = when (request) {
      is SeriousViolentReoffendingPredictorRequestValidated.Static -> StaticOrDynamic.STATIC
      is SeriousViolentReoffendingPredictorRequestValidated.Dynamic -> StaticOrDynamic.DYNAMIC
    }

    val featureValues = buildFeatureValuesMap(
      staticOrDynamic = staticOrDynamic,
      request = request,
    )

    val score = calculatePercentageScore(featureValues[FeatureValue.TOTAL_WEIGHT.outputName]!!)
    val band = getRiskBand(score)

    return SeriousViolentReoffendingPredictorObject(
      score,
      band,
      staticOrDynamic,
      validationErrors,
      featureValues,
    )
  }

  private fun buildFeatureValuesMap(
    staticOrDynamic: StaticOrDynamic,
    request: SeriousViolentReoffendingPredictorRequestValidated,
  ): Map<String, BigDecimal> {
    val staticData: SeriousViolentReoffendingPredictorRequestValidated.Static = when (request) {
      is SeriousViolentReoffendingPredictorRequestValidated.Static -> request
      is SeriousViolentReoffendingPredictorRequestValidated.Dynamic -> request.staticData
    }

    val ageAtCurrentSanction =
      getAgeAtDate(staticData.dateOfBirth, staticData.dateOfCurrentConviction, "dateOfCurrentConviction")
    val ageAtStartOfFollowup = getAgeAtDate(
      staticData.dateOfBirth,
      staticData.dateAtStartOfFollowupCalculated,
      "Date at start of followup calculated",
    )

    val featureValuesMap =  buildMap {
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
      FeatureValue.OFFENCE_GROUP_WEIGHT.set(getOffenceGroupWeight(offenceCodeCacheService, staticOrDynamic, staticData.currentOffenceCode))
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
      FeatureValue.NEVER_VIOLENT_WEIGHT.set(
        getNeverViolentHistoryWeight(
          staticOrDynamic,
          staticData.totalNumberOfViolentSanctions,
          staticData.gender,
        ),
      )
      FeatureValue.ONCE_VIOLENT_WEIGHT.set(
        getOnceViolentHistoryWeight(
          staticOrDynamic,
          staticData.totalNumberOfViolentSanctions,
        ),
      )
      FeatureValue.VIOLENT_SANCTIONS_WEIGHT.set(
        getViolentSanctionsWeight(
          staticOrDynamic,
          staticData.totalNumberOfViolentSanctions,
        ),
      )
      FeatureValue.VIOLENCE_RATE_WEIGHT.set(
        getViolenceRateWeight(
          staticOrDynamic,
          staticData.ageAtFirstSanction,
          staticData.totalNumberOfViolentSanctions,
          ageAtCurrentSanction,
        ),
      )

      if (request is SeriousViolentReoffendingPredictorRequestValidated.Dynamic) {
        FeatureValue.OFFENCE_INVOLVE_CARRYING_OR_USING_WEAPONS_WEIGHT.set(
          getOffenceInvolvedCarryingOrUsingWeaponWeight(
            request.didOffenceInvolveCarryingOrUsingWeapon,
          ),
        )
        FeatureValue.SUITABLE_ACCOMMODATION_WEIGHT.set(getSuitableAccommodationWeight(request.suitabilityOfAccommodation))
        FeatureValue.UNEMPLOYED_WEIGHT.set(getUnemployedWeight(request.isUnemployed))
        FeatureValue.CHRONIC_DRINKING_PROBLEMS_WEIGHT.set(getChronicDrinkingWeight(request.currentAlcoholUseProblems))
        FeatureValue.TEMPER_CONTROL_WEIGHT.set(getTemperControlWeight(request.temperControl))
        FeatureValue.PRO_CRIMINAL_ATTITUDES_WEIGHT.set(getProCriminalAttitudeWeight(request.proCriminalAttitudes))
        FeatureValue.PAST_HOMICIDE_OFFENCE_WEIGHT.set(getPastHomicideOffenceWeight(request.previousConvictions))
        FeatureValue.PAST_WOUNDING_GREIEVOUS_BODILY_HARM_OFFENCE_WEIGHT.set(
          getPastWoundingGrievousBodilyHarmOffenceWeight(request.previousConvictions),
        )
        FeatureValue.PAST_KIDNAPPING_OFFENCE_WEIGHT.set(getPastKidnappingOffenceWeight(request.previousConvictions))
        FeatureValue.PAST_FIREARMS_OFFENCE_WEIGHT.set(getPastFirearmsOffenceWeight(request.previousConvictions))
        FeatureValue.PAST_ROBBERY_OFFENCE_WEIGHT.set(getPastRobberyOffenceWeight(request.previousConvictions))
        FeatureValue.PAST_AGGRAVATED_BURGLARY_OFFENCE_WEIGHT.set(getPastAggravatedBurglaryOffenceWeight(request.previousConvictions))
        FeatureValue.PAST_NON_FIREARM_WEAPON_OFFENCE_WEIGHT.set(getPastNonFirearmWeaponOffenceWeight(request.previousConvictions))
        FeatureValue.PAST_CRIMINAL_DAMAGE_OFFENCE_WEIGHT.set(getPastCriminalDamageOffenceWeight(request.previousConvictions))
        FeatureValue.PAST_ARSON_OFFENCE_WEIGHT.set(getPastArsonOffenceWeight(request.previousConvictions))
      }

      val totalWeight = values.fold(BigDecimal.ZERO, BigDecimal::add)
      FeatureValue.TOTAL_WEIGHT.set(totalWeight)
    }

    log.info("#### Current Feature values = $featureValuesMap")

    return featureValuesMap
  }
}
