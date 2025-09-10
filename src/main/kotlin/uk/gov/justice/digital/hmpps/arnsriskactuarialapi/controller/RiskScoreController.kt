package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.config.ROLE_ARNS_RISK_ACTUARIAL
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.RiskScoreService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@RestController
@RequestMapping("/risk-scores/v1")
class RiskScoreController(private val riskScoreService: RiskScoreService) {

  @PostMapping
  @PreAuthorize("hasRole('$ROLE_ARNS_RISK_ACTUARIAL')")
  @Operation(
    summary = "Calculate risk scores from given inputs",
    description = "Takes in parameters and returns a set of risk scores for use in ARNS",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Successfully calculated risks scores, returned in response body.",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = RiskScoreResponse::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Request body does not match schema",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Insufficient permissions to call endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "arns-risk-actuarial-api-role")],
  )
  fun postRiskScores(@RequestBody request: RiskScoreRequest): RiskScoreResponse = riskScoreService.riskScoreProducer(request)
}
