package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest

interface RiskProducer<T> {
  fun getRiskScore(riskScoreRequest: RiskScoreRequest): T
}

interface DependentRiskProducer<T, RiskScoreDependency> {
  fun getRiskScore(request: RiskScoreRequest, dependency: RiskScoreDependency): T
}

fun <T> useProducer(producer: RiskProducer<T>, request: RiskScoreRequest): T = producer.getRiskScore(request)

fun <T, RiskScoreDependency> useProducer(
  producer: DependentRiskProducer<T, RiskScoreDependency>,
  request: RiskScoreRequest,
  dependency: RiskScoreDependency,
): T = producer.getRiskScore(request, dependency)
