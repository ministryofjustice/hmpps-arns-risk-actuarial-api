package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.AlgorithmResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.directContactSexualReoffendingPredictor.DirectContactSexualReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.imagesandIndirectcontactsexualreoffendingpredictor.ImagesAndIndirectContactSexualReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.rsr.RSRObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.seriousviolentreoffendingpredictor.SeriousViolentReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getRSRBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.asDoublePercentage
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.roundToNDecimals
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.sanitisePercentage

const val FEMALE_SEXUAL_OFFENDER_RSR_CONTRIBUTION = 0.00383141762

@Service
class RSRRiskProducerService : BaseRiskScoreProducer() {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val directContactSexualReoffendingPredictor = context.directContactSexualReoffendingPredictor ?: DirectContactSexualReoffendingPredictorObject(null, null, null, null, null, null, null, null)
    val imagesAndIndirectContactSexualReoffendingPredictor =
      context.imagesAndIndirectContactSexualReoffendingPredictor ?: ImagesAndIndirectContactSexualReoffendingPredictorObject(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
      )
    val seriousViolentReoffendingPredictor =
      context.seriousViolentReoffendingPredictor ?: SeriousViolentReoffendingPredictorObject(
        null,
        null,
        null,
        null,
        null,
      )

    val componentErrorNames = mutableListOf<String>()
    context.directContactSexualReoffendingPredictor?.validationError?.let { validationErrors -> if (validationErrors.isNotEmpty()) componentErrorNames += AlgorithmResponse.DIRECT_CONTACT_SEXUAL_REOFFENDING_PREDICTOR.name }
    imagesAndIndirectContactSexualReoffendingPredictor.validationErrors?.let { validationErrors -> if (validationErrors.isNotEmpty()) componentErrorNames += AlgorithmResponse.IMAGES_AND_INDIRECT_CONTACT_SEXUAL_REOFFENDING_PREDICTOR.name }
    context.seriousViolentReoffendingPredictor?.validationErrors?.let { validationErrors ->
      if (validationErrors.any { it.type != ValidationErrorType.MISSING_DYNAMIC_INPUT }) componentErrorNames += AlgorithmResponse.SERIOUS_VIOLENT_REOFFENDING_PREDICTOR.name
    }

    if (componentErrorNames.isNotEmpty()) {
      return applyErrorsToContext(
        context,
        listOf(
          ValidationErrorType.COMPONENT_VALIDATION_ERROR.asError(componentErrorNames),
        ),
      )
    }

    val errors = listOfNotNull(
      directContactSexualReoffendingPredictor.validationError,
      imagesAndIndirectContactSexualReoffendingPredictor.validationErrors,
      seriousViolentReoffendingPredictor.validationErrors,
    ).flatten()
      .filter { it.type != ValidationErrorType.MISSING_DYNAMIC_INPUT }
      .distinct()

    if (sexualSectionAllNull(request) || sexualOffenceHistoryTrueButSanctionsZero(request)) {
      return applyErrorsToContext(context, errors)
    }

    val directContactSexualReoffendingPredictorScore = directContactSexualReoffendingPredictor.score?.asDoublePercentage() ?: 0.0
    val directContactSexualReoffendingPredictorBand = directContactSexualReoffendingPredictor.band ?: RiskBand.NOT_APPLICABLE
    val riskReduction = directContactSexualReoffendingPredictor.riskReduction
    val imagesAndIndirectContactSexualReoffendingPredictorScore = imagesAndIndirectContactSexualReoffendingPredictor.score ?: 0.0
    val imagesAndIndirectContactSexualReoffendingPredictorBand =
      imagesAndIndirectContactSexualReoffendingPredictor.band ?: RiskBand.NOT_APPLICABLE
    val seriousViolentReoffendingScore = seriousViolentReoffendingPredictor.score
    val femaleDirectContactSexualReoffendingPredictorWeight = if (Gender.FEMALE == request.gender && request.hasEverCommittedSexualOffence == true) FEMALE_SEXUAL_OFFENDER_RSR_CONTRIBUTION.asDoublePercentage() else 0.0
    val rsrScore = if (hasBlockingErrors(errors)) {
      null
    } else {
      listOfNotNull(seriousViolentReoffendingScore, directContactSexualReoffendingPredictorScore, imagesAndIndirectContactSexualReoffendingPredictorScore, femaleDirectContactSexualReoffendingPredictorWeight).sum().roundToNDecimals(2).sanitisePercentage()
    }
    val rsrBand = getRSRBand(rsrScore)
    val scoreType = if (rsrScore != null) {
      seriousViolentReoffendingPredictor.score?.let { seriousViolentReoffendingPredictor.staticOrDynamic }
    } else {
      null
    }

    if (sexualOffenceHistoryFalseButSanctionsNull(request)) {
      return context.apply {
        RSR = RSRObject(
          null,
          directContactSexualReoffendingPredictorScore,
          null,
          imagesAndIndirectContactSexualReoffendingPredictorScore,
          seriousViolentReoffendingScore,
          rsrScore,
          rsrBand,
          scoreType,
          riskReduction,
          request.gender == Gender.FEMALE,
          request.hasEverCommittedSexualOffence,
          errors,
        )
      }
    }

    return context.apply {
      RSR = RSRObject(
        directContactSexualReoffendingPredictorBand,
        directContactSexualReoffendingPredictorScore,
        imagesAndIndirectContactSexualReoffendingPredictorBand,
        imagesAndIndirectContactSexualReoffendingPredictorScore,
        seriousViolentReoffendingScore,
        rsrScore,
        rsrBand,
        scoreType,
        riskReduction,
        request.gender == Gender.FEMALE,
        request.hasEverCommittedSexualOffence,
        errors,
      )
    }
  }

  override fun applyErrorsToContext(
    context: RiskScoreContext,
    validationErrors: List<ValidationError>,
  ): RiskScoreContext = context.apply {
    RSR = RSRObject(
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

  internal fun hasBlockingErrors(errors: List<ValidationError>): Boolean = errors.any { it.fields.contains(RiskScoreRequest::isCurrentOffenceSexuallyMotivated.name) }
}
