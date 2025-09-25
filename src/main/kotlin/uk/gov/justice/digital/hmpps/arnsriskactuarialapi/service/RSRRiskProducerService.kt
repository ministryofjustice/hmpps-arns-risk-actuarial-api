package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.osp.OSPDCObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ospiic.OSPIICObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.rsr.RSRObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.SNSVObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getRSRBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.asDoublePercentage
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.roundToNDecimals

@Service
class RSRRiskProducerService : BaseRiskScoreProducer() {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val ospdc = context.OSPDC ?: OSPDCObject(null, null, null, null, null, null, null)
    val ospiic = context.OSPIIC ?: OSPIICObject(null, null, null, null, null)
    val snsv = context.SNSV ?: SNSVObject(null, null, null)

    val errors = listOfNotNull(
      ospdc.validationError,
      ospiic.validationError,
      snsv.validationError,
    ).flatten()

    val ospdcScore = ospdc.ospdcScore?.asDoublePercentage() ?: 0.0
    val ospdcBand = ospdc.ospdcBand ?: RiskBand.NOT_APPLICABLE
    val ospRiskReduction = ospdc.ospRiskReduction
    val ospiicScore = ospiic.score?.asDoublePercentage() ?: 0.0
    val ospiicBand = ospiic.band ?: RiskBand.NOT_APPLICABLE
    val snsvScore = snsv.snsvScore?.asDoublePercentage()
    val rsrScore = if (hasBlockingErrors(errors)) {
      null
    } else {
      listOfNotNull(snsvScore, ospdcScore, ospiicScore).sum().roundToNDecimals(2)
    }
    val rsrBand = getRSRBand(rsrScore)
    val scoreType = if (rsrScore != null) {
      snsv.snsvScore?.let { snsv.scoreType }
    } else {
      null
    }
    return context.apply {
      RSR = RSRObject(
        ospdcBand,
        ospdcScore,
        ospiicBand,
        ospiicScore,
        snsvScore,
        rsrScore,
        rsrBand,
        scoreType,
        ospRiskReduction,
        request.gender == Gender.FEMALE,
        request.hasEverCommittedSexualOffence,
        errors,
      )
    }
  }

  override fun applyErrorsToContextAndReturn(
    context: RiskScoreContext,
    validationErrorResponses: List<ValidationErrorResponse>,
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
      validationErrorResponses,
    )
  }

  internal fun hasBlockingErrors(errors: List<ValidationErrorResponse>): Boolean = errors.any { it.fields.contains(RiskScoreRequest::isCurrentOffenceSexuallyMotivated.name) }
}
