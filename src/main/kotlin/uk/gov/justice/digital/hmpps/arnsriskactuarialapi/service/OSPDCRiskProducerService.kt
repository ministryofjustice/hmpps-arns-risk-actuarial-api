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
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOSPDCBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOSPDCScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getStrangerVictimWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getTotalContactAdultSexualSanctionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getTotalContactChildSexualSanctionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getTotalNonContactSexualOffencesExcludingIndecentImagesWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getTotalNumberOfSanctionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.ospdcInitialValidation
import kotlin.getOrElse

@Service
class OSPDCRiskProducerService : RiskScoreProducer {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val errors = ospdcInitialValidation(request)

    if (errors.isNotEmpty()) {
      if (request.hasCommittedSexualOffence == true && request.gender == Gender.FEMALE) {
        return context.apply {
          OSPDC = OSPDCObject(RiskBand.NOT_APPLICABLE, 0.00383142, errors)
        }
      }

      return context.apply {
        OSPDC = OSPDCObject(null, null, errors)
      }
    }

    val validRequest = OSPDCRequestValidated(
      request.gender!!,
      request.dateOfBirth!!,
      request.hasCommittedSexualOffence!!,
      request.totalContactAdultSexualSanctions!!,
      request.totalContactChildSexualSanctions!!,
      request.totalNonContactSexualOffences!!,
      request.totalIndecentImageSanctions!!,
      request.dateAtStartOfFollowup!!,
      request.dateOfMostRecentSexualOffence!!,
      request.totalNumberOfSanctions!!.toInt(),
      request.victimStranger,
    )
    return context.apply {
      OSPDC = getOSPDCObject(validRequest, errors)
    }
  }

  private fun getOSPDCObject(
    request: OSPDCRequestValidated,
    errors: List<ValidationErrorResponse>,
  ): OSPDCObject = runCatching {
    listOf(
      getTotalContactAdultSexualSanctionsWeight(request.totalContactAdultSexualSanctions),
      getTotalContactChildSexualSanctionsWeight(request.totalContactChildSexualSanctions),
      getTotalNonContactSexualOffencesExcludingIndecentImagesWeight(request.totalNonContactSexualOffences, request.totalIndecentImageSanctions),
      getAgeAtStartOfFollowupWeight(request.dateOfBirth, request.dateAtStartOfFollowup),
      getAgeAtLastSanctionForSexualOffenceWeight(request.dateOfBirth, request.dateOfMostRecentSexualOffence),
      getTotalNumberOfSanctionsWeight(request.totalNumberOfSanctions),
      getStrangerVictimWeight(request.victimStranger),
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
            errors,
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
