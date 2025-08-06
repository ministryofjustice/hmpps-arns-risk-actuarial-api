package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.rsr.RSRObject

@Service
class RSRRiskProducerService : RiskScoreProducer {

  // todo - basic return to enable testing for ospdc and ospiic tickets
  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val ospdc = context.OSPDC!!
    val errors = ospdc.validationError
    val snsv = context.SNSV!! // todo get the snsvScore and use this as part of the rsrBand calculation
    val snsvScoreType = snsv.scoreType
    val rsr = RSRObject(ospdc.ospdcBand, ospdc.ospdcScore, null, null, null, snsvScoreType, null, errors)

    return context.apply { RSR = rsr }
  }
}
