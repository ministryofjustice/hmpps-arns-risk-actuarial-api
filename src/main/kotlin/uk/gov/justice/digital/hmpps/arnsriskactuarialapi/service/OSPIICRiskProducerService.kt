package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ospiic.OSPIICInputValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ospiic.OSPIICObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OSPIICTransformationHelper.Companion.ospiicHierarchyBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OSPIICTransformationHelper.Companion.toOSPIICOutput
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.OSPIICValidationHelper.Companion.ospiicInitialValidation

@Service
class OSPIICRiskProducerService : RiskScoreProducer {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext = context.apply { OSPIIC = getOSPIICOutput(request, ospiicInitialValidation(request)) }

  companion object {

    fun getOSPIICOutput(request: RiskScoreRequest, errors: List<ValidationErrorResponse>): OSPIICObject = when {
      !errors.isEmpty() -> OSPIICObject(null, null, errors)
      request.gender == Gender.FEMALE -> OSPIICObject(RiskBand.LOW, 0.0, emptyList())
      else -> {
        val validInput = OSPIICInputValidated(
          hasEverCommittedSexualOffence = request.hasEverCommittedSexualOffence!!,
          totalContactAdultSexualSanctions = request.totalContactAdultSexualSanctions!!,
          totalContactChildSexualSanctions = request.totalContactChildSexualSanctions!!,
          totalIndecentImageSanctions = request.totalIndecentImageSanctions!!,
          totalNonContactSexualOffences = request.totalNonContactSexualOffences!!,
        )
        toOSPIICOutput(ospiicHierarchyBand(validInput))
      }
    }
  }
}
