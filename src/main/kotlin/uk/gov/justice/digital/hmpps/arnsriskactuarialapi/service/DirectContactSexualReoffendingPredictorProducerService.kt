package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.directContactSexualReoffendingPredictor.DirectContactSexualReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.directContactSexualReoffendingPredictor.DirectContactSexualReoffendingPredictorRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.DirectContactSexualReoffendingPredictorTransformationHelper.getAgeAtLastSanctionForSexualOffenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.DirectContactSexualReoffendingPredictorTransformationHelper.getAgeAtStartOfFollowupWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.DirectContactSexualReoffendingPredictorTransformationHelper.getBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.DirectContactSexualReoffendingPredictorTransformationHelper.getIsCurrentOffenceAgainstVictimStrangerWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.DirectContactSexualReoffendingPredictorTransformationHelper.getRiskBandReduction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.DirectContactSexualReoffendingPredictorTransformationHelper.getRiskReduction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.DirectContactSexualReoffendingPredictorTransformationHelper.getScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.DirectContactSexualReoffendingPredictorTransformationHelper.getTotalContactAdultSexualSanctionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.DirectContactSexualReoffendingPredictorTransformationHelper.getTotalContactChildSexualSanctionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.DirectContactSexualReoffendingPredictorTransformationHelper.getTotalNonContactSexualOffencesWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.DirectContactSexualReoffendingPredictorTransformationHelper.getTotalNumberOfSanctionsForAllOffencesWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.DirectContactSexualReoffendingPredictorValidator
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.getAgeAtDate
import java.math.BigDecimal

@Service
class DirectContactSexualReoffendingPredictorProducerService(val inputValidator: DirectContactSexualReoffendingPredictorValidator) : BaseRiskScoreProducer() {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val errors = inputValidator.validateStatic(request)

    if (errors.isNotEmpty()) {
      return applyErrorsToContext(context, errors)
    }

