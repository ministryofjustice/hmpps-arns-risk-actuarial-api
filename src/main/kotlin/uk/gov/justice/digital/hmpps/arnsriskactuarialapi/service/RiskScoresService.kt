package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreResponse

@Service
class RiskScoresService {

  fun riskScoreProducer(riskScoreRequest: RiskScoreRequest): RiskScoreResponse {
    // todo 1) Check all nullables
    // todo 2) call transformation helpers with valid input request
    return RiskScoreResponse(riskScoreRequest.score)
  }
}
