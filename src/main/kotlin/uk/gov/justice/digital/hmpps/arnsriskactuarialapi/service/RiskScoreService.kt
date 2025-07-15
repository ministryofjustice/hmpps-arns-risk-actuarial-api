package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.OGRS3Object
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreResponse

@Service
class RiskScoreService {

  fun riskScoreProducer(riskScoreRequest: RiskScoreRequest): RiskScoreResponse {
    // todo placeholder response, transformation logic required
    val ogrs3 = OGRS3Object(riskScoreRequest.version, 1.0, 1.0, RiskBand.LOW, listOf())
    return RiskScoreResponse(ogrs3)
  }
}
