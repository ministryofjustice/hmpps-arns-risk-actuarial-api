package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.osp.OSPDCObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.osp.OSPDCRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.FeatureValue.AGE_AT_LAST_SANCTION_FOR_SEXUAL_OFFENCE_WEIGHT
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.FeatureValue.AGE_AT_START_OF_FOLLOW_UP_WEIGHT
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.FeatureValue.CURRENT_OFFENCE_AGAINST_VICTIM_STRANGER_WEIGHT
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.FeatureValue.TOTAL_CONTACT_ADULT_SEXUAL_SANCTIONS_WEIGHT
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.FeatureValue.TOTAL_CONTACT_CHILD_SEXUAL_SANCTIONS_WEIGHT
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.FeatureValue.TOTAL_NON_CONTACT_SEXUAL_OFFENCES_WEIGHT
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.FeatureValue.TOTAL_NUMBER_OF_SANCTIONS_FOR_ALL_OFFENCES_WEIGHT
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getAgeAtLastSanctionForSexualOffenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getAgeAtStartOfFollowupWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getIsCurrentOffenceAgainstVictimStrangerWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOSPDCBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOSPDCRiskBandReduction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOSPDCRiskReduction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOSPDCScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getTotalContactAdultSexualSanctionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getTotalContactChildSexualSanctionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getTotalNonContactSexualOffencesWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getTotalNumberOfSanctionsForAllOffencesWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validateOSP

@Service
class OSPDCRiskProducerService : BaseRiskScoreProducer() {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val errors = validateOSP(request, true)

    if (errors.isNotEmpty()) {
      return applyErrorsToContextAndReturn(context, errors)
    }

    if (request.hasEverCommittedSexualOffence == false) {
      return context.apply {
        OSPDC = OSPDCObject(
          RiskBand.NOT_APPLICABLE,
          0.0,
          null,
          null,
          request.gender == Gender.FEMALE,
          false,
          listOf(),
          null,
        )
      }
    }

    if (Gender.FEMALE == request.gender && request.hasEverCommittedSexualOffence == true) {
      return context.apply {
        OSPDC = OSPDCObject(
          RiskBand.NOT_APPLICABLE,
          0.0,
          null,
          null,
          true,
          false,
          listOf(),
          null,
        )
      }
    }

