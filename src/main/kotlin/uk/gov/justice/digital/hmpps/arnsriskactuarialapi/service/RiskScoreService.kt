package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreResponse

@Service
class RiskScoreService {

  @Autowired
  lateinit var ogrs3RiskProducerService: OGRS3RiskProducerService

  @Autowired
  lateinit var ovpRiskProducerService: OVPRiskProducerService

  @Autowired
  lateinit var mstScoreProducerService: MstScoreProducerService

  fun riskScoreProducer(riskScoreRequest: RiskScoreRequest): RiskScoreResponse {
    val ogrs3 = ogrs3RiskProducerService.getRiskScore(riskScoreRequest)
    val ovp = ovpRiskProducerService.getRiskScore(riskScoreRequest)

//    val mstInput = MstInput(
//      riskScoreRequest.version,
//      riskScoreRequest.gender,
//      riskScoreRequest.dateOfBirth,
//      riskScoreRequest.peerGroupInfluences,
//      riskScoreRequest.attitudesPeerPressure,
//      riskScoreRequest.attitudesStableBehaviour,
//      riskScoreRequest.difficultiesCoping,
//      riskScoreRequest.attitudesTowardsSelf,
//      riskScoreRequest.impusilvityBehaviour,
//      riskScoreRequest.temperControl,
//      ogp.problemSolvingSkills,
//      ogp.awarenessOfConsequences,
//      ogp.understandsPeoplesViews
//    )
//
//    val mst = mstScoreProducerService.getMstScore(mstInput)
    return RiskScoreResponse(ogrs3, ovp)
  }
}
