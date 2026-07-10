package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.imagesandIndirectcontactsexualreoffendingpredictor.ImagesAndIndirectContactSexualReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.imagesandIndirectcontactsexualreoffendingpredictor.ImagesAndIndirectContactSexualReoffendingPredictorRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ImagesAndIndirectContactSexualReoffendingPredictorTransformationHelper.calculatePercentageScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ImagesAndIndirectContactSexualReoffendingPredictorTransformationHelper.getHierarchyWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ImagesAndIndirectContactSexualReoffendingPredictorTransformationHelper.getRiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.ImagesAndIndirectContactSexualReoffendingPredictorValidator
import java.math.BigDecimal

@Service
class ImagesAndIndirectContactSexualReoffendingPredictorProducerService(val inputValidator: ImagesAndIndirectContactSexualReoffendingPredictorValidator) : BaseRiskScoreProducer() {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val staticValidationErrors = inputValidator.validateStatic(request)

    if (staticValidationErrors.isNotEmpty()) {
      return applyErrorsToContext(context, staticValidationErrors)
    }

    return context.apply {
      imagesAndIndirectContactSexualReoffendingPredictor =
        calculateAndBuildPredictor(request, staticValidationErrors)
    }
  }

  override fun applyErrorsToContext(
    context: RiskScoreContext,
    validationErrors: List<ValidationError>,
  ): RiskScoreContext = context.apply {
    imagesAndIndirectContactSexualReoffendingPredictor = ImagesAndIndirectContactSexualReoffendingPredictorObject(
      null,
      null,
      null,
      null,
      null,
      validationErrors,
      null,
    )
  }

  private fun calculateAndBuildPredictor(
    request: RiskScoreRequest,
    validationErrors: List<ValidationError>,
  ): ImagesAndIndirectContactSexualReoffendingPredictorObject {
    val isFemaleVersion = request.gender == Gender.FEMALE
    if (request.hasEverCommittedSexualOffence != true) {
      return ImagesAndIndirectContactSexualReoffendingPredictorObject(
        0.0,
        RiskBand.NOT_APPLICABLE,
        isFemaleVersion,
        request.hasEverCommittedSexualOffence,
        StaticOrDynamic.STATIC,
        null,
        null,
      )
    }

    val staticData = ImagesAndIndirectContactSexualReoffendingPredictorRequestValidated.Static(
      request.gender!!,
      request.totalIndecentImageSanctions!!,
      request.totalContactAdultSexualSanctions!!,
      request.totalContactChildSexualSanctions!!,
      request.totalNonContactSexualOffences!!,
      true,
    )

    val featureValues = buildFeatureValuesMap(
      staticData = staticData,
    )

    val imagesAndIndirectContactWeight = featureValues[FeatureValue.IMAGES_AND_INDIRECT_CONTACT_WEIGHT.outputName]!!
    val score = calculatePercentageScore(imagesAndIndirectContactWeight)
    val band = getRiskBand(imagesAndIndirectContactWeight)

    return ImagesAndIndirectContactSexualReoffendingPredictorObject(
      score,
      band,
      isFemaleVersion,
      true,
      StaticOrDynamic.STATIC,
      validationErrors,
      featureValues,
    )
  }

  private fun buildFeatureValuesMap(
    staticData: ImagesAndIndirectContactSexualReoffendingPredictorRequestValidated.Static,
  ): Map<String, BigDecimal> = buildMap {
    fun FeatureValue.set(weight: BigDecimal) = put(this.outputName, weight.stripTrailingZeros())

    FeatureValue.IMAGES_AND_INDIRECT_CONTACT_WEIGHT.set(
      getHierarchyWeight(
        staticData.gender,
        staticData.totalIndecentImageSanctions,
        staticData.totalContactAdultSexualSanctions,
        staticData.totalContactChildSexualSanctions,
        staticData.totalNonContactSexualOffences,
      ),
    )
  }
}
