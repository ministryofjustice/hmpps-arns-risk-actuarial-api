package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest

interface RiskProducer<T> {
  fun getRiskScore(riskScoreRequest: RiskScoreRequest): T
}
interface DependentRiskProducer<T, D> {
  fun getRiskScore(request: RiskScoreRequest, dependency: D): T
}
fun <T> useProducer(producer: RiskProducer<T>, request: RiskScoreRequest): T = producer.getRiskScore(request)

fun <T, D> useProducer(producer: DependentRiskProducer<T, D>, request: RiskScoreRequest, dependency: D): T = producer.getRiskScore(request, dependency)
