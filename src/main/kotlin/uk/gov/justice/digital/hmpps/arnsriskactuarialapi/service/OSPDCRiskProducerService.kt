package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.osp.OSPDCObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.osp.OSPDCRequestValidated
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
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.addMissingCriteriaValidation
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validateOSPDC

const val FIXED_RSR_CONTRIBUTION = 0.00383142

@Service
class OSPDCRiskProducerService : BaseRiskScoreProducer() {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val errors = validateOSPDC(request)

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
        )
      }
    }

    // When female, there is no score or band produced
    if (request.gender == Gender.FEMALE && request.hasEverCommittedSexualOffence == true) {
      return context.apply {
        OSPDC = OSPDCObject(
          RiskBand.NOT_APPLICABLE,
          FIXED_RSR_CONTRIBUTION,
          null,
          addMissingCriteriaValidation(arrayListOf(RiskScoreRequest::gender.name), emptyList()),
        )
      }
    }

    val validRequest = OSPDCRequestValidated(
      request.gender!!,
      request.dateOfBirth!!,
      request.hasEverCommittedSexualOffence ?: false,
      request.totalContactAdultSexualSanctions!!,
      request.totalContactChildSexualSanctions!!,
      request.totalNonContactSexualOffences!!,
      request.totalIndecentImageSanctions!!,
      request.dateAtStartOfFollowup!!,
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
  ): RiskScoreContext = context.apply { OSPDC = OSPDCObject(null, null, null, validationErrorResponses) }

  private fun getOSPDCObject(
    request: OSPDCRequestValidated,
  ): OSPDCObject {
    listOf(
      getTotalContactAdultSexualSanctionsWeight(request.totalContactAdultSexualSanctions),
      getTotalContactChildSexualSanctionsWeight(request.totalContactChildSexualSanctions),
      getTotalNonContactSexualOffencesWeight(request.totalNonContactSexualOffences),
      getAgeAtStartOfFollowupWeight(request.dateOfBirth, request.dateAtStartOfFollowup),
      request.dateOfMostRecentSexualOffence?.let { date ->
        getAgeAtLastSanctionForSexualOffenceWeight(
          request.dateOfBirth,
          date,
        )
      } ?: 0,
      getTotalNumberOfSanctionsForAllOffencesWeight(request.totalNumberOfSanctionsForAllOffences),
      getIsCurrentOffenceAgainstVictimStrangerWeight(request.isCurrentOffenceAgainstVictimStranger),
    ).sum()
      .let { ospdc64PointScore ->
        if (ospdc64PointScore == 0) {
          return OSPDCObject(
            getOSPDCBand(ospdc64PointScore),
            getOSPDCScore(ospdc64PointScore),
            null,
            emptyList(),
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
            ospRiskReduction,
            emptyList(),
          )
        }
      }
  }
}
