package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest

interface RiskProducer<T> {
  @Override
  fun getRiskScore(riskScoreRequest: RiskScoreRequest): T
}
