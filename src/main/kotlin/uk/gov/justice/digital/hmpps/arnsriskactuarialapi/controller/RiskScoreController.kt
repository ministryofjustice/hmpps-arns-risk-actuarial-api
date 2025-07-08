package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.RiskScoreService

@RestController
@RequestMapping("/risk-scores")
class RiskScoreController(private val riskScoreService: RiskScoreService) {

  @PostMapping
  @PreAuthorize("hasRole('ARNS_RISK_ACTUARIAL')")
  fun postRiskScores(@RequestBody request: RiskScoreRequest): RiskScoreResponse = riskScoreService.riskScoreProducer(request)
}
