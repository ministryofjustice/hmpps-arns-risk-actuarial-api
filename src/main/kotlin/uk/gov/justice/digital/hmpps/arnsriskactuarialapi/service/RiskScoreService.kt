package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreResponse

@Service
class RiskScoreService {

  @Autowired
  lateinit var oGRS3RiskProducerService: OGRS3RiskProducerService

  fun riskScoreProducer(riskScoreRequest: RiskScoreRequest): RiskScoreResponse {
    val ogrs3 = oGRS3RiskProducerService.getRiskScore(riskScoreRequest)
    return RiskScoreResponse(ogrs3)
  }
}
