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
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validateMaleSexualOffencesInconsistentFields
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validateOSPIIC

@Service
class OSPIICRiskProducerService : RiskScoreProducer {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val errors = validateOSPIIC(request)

    if (errors.isNotEmpty()) {
      return context.apply { OSPIIC = OSPIICObject(null, null, errors) }
    }

    return context.apply {
      when (request.gender!!) {
        Gender.FEMALE -> {
          OSPIIC = OSPIICObject(RiskBand.NOT_APPLICABLE, 0.0, errors)
        }
        Gender.MALE -> {
          val maleErrors = mutableListOf<ValidationErrorResponse>()
          validateMaleSexualOffencesInconsistentFields(request, maleErrors)
          if (maleErrors.isNotEmpty()) {
            OSPIIC = OSPIICObject(
              RiskBand.NOT_APPLICABLE,
              0.0,
              maleErrors,
            )
          } else {
            val validInput = OSPIICInputValidated(
              totalContactAdultSexualSanctions = request.totalContactAdultSexualSanctions!!,
              totalContactChildSexualSanctions = request.totalContactChildSexualSanctions!!,
              totalIndecentImageSanctions = request.totalIndecentImageSanctions!!,
              totalNonContactSexualOffences = request.totalNonContactSexualOffences!!,
            )
            OSPIIC = toOSPIICOutput(ospiicHierarchyBand(validInput))
          }
        }
      }
    }
  }
}
