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
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validateImagesAndIndirectContactSexualReoffendingPredictor
import java.math.BigDecimal

@Service
class ImagesAndIndirectContactSexualReoffendingPredictorRiskProducerService : BaseRiskScoreProducer() {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    if (request.hasEverCommittedSexualOffence != true) {
      return context.apply {
        imagesAndIndirectContactSexualReoffendingPredictor = ImagesAndIndirectContactSexualReoffendingPredictorObject(
          0.0,
          RiskBand.NOT_APPLICABLE,
          request.gender == Gender.FEMALE,
          request.hasEverCommittedSexualOffence,
          StaticOrDynamic.STATIC,
          null,
          null,
        )
      }
    }

    val staticValidationErrors = validateImagesAndIndirectContactSexualReoffendingPredictor(request)

    if (staticValidationErrors.isNotEmpty()) {
      return applyErrorsToContext(context, staticValidationErrors)
    }

    val validStaticRequest =
      ImagesAndIndirectContactSexualReoffendingPredictorRequestValidated.Static(
        request.gender!!,
        request.totalIndecentImageSanctions!!,
        request.totalContactAdultSexualSanctions!!,
        request.totalContactChildSexualSanctions!!,
        request.totalNonContactSexualOffences!!,
      )

    return context.apply {
      imagesAndIndirectContactSexualReoffendingPredictor =
        calculateAndBuildPredictor(validStaticRequest, staticValidationErrors)
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
    request: ImagesAndIndirectContactSexualReoffendingPredictorRequestValidated,
    validationErrors: List<ValidationError>,
  ): ImagesAndIndirectContactSexualReoffendingPredictorObject {
    val staticData = request as ImagesAndIndirectContactSexualReoffendingPredictorRequestValidated.Static

    val featureValues = buildFeatureValuesMap(
      staticData = staticData,
    )

    val imagesAndIndirectContactWeight = featureValues[FeatureValue.IMAGES_AND_INDIRECT_CONTACT_WEIGHT.outputName]!!
    val score = calculatePercentageScore(imagesAndIndirectContactWeight)
    val band = getRiskBand(imagesAndIndirectContactWeight)

    return ImagesAndIndirectContactSexualReoffendingPredictorObject(
      score,
      band,
      staticData.gender == Gender.FEMALE,
      true,
      StaticOrDynamic.STATIC,
      validationErrors,
      featureValues,
    )
  }

  private fun buildFeatureValuesMap(
    staticData: ImagesAndIndirectContactSexualReoffendingPredictorRequestValidated.Static,
  ): Map<String, BigDecimal> = buildMap {
    fun FeatureValue.set(weight: BigDecimal) = put(this.outputName, weight)

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
