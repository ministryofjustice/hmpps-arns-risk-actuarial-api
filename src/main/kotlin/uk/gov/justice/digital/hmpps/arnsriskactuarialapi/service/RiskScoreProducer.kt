package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest

interface RiskScoreProducer {
  fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext
}
