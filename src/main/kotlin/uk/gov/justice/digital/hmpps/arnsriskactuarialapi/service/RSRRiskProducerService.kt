package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.osp.OSPDCObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ospiic.OSPIICObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.rsr.RSRObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.rsr.RSRRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.SNSVObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getFullRSRScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getRSRBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.asDoublePercentage

@Service
class RSRRiskProducerService : RiskScoreProducer {

  // todo - basic return to enable testing for ospdc and ospiic tickets
  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val ospdc = context.OSPDC ?: OSPDCObject(null, null, null)
    val ospiic = context.OSPIIC ?: OSPIICObject(null, null, null)
    val snsv =
      context.SNSV ?: SNSVObject(null, null, null)

    val errors = listOfNotNull(
      ospdc.validationError,
      ospiic.validationError,
      snsv.validationError,
    ).flatten()

    if (errors.isNotEmpty()) {
      val rsrScore = snsv.snsvScore?.let { snsvScore ->
        getFullRSRScore(snsvScore, ospdc.ospdcScore, ospiic.score, snsv.scoreType)?.asDoublePercentage()
      }
      val rsrBand = rsrScore?.let { getRSRBand(it) }

      return context.apply {
        RSR = RSRObject(
          ospdc.ospdcBand,
          ospdc.ospdcScore?.asDoublePercentage(),
          ospiic.band,
          ospiic.score?.asDoublePercentage(),
          rsrScore,
          rsrBand,
          snsv.snsvScore?.let { snsv.scoreType },
          null,
          errors,
        )
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
    val rsrScore =
      getFullRSRScore(request.snsv.snsvScore, request.ospdc.ospdcScore, request.ospiic.score, snsvScoreType)
    val rsrBand = getRSRBand(rsrScore)
    RSRObject(
      request.ospdc.ospdcBand,
      request.ospdc.ospdcScore,
      request.ospiic.band,
      request.ospiic.score,
      rsrScore,
      rsrBand,
      snsvScoreType,
      null,
      null,
    )
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
