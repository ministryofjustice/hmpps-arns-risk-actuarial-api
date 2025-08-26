package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.osp.OSPDCObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.osp.OSPDCRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getAgeAtLastSanctionForSexualOffenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getAgeAtStartOfFollowupWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getIsCurrentOffenceAgainstVictimStrangerWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOSPDCBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOSPDCScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getTotalContactAdultSexualSanctionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getTotalContactChildSexualSanctionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getTotalNonContactSexualOffencesExcludingIndecentImagesWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getTotalNumberOfSanctionsForAllOffencesWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.addMissingCriteriaValidation
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.ospdcInitialValidation

const val FIXED_RSR_CONTRIBUTION = 0.00383142

@Service
class OSPDCRiskProducerService : RiskScoreProducer {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val errors = ospdcInitialValidation(request)

    if (errors.isNotEmpty()) {
      return context.apply {
        OSPDC = OSPDCObject(null, null, errors)
      }
    }

    if (request.hasEverCommittedSexualOffence == false) {
      return context.apply {
        OSPDC = OSPDCObject(
          RiskBand.NOT_APPLICABLE,
          0.0,
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
    )
    return context.apply {
      OSPDC = getOSPDCObject(validRequest)
    }
  }

  private fun getOSPDCObject(
    request: OSPDCRequestValidated,
  ): OSPDCObject = runCatching {
    listOf(
      getTotalContactAdultSexualSanctionsWeight(request.totalContactAdultSexualSanctions),
      getTotalContactChildSexualSanctionsWeight(request.totalContactChildSexualSanctions),
      getTotalNonContactSexualOffencesExcludingIndecentImagesWeight(
        request.totalNonContactSexualOffences,
        request.totalIndecentImageSanctions,
      ),
      getAgeAtStartOfFollowupWeight(request.dateOfBirth, request.dateAtStartOfFollowup),
      request.dateOfMostRecentSexualOffence?.let { date -> getAgeAtLastSanctionForSexualOffenceWeight(request.dateOfBirth, date) } ?: 0,
      getTotalNumberOfSanctionsForAllOffencesWeight(request.totalNumberOfSanctionsForAllOffences),
      getIsCurrentOffenceAgainstVictimStrangerWeight(request.isCurrentOffenceAgainstVictimStranger),
    ).sum()
      .let { ospdc64PointScore ->
        if (ospdc64PointScore == 0) {
          OSPDCObject(
            getOSPDCBand(ospdc64PointScore),
            getOSPDCScore(ospdc64PointScore),
            arrayListOf(
              ValidationErrorResponse(
                type = ValidationErrorType.NOT_APPLICABLE,
                message = "OSP/DC band not applicable, 64 point score value: 0",
                fields = null,
              ),
            ),
          )
        } else {
          OSPDCObject(
            getOSPDCBand(ospdc64PointScore),
            getOSPDCScore(ospdc64PointScore),
            emptyList(),
          )
        }
      }
  }.getOrElse {
    OSPDCObject(
      null,
      null,
      arrayListOf(
        ValidationErrorResponse(
          type = ValidationErrorType.UNEXPECTED_VALUE,
          message = "Error: ${it.message}",
          fields = null,
        ),
      ),
    )
  }
}
