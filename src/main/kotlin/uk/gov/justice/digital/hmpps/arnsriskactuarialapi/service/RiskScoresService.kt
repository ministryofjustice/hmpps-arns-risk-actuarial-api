package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreResponse

@Service
class RiskScoresService {

  fun riskScoreProducer(riskScoreRequest: RiskScoreRequest): RiskScoreResponse {
    // todo
    return RiskScoreResponse(riskScoreRequest.score)
  }
}
