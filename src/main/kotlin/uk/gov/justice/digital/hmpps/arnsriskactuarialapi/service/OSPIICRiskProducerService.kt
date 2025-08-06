package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ospiic.OSPIICInputValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ospiic.OSPIICOutput
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OSPIICTransformationHelper.Companion.ospiicHierarchyBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OSPIICTransformationHelper.Companion.toOSPIICOutput
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.OSPIICValidationHelper.Companion.ospiicInitialValidation

@Service
class OSPIICRiskProducerService : RiskScoreProducer {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val errors = ospiicInitialValidation(request)
    return if (errors.isEmpty()) {
      context.updateRSR(getOSPIICOutput(request))
    } else {
      context.updateRSR(errors)
    }
  }

  companion object {

    fun getOSPIICOutput(request: RiskScoreRequest): OSPIICOutput = when (request.gender!!) {
      Gender.FEMALE -> OSPIICOutput(RiskBand.LOW, 0.0, emptyList())
      Gender.MALE -> {
        val validInput = OSPIICInputValidated(
          totalContactAdultSexualSanctions = request.totalContactAdultSexualSanctions!!,
          totalContactChildSexualSanctions = request.totalContactChildSexualSanctions!!,
          totalIndecentImageSanctions = request.totalIndecentImageSanctions!!,
          totalNonContactSexualOffences = request.totalNonContactSexualOffences!!,
        )
        toOSPIICOutput(ospiicHierarchyBand(validInput))
      }
    }
  }

  private fun RiskScoreContext.updateRSR(ospiicOutput: OSPIICOutput): RiskScoreContext = this.apply { OSPIIC = ospiicOutput }

  private fun RiskScoreContext.updateRSR(errors: List<ValidationErrorResponse>): RiskScoreContext = this.apply { OSPIIC = OSPIICOutput(null, null, errors) }
}
