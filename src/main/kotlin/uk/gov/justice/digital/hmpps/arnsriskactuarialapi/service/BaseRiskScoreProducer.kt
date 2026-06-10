package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType

abstract class BaseRiskScoreProducer<T: RequestValidated> {

  private val logger = LoggerFactory.getLogger(javaClass)

  fun calculateRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    runCatching {
      return getRiskScore(request, context)
    }.getOrElse {
      logger.error("Unexpected error calculating risk score", it)
      return buildPredictorAndApplyToContext(
        context,
        null,
        listOf(ValidationErrorType.UNEXPECTED_ERROR.asErrorResponseForUnexpectedError("${it.message}")),
        emptyList(),
      )
    }
  }

  abstract fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext

  abstract fun buildPredictorAndApplyToContext(
    context: RiskScoreContext,
    request: T?,
    staticValidationErrors: List<ValidationError>,
    dynamicValidationErrors: List<ValidationError>,
  ): RiskScoreContext
}
