package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.rsr.RSRObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.rsr.RSRRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getFullRSRScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOSPDCRiskBandReduction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOSPDCRiskReduction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getRSRBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.rsrInitialValidation

@Service
class RSRRiskProducerService : RiskScoreProducer {

  // todo - basic return to enable testing for ospdc and ospiic tickets
  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val ospdc = context.OSPDC!!
    val ospiic = context.OSPIIC!!
    val snsv = context.SNSV!! // todo these should never be null/empty, there will either be scores or validation exceptions
    val snsvErrors = snsv.validationError ?: emptyList()
    val riskReductionValidationErrors = rsrInitialValidation(request)
    val errors = (ospdc.validationError ?: emptyList()) + (ospiic.validationError ?: emptyList()) + snsvErrors + riskReductionValidationErrors

    val ospdcRiskReduction = if (riskReductionValidationErrors.isEmpty()) {
      getOSPDCRiskReduction(
        request.gender!!,
        request.inCustodyOrCommunity!!,
        request.mostRecentOffenceDate!!,
        request.dateOfMostRecentSexualOffence!!,
        request.dateAtStartOfFollowup!!,
        request.assessmentDate,
        ospdc.ospdcBand,
      )
    } else {
      null
    }
    
    if (snsvErrors.isNotEmpty()) {
      val ospdcBand = if (ospdcRiskReduction != null && ospdcRiskReduction) {
        getOSPDCRiskBandReduction(ospdcRiskReduction, ospdc.ospdcBand)
      } else {
        ospdc.ospdcBand
      }
      return context.apply { RSR = RSRObject(ospdcBand, ospdc.ospdcScore, ospiic.band, ospiic.score, null, null, null, ospdcRiskReduction, errors) }
    }

    val validRequest = RSRRequestValidated(
      ospdc,
      ospiic,
      snsv,
    )

    return context.apply {
      RSR = getRSRObject(validRequest, ospdcRiskReduction, errors)
    }
  }

  private fun getRSRObject(
    request: RSRRequestValidated,
    ospdcRiskReduction: Boolean?,
    errors: List<ValidationErrorResponse>?,
  ): RSRObject = runCatching {
    val snsvScoreType = request.snsv.scoreType
    val rsrScore = getFullRSRScore(request.snsv.snsvScore, request.ospdc.ospdcScore, request.ospiic.score, snsvScoreType)
    val rsrBand = getRSRBand(rsrScore)
    val ospdcBand = if (ospdcRiskReduction != null && ospdcRiskReduction) {
      getOSPDCRiskBandReduction(ospdcRiskReduction, request.ospdc.ospdcBand)
    } else {
      request.ospdc.ospdcBand
    }

    RSRObject(
      ospdcBand,
      request.ospdc.ospdcScore,
      request.ospiic.band,
      request.ospiic.score,
      rsrScore,
      rsrBand,
      snsvScoreType,
      ospdcRiskReduction,
      errors,
    )
  }.getOrElse {
    val ospdcBand = if (ospdcRiskReduction != null && ospdcRiskReduction) {
      getOSPDCRiskBandReduction(ospdcRiskReduction, request.ospdc.ospdcBand)
    } else {
      request.ospdc.ospdcBand
    }
    RSRObject(
      ospdcBand,
      request.ospdc.ospdcScore,
      request.ospiic.band,
      request.ospiic.score,
      null,
      null,
      request.snsv.scoreType,
      null,
      errors?.plus(
        arrayListOf(
          ValidationErrorResponse(
            type = ValidationErrorType.UNEXPECTED_VALUE,
            message = "Error: ${it.message}",
            fields = null,
          ),
        ),
      ),
    )
  }
}
