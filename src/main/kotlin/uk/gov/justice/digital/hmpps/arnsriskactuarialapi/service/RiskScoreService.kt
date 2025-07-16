package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreResponse

@Service
class RiskScoreService {

  @Autowired
  lateinit var oGRS3RiskScoreService: OGRS3RiskScoreService

  fun riskScoreProducer(riskScoreRequest: RiskScoreRequest): RiskScoreResponse {
    val ogrs3 = oGRS3RiskScoreService.getRiskScore(riskScoreRequest)
    return RiskScoreResponse(ogrs3)
  }
}
