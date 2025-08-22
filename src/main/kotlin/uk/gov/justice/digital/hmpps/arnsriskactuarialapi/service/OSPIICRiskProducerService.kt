package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
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

      request.hasEverCommittedSexualOffence == false -> {
        val unexpectedFields = arrayListOf<String>()

        if (request.totalContactAdultSexualSanctions != null) {
          unexpectedFields.add(RiskScoreRequest::totalContactAdultSexualSanctions.name)
        }
        if (request.totalContactChildSexualSanctions != null) {
          unexpectedFields.add(RiskScoreRequest::totalContactChildSexualSanctions.name)
        }
        if (request.totalIndecentImageSanctions != null) {
          unexpectedFields.add(RiskScoreRequest::totalIndecentImageSanctions.name)
        }
        if (request.totalNonContactSexualOffences != null) {
          unexpectedFields.add(RiskScoreRequest::totalNonContactSexualOffences.name)
        }

        val errors = errors + ValidationErrorResponse(
          type = ValidationErrorType.UNEXPECTED_VALUE,
          message = "Error: No sexual motivation/offending identified - fields should not be populated.",
          fields = unexpectedFields,
        )
        OSPIICObject(RiskBand.NOT_APPLICABLE, 0.0, errors)
      }

      request.gender == Gender.FEMALE -> OSPIICObject(RiskBand.LOW, 0.0, errors)
      else -> {
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
}
