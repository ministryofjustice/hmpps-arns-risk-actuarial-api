package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.AlgorithmResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.combinedseriousreoffendingpredictor.CombinedSeriousReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.CombinedSeriousReoffendingPredictorTransformationHelper.getBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.CombinedSeriousReoffendingPredictorTransformationHelper.getFemaleWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.CombinedSeriousReoffendingPredictorTransformationHelper.getScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.CombinedSeriousReoffendingPredictorValidator
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.asDoublePercentage

@Service
class CombinedSeriousReoffendingPredictorRiskProducerService(val validator: CombinedSeriousReoffendingPredictorValidator) : BaseRiskScoreProducer() {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val componentErrors = getValidationErrorsFromContext(context)
    val staticValidationErrors = validator.validateStatic(request)

    if (componentErrors.isNotEmpty() || staticValidationErrors.isNotEmpty()) {
      return applyErrorsToContext(
        context,
        listOf(
          ValidationErrorType.COMPONENT_VALIDATION_ERROR.asError(componentErrors),
        ) + staticValidationErrors,
      )
    }

    val directContactSexualReoffendingPredictor = context.directContactSexualReoffendingPredictor
    val imagesAndIndirectContactSexualReoffendingPredictor = context.imagesAndIndirectContactSexualReoffendingPredictor
    val seriousViolentReoffendingPredictor = context.seriousViolentReoffendingPredictor

    val directContactSexualReoffendingPredictorScore =
      directContactSexualReoffendingPredictor?.score?.asDoublePercentage()
    val directContactSexualReoffendingPredictorBand =
      directContactSexualReoffendingPredictor?.band
    val riskReduction = directContactSexualReoffendingPredictor?.riskReduction

    val imagesAndIndirectContactSexualReoffendingPredictorScore =
      imagesAndIndirectContactSexualReoffendingPredictor?.score
    val imagesAndIndirectContactSexualReoffendingPredictorBand =
      imagesAndIndirectContactSexualReoffendingPredictor?.band

    val seriousViolentReoffendingScore = seriousViolentReoffendingPredictor?.score
    val seriousViolentReoffendingBand = seriousViolentReoffendingPredictor?.band

    val femaleSexualOffenderCoefficient = getFemaleWeight(request)

    val combinedSeriousReoffendingPredictorScore = getScore(
      seriousViolentReoffendingScore,
      directContactSexualReoffendingPredictorScore,
      imagesAndIndirectContactSexualReoffendingPredictorScore,
      femaleSexualOffenderCoefficient?.toDouble(),
    )

    val combinedSeriousReoffendingPredictorBand = getBand(combinedSeriousReoffendingPredictorScore)
    val scoreType = seriousViolentReoffendingPredictor?.score?.let { seriousViolentReoffendingPredictor.staticOrDynamic }

    return context.apply {
      combinedSeriousReoffendingPredictorObject = CombinedSeriousReoffendingPredictorObject(
        directContactSexualReoffendingPredictorBand,
        directContactSexualReoffendingPredictorScore,
        imagesAndIndirectContactSexualReoffendingPredictorBand,
        imagesAndIndirectContactSexualReoffendingPredictorScore,
        seriousViolentReoffendingBand,
        seriousViolentReoffendingScore,
        combinedSeriousReoffendingPredictorScore,
        combinedSeriousReoffendingPredictorBand,
        scoreType,
        riskReduction,
        request.gender == Gender.FEMALE,
        request.hasEverCommittedSexualOffence,
        emptyList(),
      )
    }
  }

  fun getValidationErrorsFromContext(context: RiskScoreContext): MutableList<String> {
    val componentErrorNames = mutableListOf<String>()

    context.directContactSexualReoffendingPredictor?.validationError?.let { validationErrors ->
      if (validationErrors.isNotEmpty()) componentErrorNames += AlgorithmResponse.DIRECT_CONTACT_SEXUAL_REOFFENDING_PREDICTOR.name
    }

    context.imagesAndIndirectContactSexualReoffendingPredictor?.validationErrors?.let { validationErrors ->
      if (validationErrors.isNotEmpty()) componentErrorNames += AlgorithmResponse.IMAGES_AND_INDIRECT_CONTACT_SEXUAL_REOFFENDING_PREDICTOR.name
    }

    context.seriousViolentReoffendingPredictor?.validationErrors?.let { validationErrors ->
      if (validationErrors.any { it.type != ValidationErrorType.MISSING_DYNAMIC_INPUT }) componentErrorNames += AlgorithmResponse.SERIOUS_VIOLENT_REOFFENDING_PREDICTOR.name
    }

    return componentErrorNames
  }

  override fun applyErrorsToContext(
    context: RiskScoreContext,
    validationErrors: List<ValidationError>,
  ): RiskScoreContext = context.apply {
    combinedSeriousReoffendingPredictorObject = CombinedSeriousReoffendingPredictorObject(
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      validationErrors,
    )
  }
}
