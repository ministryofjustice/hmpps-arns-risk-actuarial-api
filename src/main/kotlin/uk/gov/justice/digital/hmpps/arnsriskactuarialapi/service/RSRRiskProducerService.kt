package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.osp.OSPDCObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ospiic.OSPIICObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.rsr.RSRObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.SNSVObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getFullRSRScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getRSRBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.asDoublePercentage
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.roundToNDecimals

@Service
class RSRRiskProducerService : RiskScoreProducer {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val ospdc = context.OSPDC ?: OSPDCObject(null, null, null)
    val ospiic = context.OSPIIC ?: OSPIICObject(null, null, null)
    val snsv = context.SNSV ?: SNSVObject(null, null, null)

    val errors = listOfNotNull(
      ospdc.validationError,
      ospiic.validationError,
      snsv.validationError,
    ).flatten()

    try {
      val ospdcScore = ospdc.ospdcScore ?: 0.0
      val ospdcBand = ospdc.ospdcBand ?: RiskBand.NOT_APPLICABLE
      val ospiicScore = ospiic.score ?: 0.0
      val ospiicBand = ospiic.band ?: RiskBand.NOT_APPLICABLE
      val snsvScore = snsv.snsvScore?.asDoublePercentage()
      val rsrScore = snsvScore?.let {
        getFullRSRScore(it, ospdcScore, ospiicScore, snsv.scoreType)?.roundToNDecimals(2)
      }
      val rsrBand = rsrScore?.let { getRSRBand(it) }

      return context.apply {
        RSR = RSRObject(
          ospdcBand,
          ospdcScore.asDoublePercentage(),
          ospiicBand,
          ospiicScore.asDoublePercentage(),
          rsrScore,
          rsrBand,
          snsv.snsvScore?.let { snsv.scoreType },
          null,
          errors,
        )
      }
    } catch (e: Exception) {
      arrayListOf(
        ValidationErrorResponse(
          type = ValidationErrorType.UNEXPECTED_VALUE,
          message = "Error: ${e.message}",
          fields = null,
        ),
      )
      return context.apply {
        RSR = RSRObject(
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          errors,
        )
      }
    }
  }
}
