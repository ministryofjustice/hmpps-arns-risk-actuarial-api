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
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validateOSP

@Service
class OSPIICRiskProducerService : BaseRiskScoreProducer() {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val errors = validateOSP(request, false)

    if (errors.isNotEmpty()) {
      return applyErrorsToContextAndReturn(context, errors)
    }

    if (request.gender == Gender.FEMALE) {
      return context.apply {
        OSPIIC = OSPIICObject(RiskBand.NOT_APPLICABLE, 0.0, true, request.hasEverCommittedSexualOffence, errors)
      }
    }

    if (request.hasEverCommittedSexualOffence == false) {
      return context.apply {
        OSPIIC = OSPIICObject(RiskBand.NOT_APPLICABLE, 0.0, false, request.hasEverCommittedSexualOffence, errors)
      }
    }

    return context.apply {
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

  override fun applyErrorsToContextAndReturn(
    context: RiskScoreContext,
    validationErrorResponses: List<ValidationErrorResponse>,
  ): RiskScoreContext = context.apply { OSPIIC = OSPIICObject(null, null, null, null, validationErrorResponses) }
}
