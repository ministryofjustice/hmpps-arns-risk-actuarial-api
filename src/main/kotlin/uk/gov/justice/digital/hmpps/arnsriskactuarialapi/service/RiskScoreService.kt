package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.toRiskScoreResponse

@Service
class RiskScoreService {

  @Autowired
  lateinit var ogrs3RiskProducerService: OGRS3RiskProducerService

  @Autowired
  lateinit var ovpRiskProducerService: OVPRiskProducerService

  @Autowired
  lateinit var ogpRiskProducerService: OGPRiskProducerService

  @Autowired
  lateinit var mstRiskProducerService: MSTRiskProducerService

  fun riskScoreProducer(riskScoreRequest: RiskScoreRequest): RiskScoreResponse = with(riskScoreRequest) {
    var context = RiskScoreContext()
    context = ogrs3RiskProducerService.getRiskScore(this, context)
    context = ovpRiskProducerService.getRiskScore(this, context)
    context = ogpRiskProducerService.getRiskScore(this, context)
    context = mstRiskProducerService.getRiskScore(this, context)

    return context.toRiskScoreResponse()
  }
}
