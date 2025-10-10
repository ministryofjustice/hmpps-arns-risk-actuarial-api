package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MSTObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MSTRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.calculateAge
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getMaturityFlag
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.getMstApplicable
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.isNotNullAndInvalidMstAge
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validateMST

@Service
class MSTRiskProducerService : BaseRiskScoreProducer() {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val currentAge: Int? = request.dateOfBirth?.let { calculateAge(it, request.assessmentDate) }

    if (isNotNullAndInvalidMstAge(currentAge)) {
      return context.apply {
        MST = createNonApplicableMstObject()
      }
    }

    val errors = validateMST(request)

    if (errors.isNotEmpty()) {
      return applyErrorsToContextAndReturn(context, errors)
    }

    val validRequest = MSTRequestValidated(
      gender = request.gender!!,
      assessmentDate = request.assessmentDate,
      dateOfBirth = request.dateOfBirth!!,
      hasPeerGroupInfluences = request.hasPeerGroupInfluences,
      influenceFromCriminalAssociates = request.influenceFromCriminalAssociates,
      recklessnessAndRiskTakingBehaviour = request.recklessnessAndRiskTakingBehaviour,
      difficultiesCoping = request.difficultiesCoping,
      attitudesTowardsSelf = request.attitudesTowardsSelf,
      impulsivityProblems = request.impulsivityProblems,
      temperControl = request.temperControl,
      problemSolvingSkills = request.problemSolvingSkills,
      awarenessOfConsequences = request.awarenessOfConsequences,
      understandsOtherPeoplesViews = request.understandsOtherPeoplesViews,
    )

    return context.apply {
      MST = getMstObject(validRequest, errors, currentAge!!)
    }
  }

  override fun applyErrorsToContextAndReturn(
    context: RiskScoreContext,
    validationErrorResponses: List<ValidationErrorResponse>,
  ): RiskScoreContext = context.apply {
    MST = MSTObject(
      null,
      null,
      null,
      validationErrorResponses,
    )
  }

  private fun getMstObject(
    request: MSTRequestValidated,
    errors: List<ValidationErrorResponse>,
    currentAge: Int,
  ): MSTObject {
    val isMstApplicable = getMstApplicable(request.gender, currentAge)

    if (isMstApplicable) {
      val maturityScore = listOf(
        if (request.hasPeerGroupInfluences == true) 1 else 0,
        request.influenceFromCriminalAssociates?.score ?: 0,
        request.recklessnessAndRiskTakingBehaviour?.score ?: 0,
        request.difficultiesCoping?.score ?: 0,
        request.attitudesTowardsSelf?.score ?: 0,
        request.impulsivityProblems?.score ?: 0,
        request.temperControl?.score ?: 0,
        request.problemSolvingSkills?.score ?: 0,
        request.awarenessOfConsequences?.score ?: 0,
        request.understandsOtherPeoplesViews?.score ?: 0,
      ).sum()

      return MSTObject(
        maturityScore,
        getMaturityFlag(maturityScore),
        true,
        errors,
      )
    }

    return createNonApplicableMstObject()
  }

  fun createNonApplicableMstObject(): MSTObject = MSTObject(
    maturityScore = null,
    maturityFlag = false,
    isMstApplicable = false,
    listOf(),
  )
}