    val validRequest = OSPDCRequestValidated(
      request.gender!!,
      request.dateOfBirth!!,
      request.hasEverCommittedSexualOffence!!,
      request.totalContactAdultSexualSanctions!!,
      request.totalContactChildSexualSanctions!!,
      request.totalNonContactSexualOffences!!,
      request.totalIndecentImageSanctions!!,
      request.dateAtStartOfFollowupUserInput!!,
      request.totalNumberOfSanctionsForAllOffences!!.toInt(),
      request.dateOfMostRecentSexualOffence,
      request.isCurrentOffenceAgainstVictimStranger,
      request.supervisionStatus!!,
      request.mostRecentOffenceDate,
      request.assessmentDate,
    )
    return context.apply {
      OSPDC = getOSPDCObject(validRequest)
    }
  }

  override fun applyErrorsToContextAndReturn(
    context: RiskScoreContext,
    validationErrorResponses: List<ValidationErrorResponse>,
  ): RiskScoreContext = context.apply { OSPDC = OSPDCObject(null, null, null, null, null, null, validationErrorResponses, null) }

  private fun getOSPDCObject(
    request: OSPDCRequestValidated,
  ): OSPDCObject {
    val totalContactAdultSexualSanctionsWeight =
      getTotalContactAdultSexualSanctionsWeight(request.totalContactAdultSexualSanctions)
    val totalContactChildSexualSanctionsWeight =
      getTotalContactChildSexualSanctionsWeight(request.totalContactChildSexualSanctions)
    val totalNonContactSexualOffencesWeight =
      getTotalNonContactSexualOffencesWeight(request.totalNonContactSexualOffences)
    val ageAtStartOfFollowupWeight = getAgeAtStartOfFollowupWeight(request.dateOfBirth, request.dateAtStartOfFollowup)
    val ageAtLastSanctionForSexualOffenceWeight = request.dateOfMostRecentSexualOffence?.let { date ->
      getAgeAtLastSanctionForSexualOffenceWeight(
        request.dateOfBirth,
        date,
      )
    } ?: 0
    val totalNumberOfSanctionsForAllOffencesWeight =
      getTotalNumberOfSanctionsForAllOffencesWeight(request.totalNumberOfSanctionsForAllOffences)
    val currentOffenceAgainstVictimStrangerWeight =
      getIsCurrentOffenceAgainstVictimStrangerWeight(request.isCurrentOffenceAgainstVictimStranger)
    listOf(
      totalContactAdultSexualSanctionsWeight,
      totalContactChildSexualSanctionsWeight,
      totalNonContactSexualOffencesWeight,
      ageAtStartOfFollowupWeight,
      ageAtLastSanctionForSexualOffenceWeight,
      totalNumberOfSanctionsForAllOffencesWeight,
      currentOffenceAgainstVictimStrangerWeight,
    ).sum()
      .let { ospdc64PointScore ->

        val featureValues = buildFeatureValues(
          totalContactAdultSexualSanctionsWeight,
          totalContactChildSexualSanctionsWeight,
          totalNonContactSexualOffencesWeight,
          ageAtStartOfFollowupWeight,
          ageAtLastSanctionForSexualOffenceWeight,
          totalNumberOfSanctionsForAllOffencesWeight,
          currentOffenceAgainstVictimStrangerWeight,
        )

        // Use ospdc64PointScore as pointScore
        if (ospdc64PointScore == 0) {
          return OSPDCObject(
            getOSPDCBand(ospdc64PointScore),
            getOSPDCScore(ospdc64PointScore),
            ospdc64PointScore,
            null,
            request.gender == Gender.FEMALE,
            request.hasEverCommittedSexualOffence,
            emptyList(),
            featureValues,
          )
        } else {
          val ospdcBand = getOSPDCBand(ospdc64PointScore)
          val ospRiskReduction = getOSPDCRiskReduction(
            request.gender,
            request.supervisionStatus,
            request.mostRecentOffenceDate,
            request.dateOfMostRecentSexualOffence,
            request.dateAtStartOfFollowup,
            request.assessmentDate,
            ospdcBand,
          )
          return OSPDCObject(
            getOSPDCRiskBandReduction(ospRiskReduction, ospdcBand),
            getOSPDCScore(ospdc64PointScore),
            ospdc64PointScore,
            ospRiskReduction,
            request.gender == Gender.FEMALE,
            request.hasEverCommittedSexualOffence,
            emptyList(),
            featureValues,
          )
        }
      }
  }

  private fun buildFeatureValues(
    totalContactAdultSexualSanctionsWeight: Int,
    totalContactChildSexualSanctionsWeight: Int,
    totalNonContactSexualOffencesWeight: Int,
    ageAtStartOfFollowupWeight: Int,
    ageAtLastSanctionForSexualOffenceWeight: Int,
    totalNumberOfSanctionsForAllOffencesWeight: Int,
    currentOffenceAgainstVictimStrangerWeight: Int,
  ): Map<String, String> = mapOf(
    TOTAL_CONTACT_ADULT_SEXUAL_SANCTIONS_WEIGHT.asPair(totalContactAdultSexualSanctionsWeight.toString()),
    TOTAL_CONTACT_CHILD_SEXUAL_SANCTIONS_WEIGHT.asPair(totalContactChildSexualSanctionsWeight.toString()),
    TOTAL_NON_CONTACT_SEXUAL_OFFENCES_WEIGHT.asPair(totalNonContactSexualOffencesWeight.toString()),
    AGE_AT_START_OF_FOLLOW_UP_WEIGHT.asPair(ageAtStartOfFollowupWeight.toString()),
    AGE_AT_LAST_SANCTION_FOR_SEXUAL_OFFENCE_WEIGHT.asPair(ageAtLastSanctionForSexualOffenceWeight.toString()),
    TOTAL_NUMBER_OF_SANCTIONS_FOR_ALL_OFFENCES_WEIGHT.asPair(totalNumberOfSanctionsForAllOffencesWeight.toString()),
    CURRENT_OFFENCE_AGAINST_VICTIM_STRANGER_WEIGHT.asPair(currentOffenceAgainstVictimStrangerWeight.toString()),
  )
}
