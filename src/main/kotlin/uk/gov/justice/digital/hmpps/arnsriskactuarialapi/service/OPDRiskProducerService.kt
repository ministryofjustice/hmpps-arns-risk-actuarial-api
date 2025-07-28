package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd.OPDObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.opdInitialValidation

@Service
class OPDRiskProducerService : RiskScoreProducer {
  override fun getRiskScore(
    request: RiskScoreRequest,
    context: RiskScoreContext,
  ): RiskScoreContext {
    val errors = opdInitialValidation(request)

    if (errors.isNotEmpty()) {
      return context
        .copy(OPD = OPDObject(opdEligibility = false, opdCheck = null, errors))
    }

    // TODO: transformation
    return context.copy(OPD = OPDObject(opdEligibility = false, opdCheck = null, validationError = emptyList()))
  }
}