    return context.apply {
      directContactSexualReoffendingPredictor = calculateAndBuildPredictor(request)
    }
  }

  override fun applyErrorsToContext(
    context: RiskScoreContext,
    validationErrors: List<ValidationError>,
  ): RiskScoreContext = context.apply {
    directContactSexualReoffendingPredictor = DirectContactSexualReoffendingPredictorObject(null, null, null, null, null, null, validationErrors, null)
  }

  private fun calculateAndBuildPredictor(
    request: RiskScoreRequest,
  ): DirectContactSexualReoffendingPredictorObject {
    if (Gender.FEMALE == request.gender || request.hasEverCommittedSexualOffence == false) {
      return DirectContactSexualReoffendingPredictorObject(
        RiskBand.NOT_APPLICABLE,
        0.0,
        null,
        null,
        request.gender == Gender.FEMALE,
        request.hasEverCommittedSexualOffence,
        listOf(),
        null,
      )
    }

    val validRequest = DirectContactSexualReoffendingPredictorRequestValidated(
      request.gender!!,
      request.dateOfBirth!!,
      request.hasEverCommittedSexualOffence!!,
      request.totalContactAdultSexualSanctions!!,
      request.totalContactChildSexualSanctions!!,
      request.totalNonContactSexualOffences!!,
      request.totalIndecentImageSanctions!!,
      request.dateAtStartOfFollowupCalculated ?: request.dateOfCurrentConviction!!,
      request.totalNumberOfSanctionsForAllOffences!!,
      request.dateOfMostRecentSexualOffence,
      request.isCurrentOffenceAgainstVictimStranger,
      request.supervisionStatus!!,
      request.mostRecentOffenceDate,
      request.assessmentDate,
    )

    val featureValues = buildFeatureValuesMap(validRequest)
    val directContactSexualReoffendingPredictor64PointScore = featureValues[FeatureValue.TOTAL_WEIGHT.outputName]!!.toInt()

    if (directContactSexualReoffendingPredictor64PointScore == 0) {
      return DirectContactSexualReoffendingPredictorObject(
        RiskBand.LOW,
        getScore(directContactSexualReoffendingPredictor64PointScore),
        directContactSexualReoffendingPredictor64PointScore,
        null,
        false,
        request.hasEverCommittedSexualOffence,
        emptyList(),
        featureValues,
      )
    } else {
      val directContactSexualReoffendingPredictorBand = getBand(directContactSexualReoffendingPredictor64PointScore)
      val directContactSexualReoffendingPredictorRiskReduction = getRiskReduction(
        validRequest.gender,
        validRequest.supervisionStatus,
        validRequest.mostRecentOffenceDate,
        validRequest.dateOfMostRecentSexualOffence,
        validRequest.dateAtStartOfFollowup,
        validRequest.assessmentDate,
        directContactSexualReoffendingPredictorBand,
      )
      return DirectContactSexualReoffendingPredictorObject(
        getRiskBandReduction(directContactSexualReoffendingPredictorRiskReduction, directContactSexualReoffendingPredictorBand),
        getScore(directContactSexualReoffendingPredictor64PointScore),
        directContactSexualReoffendingPredictor64PointScore,
        directContactSexualReoffendingPredictorRiskReduction,
        false,
        request.hasEverCommittedSexualOffence,
        emptyList(),
        featureValues,
      )
    }
  }

  private fun buildFeatureValuesMap(
    request: DirectContactSexualReoffendingPredictorRequestValidated,
  ): Map<String, BigDecimal> {
    val ageAtStartOfFollowup = getAgeAtDate(request.dateOfBirth, request.dateAtStartOfFollowup, "ageAtStartOfFollowup")
    val ageAtLastSanctionForSexualOffence = if (request.dateOfMostRecentSexualOffence != null) {
      getAgeAtDate(
        request.dateOfBirth,
        request.dateOfMostRecentSexualOffence,
        "ageAtLastSanctionForSexualOffence",
      )
    } else {
      0
    }

    return buildMap {
      fun FeatureValue.set(weight: BigDecimal) = put(this.outputName, weight)

      FeatureValue.TOTAL_CONTACT_ADULT_SEXUAL_SANCTIONS_WEIGHT.set(
        BigDecimal(
          getTotalContactAdultSexualSanctionsWeight(
            request.totalContactAdultSexualSanctions,
          ),
        ),
      )
      FeatureValue.TOTAL_CONTACT_CHILD_SEXUAL_SANCTIONS_WEIGHT.set(
        BigDecimal(
          getTotalContactChildSexualSanctionsWeight(
            request.totalContactChildSexualSanctions,
          ),
        ),
      )
      FeatureValue.TOTAL_NON_CONTACT_SEXUAL_OFFENCES_WEIGHT.set(BigDecimal(getTotalNonContactSexualOffencesWeight(request.totalNonContactSexualOffences)))
      FeatureValue.AGE_AT_START_OF_FOLLOW_UP_WEIGHT.set(BigDecimal(getAgeAtStartOfFollowupWeight(ageAtStartOfFollowup)))
      FeatureValue.AGE_AT_LAST_SANCTION_FOR_SEXUAL_OFFENCE_WEIGHT.set(
        BigDecimal(
          getAgeAtLastSanctionForSexualOffenceWeight(
            ageAtLastSanctionForSexualOffence,
          ),
        ),
      )
      FeatureValue.TOTAL_NUMBER_OF_SANCTIONS_FOR_ALL_OFFENCES_WEIGHT.set(
        BigDecimal(
          getTotalNumberOfSanctionsForAllOffencesWeight(request.totalNumberOfSanctionsForAllOffences),
        ),
      )
      FeatureValue.CURRENT_OFFENCE_AGAINST_VICTIM_STRANGER_WEIGHT.set(
        BigDecimal(
          getIsCurrentOffenceAgainstVictimStrangerWeight(request.isCurrentOffenceAgainstVictimStranger),
        ),
      )

      val totalWeight = values.fold(BigDecimal.ZERO, BigDecimal::add)
      FeatureValue.TOTAL_WEIGHT.set(totalWeight)
    }
  }
}
