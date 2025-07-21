package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.OGPRiskProducerService.Companion.coalesceForOGP

@Service
class RiskScoreService {

  @Autowired
  lateinit var ogrs3RiskProducerService: OGRS3RiskProducerService

  @Autowired
  lateinit var ovpRiskProducerService: OVPRiskProducerService

  @Autowired
  lateinit var ogpRiskProducerService: OGPRiskProducerService

  fun riskScoreProducer(riskScoreRequest: RiskScoreRequest): RiskScoreResponse {
    val ogrs3 = ogrs3RiskProducerService.getRiskScore(riskScoreRequest)
    val ovp = ovpRiskProducerService.getRiskScore(riskScoreRequest)
    val ogp = ogpRiskProducerService.getRiskScore(coalesceForOGP(riskScoreRequest, ogrs3.ogrs3TwoYear))
    return RiskScoreResponse(ogrs3, ovp, ogp)
  }
}
