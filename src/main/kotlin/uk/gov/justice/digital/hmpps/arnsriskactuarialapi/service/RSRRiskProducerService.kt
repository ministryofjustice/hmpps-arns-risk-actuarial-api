package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.rsr.RSRObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.rsr.RSRRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getFullRSRScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getRSRBand

@Service
class RSRRiskProducerService : RiskScoreProducer {

  // todo - basic return to enable testing for ospdc and ospiic tickets
  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val ospdc = context.OSPDC!!
    val ospiic = context.OSPIIC!!
    val snsv = context.SNSV!! // todo these should never be null/empty, there will either be scores or validation exceptions

    val errors = (ospdc.validationError ?: emptyList()) + (ospiic.validationError ?: emptyList()) + (snsv.validationError ?: emptyList()) // TODO cleanup

    // todo could be cleaner
    if (errors.isNotEmpty()) {
      if (snsv.snsvScore != null) {
        val snsvScoreType = snsv.scoreType
        val rsrScore = getFullRSRScore(snsv.snsvScore, ospdc.ospdcScore, ospiic.score, snsvScoreType)
        val rsrBand = getRSRBand(rsrScore)
        return context.apply { RSR = RSRObject(ospdc.ospdcBand, ospdc.ospdcScore, ospiic.band, ospiic.score, rsrScore, rsrBand, snsvScoreType, null, errors) }
      } else {
        return context.apply { RSR = RSRObject(ospdc.ospdcBand, ospdc.ospdcScore, ospiic.band, ospiic.score, null, null, null, null, errors) }
      }
    }

    val validRequest = RSRRequestValidated(
      ospdc,
      ospiic,
      snsv,
    )
    return context.apply {
      RSR = getRSRObject(validRequest)
    }
  }

  private fun getRSRObject(
    request: RSRRequestValidated,
  ): RSRObject = runCatching {
    val snsvScoreType = request.snsv.scoreType
    val rsrScore = getFullRSRScore(request.snsv.snsvScore, request.ospdc.ospdcScore, request.ospiic.score, snsvScoreType)
    val rsrBand = getRSRBand(rsrScore)
    RSRObject(request.ospdc.ospdcBand, request.ospdc.ospdcScore, request.ospiic.band, request.ospiic.score, rsrScore, rsrBand, snsvScoreType, null, null)
  }.getOrElse {
    RSRObject(
      request.ospdc.ospdcBand,
      request.ospdc.ospdcScore,
      request.ospiic.band,
      request.ospiic.score,
      null,
      null,
      request.snsv.scoreType,
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
