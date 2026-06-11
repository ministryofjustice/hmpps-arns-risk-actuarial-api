package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType

abstract class BaseRiskScoreProducer {

  private val logger = LoggerFactory.getLogger(javaClass)

  fun calculateRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    runCatching {
      return getRiskScore(request, context)
    }.getOrElse {
      logger.error("Unexpected error calculating risk score", it)
      return applyErrorsToContextAndReturn(
        context,
        listOf(ValidationErrorType.UNEXPECTED_ERROR.asErrorResponseForUnexpectedError("${it.message}")),
      )
    }
  }

  abstract fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext

  abstract fun applyErrorsToContextAndReturn(
    context: RiskScoreContext,
    validationErrorResponses: List<ValidationErrorResponse>,
  ): RiskScoreContext
}
