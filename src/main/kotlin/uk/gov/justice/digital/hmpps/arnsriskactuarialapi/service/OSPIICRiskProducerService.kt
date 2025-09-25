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
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validateMaleSexualOffencesInconsistentFields
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validateOSPIIC

@Service
class OSPIICRiskProducerService : BaseRiskScoreProducer() {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val errors = validateOSPIIC(request)

    if (errors.isNotEmpty()) {
      return applyErrorsToContextAndReturn(context, errors)
    }
    return context.apply {
      when (request.gender!!) {
        Gender.FEMALE -> {
          OSPIIC = OSPIICObject(RiskBand.NOT_APPLICABLE, 0.0, true, request.hasEverCommittedSexualOffence, errors)
        }
        Gender.MALE -> {
          val maleErrors = mutableListOf<ValidationErrorResponse>()
          validateMaleSexualOffencesInconsistentFields(request, maleErrors)
          if (maleErrors.isNotEmpty()) {
            OSPIIC = OSPIICObject(
              RiskBand.NOT_APPLICABLE,
              0.0,
              false,
              request.hasEverCommittedSexualOffence,
              maleErrors,
            )
          } else {
            val validInput = OSPIICInputValidated(
              totalContactAdultSexualSanctions = request.totalContactAdultSexualSanctions!!,
              totalContactChildSexualSanctions = request.totalContactChildSexualSanctions!!,
              totalIndecentImageSanctions = request.totalIndecentImageSanctions!!,
              totalNonContactSexualOffences = request.totalNonContactSexualOffences!!,
            )
            OSPIIC = run {
              val hierarchyBand = ospiicHierarchyBand(validInput)
              OSPIICObject(hierarchyBand.band, hierarchyBand.rsrContribution, false, request.hasEverCommittedSexualOffence, emptyList())
            }
          }
        }
      }
    }
  }

  override fun applyErrorsToContextAndReturn(
    context: RiskScoreContext,
    validationErrorResponses: List<ValidationErrorResponse>,
  ): RiskScoreContext = context.apply { OSPIIC = OSPIICObject(null, null, null, null, validationErrorResponses) }
}
