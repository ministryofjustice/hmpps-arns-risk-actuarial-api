package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreDependency
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreResponse

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
    val ogrs3 = useProducer(ogrs3RiskProducerService, this)
    val ovp = useProducer(ovpRiskProducerService, this)
    val ogp = useProducer(ogpRiskProducerService, this, RiskScoreDependency().copy(OGRS3 = ogrs3))
    val mst = useProducer(mstRiskProducerService, this)

    return RiskScoreResponse(ogrs3, ovp, ogp, mst)
  }
}
