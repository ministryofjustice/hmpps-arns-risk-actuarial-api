package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd.OPDObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd.OPDRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd.OPDResult
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.accommodationLinkedRiskOfSeriousHarmOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ageAtFirstSanctionOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.allUnansweredQuestion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.attitudesStableBehaviourOffendersScoreOpd
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.carryingOrUsingWeaponOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.childhoodBehaviourOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.controllingBehaviourOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.currentPsychologicalProblemsOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.difficultiesCopingOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.domesticAbuseOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.excessiveOrSadisticViolenceOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.experienceOfChildhoodOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.financialRelianceOnOthersOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.historyOfMentalHealthDifficultiesOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.impactOfOffendingOnOthersOffendersScoreOpd
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.impulsivityBehaviourOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.manipulativePredatoryBehaviourOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.offenceArsonOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.offenceLinkedRiskOfSeriousHarmOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.offenderMotivationsOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.presenceOfChildhoodDifficultiesOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.relationshipLinkedSeriousHarmOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.selfHarmSuicideAttemptOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.severeChallengingBehavioursOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.thinkingAndBehaviourLinedToRiskOfSeriousHarmOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.violenceOrThreatOfViolenceOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.wellbeingEmotionalLinkedRiskOfSeriousHarmOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.opdInitialValidation

private const val ODP_MALE_PERSONALITY_SCORE_MIN = 7
private const val ODP_MALE_INDICATOR_SCORE_MIN = 2
private const val ODP_FEMALE_SCORE_MIN = 10

@Service
class OPDRiskProducerService : RiskScoreProducer {

  @Autowired
  lateinit var offenceGroupParametersService: OffenceGroupParametersService

  override fun getRiskScore(
    request: RiskScoreRequest,
    context: RiskScoreContext,
  ): RiskScoreContext {
    try {
      // validation
      val errors = opdInitialValidation(request)
      if (errors.isNotEmpty()) {
        return invalidInformationResult(context, errors)
      }

      val validatedRequest = mapRequestValidated(request)

      // checks
      if (!isOpdApplicable(validatedRequest)) {
        return notApplicableResult(context)
      }
      if (hasAllMaleQuestionsUnanswered(request) || hasAllFemaleQuestionsUnanswered(request)) {
        return notApplicableResult(context)
      }

      return getOPDResult(validatedRequest, context)
    } catch (error: Throwable) {
      return errorResult(error, context)
    }
  }

  private fun mapRequestValidated(request: RiskScoreRequest): OPDRequestValidated = OPDRequestValidated(
    overallRiskForAssessment = request.overallRiskForAssessment!!,
    highestRiskLevel = request.highestRiskLevel!!,
    gender = request.gender!!,
    currentOffence = request.currentOffence!!,
    custodialSentence = request.custodialSentence!!,
    currentPsychologicalProblems = request.currentPsychologicalProblems,
    experienceOfChildhood = request.experienceOfChildhood,
    difficultiesCoping = request.difficultiesCoping,
    domesticAbusePartner = request.domesticAbusePartner,
    domesticAbuseFamily = request.domesticAbuseFamily,
    ageAtFirstSanction = request.ageAtFirstSanction?.toInt(),
    financialRelianceOnOthers = request.financialRelianceOnOthers,
    manipulativePredatoryBehaviour = request.manipulativePredatoryBehaviour,
    childhoodBehaviour = request.childhoodBehaviour,
    currentPsychiatricProblems = request.currentPsychiatricProblems,
    attitudesStableBehaviour = request.attitudesStableBehaviour,
    impulsivityBehaviour = request.impulsivityBehaviour,
    attitudeTowardsSupervision = request.attitudeTowardsSupervision,
    currentPsychiatricTreatmentOrPending = request.currentPsychiatricTreatmentOrPending,
    controllingBehaviour = request.controllingBehaviour,
    opdOverride = request.opdOverride ?: false,
    eligibleForMappa = request.eligibleForMappa ?: false,
    carryingOrUsingWeapon = request.carryingOrUsingWeapon ?: false,
    violenceOrThreatOfViolence = request.violenceOrThreatOfViolence ?: false,
    excessiveOrSadisticViolence = request.excessiveOrSadisticViolence ?: false,
    offenceArson = request.offenceArson ?: false,
    offenderMotivations = request.offenderMotivations ?: false,
    offenceLinkedRiskOfSeriousHarm = request.offenceLinkedRiskOfSeriousHarm ?: false,
    accommodationLinkedRiskOfSeriousHarm = request.accommodationLinkedRiskOfSeriousHarm ?: false,
    domesticAbuse = request.domesticAbuse ?: false,
    relationshipLinkedSeriousHarm = request.relationshipLinkedSeriousHarm ?: false,
    wellbeingEmotionalLinkedRiskOfSeriousHarm = request.wellbeingEmotionalLinkedRiskOfSeriousHarm ?: false,
    thinkingAndBehaviourLinedToRiskOfSeriousHarm = request.thinkingAndBehaviourLinedToRiskOfSeriousHarm
      ?: false,
    historyOfPsychiatricTreatment = request.historyOfPsychiatricTreatment ?: false,
    medicationMentalHealth = request.medicationMentalHealth ?: false,
    patientSecureUnitOrHospital = request.patientSecureUnitOrHospital ?: false,
    obsessiveBehaviour = request.obsessiveBehaviour ?: false,
    selfHarmSuicideAttempt = request.selfHarmSuicideAttempt ?: false,
    concernsAboutSuicidePast = request.concernsAboutSuicidePast ?: false,
    concernsAboutSelfHarmPast = request.concernsAboutSelfHarmPast ?: false,
    assaultedOrThreatenedStaff = request.assaultedOrThreatenedStaff ?: false,
    escapeOrAbsconded = request.escapeOrAbsconded ?: false,
    controlIssues = request.controlIssues ?: false,
    breachOfTrust = request.breachOfTrust ?: false,
    impactOfOffendingOnOthers = request.impactOfOffendingOnOthers ?: false,
  )

  /**
   * Minimum set of criteria to progress with scoring
   */
  private fun isOpdApplicable(validatedRequest: OPDRequestValidated): Boolean {
    val isViolentOrSexualType =
      offenceGroupParametersService.isViolentOrSexualType(validatedRequest.currentOffence)

    val isOpdApplicable = when (validatedRequest.gender) {
      Gender.MALE -> isOpdApplicableMale(validatedRequest, isViolentOrSexualType)
      Gender.FEMALE -> isOpdApplicableFemale(validatedRequest)
    }
    return isOpdApplicable
  }

  private fun isOpdApplicableMale(
    request: OPDRequestValidated,
    isViolentOrSexualType: Boolean,
  ): Boolean = (request.overallRiskForAssessment in arrayOf(RiskBand.HIGH, RiskBand.VERY_HIGH)) &&
    isViolentOrSexualType &&
    request.custodialSentence

  private fun isOpdApplicableFemale(
    request: OPDRequestValidated,
  ): Boolean = (request.overallRiskForAssessment in arrayOf(RiskBand.HIGH, RiskBand.VERY_HIGH)) ||
    request.eligibleForMappa

  /**
   * Validation failed, will contain errors.
   */
  private fun invalidInformationResult(
    context: RiskScoreContext,
    errors: List<ValidationErrorResponse>,
  ): RiskScoreContext = context.copy(
    OPD = OPDObject(
      opdCheck = false,
      opdResult = null,
      validationError = errors,
    ),
  )

  /**
   * Based on information provided or lack thereof, the OPD is not applicable, does not contain errors.
   */
  private fun notApplicableResult(
    context: RiskScoreContext,
  ): RiskScoreContext = context.copy(
    OPD = OPDObject(
      opdCheck = false,
      opdResult = null,
      validationError = emptyList(),
    ),
  )

  /**
   * OPD calculations for both MALE and FEMALE.
   */
  private fun getOPDResult(
    validatedRequest: OPDRequestValidated,
    context: RiskScoreContext,
  ): RiskScoreContext {
    val opdResult: OPDResult? = when (validatedRequest.gender) {
      Gender.MALE -> {
        val opdMalePersonalityScore =
          listOf(
            ageAtFirstSanctionOffendersScore(validatedRequest),
            violenceOrThreatOfViolenceOffendersScore(validatedRequest),
            excessiveOrSadisticViolenceOffendersScore(validatedRequest),
            impactOfOffendingOnOthersOffendersScoreOpd(validatedRequest),
            financialRelianceOnOthersOffendersScore(validatedRequest),
            manipulativePredatoryBehaviourOffendersScore(validatedRequest),
            attitudesStableBehaviourOffendersScoreOpd(validatedRequest),
            childhoodBehaviourOffendersScore(validatedRequest),
            impulsivityBehaviourOffendersScore(validatedRequest),
            controllingBehaviourOffendersScore(validatedRequest),
          ).sum()

        val opdMaleIndicatorScore =
          listOf(
            presenceOfChildhoodDifficultiesOffendersScore(validatedRequest),
            historyOfMentalHealthDifficultiesOffendersScore(validatedRequest),
            selfHarmSuicideAttemptOffendersScore(validatedRequest),
            severeChallengingBehavioursOffendersScore(validatedRequest),
          ).sum()

        if (opdMalePersonalityScore >= ODP_MALE_PERSONALITY_SCORE_MIN ||
          opdMaleIndicatorScore >= ODP_MALE_INDICATOR_SCORE_MIN ||
          validatedRequest.opdOverride
        ) {
          OPDResult.SCREEN_IN
        } else {
          OPDResult.SCREEN_OUT
        }
      }

      Gender.FEMALE -> {
        val opdFemaleScore = listOf(
          carryingOrUsingWeaponOffendersScore(validatedRequest),
          violenceOrThreatOfViolenceOffendersScore(validatedRequest),
          excessiveOrSadisticViolenceOffendersScore(validatedRequest),
          offenceArsonOffendersScore(validatedRequest),
          offenderMotivationsOffendersScore(validatedRequest),
          offenceLinkedRiskOfSeriousHarmOffendersScore(validatedRequest),
          accommodationLinkedRiskOfSeriousHarmOffendersScore(validatedRequest),
          experienceOfChildhoodOffendersScore(validatedRequest),
          domesticAbuseOffendersScore(validatedRequest),
          relationshipLinkedSeriousHarmOffendersScore(validatedRequest),
          difficultiesCopingOffendersScore(validatedRequest),
          currentPsychologicalProblemsOffendersScore(validatedRequest),
          selfHarmSuicideAttemptOffendersScore(validatedRequest),
          wellbeingEmotionalLinkedRiskOfSeriousHarmOffendersScore(validatedRequest),
          thinkingAndBehaviourLinedToRiskOfSeriousHarmOffendersScore(validatedRequest),
        ).sum()

        if (opdFemaleScore >= ODP_FEMALE_SCORE_MIN ||
          validatedRequest.opdOverride
        ) {
          OPDResult.SCREEN_IN
        } else {
          OPDResult.SCREEN_OUT
        }
      }
    }

    return context.copy(
      OPD = OPDObject(
        opdCheck = true,
        opdResult = opdResult,
        validationError = emptyList(),
      ),
    )
  }

  private fun errorResult(
    throwable: Throwable,
    context: RiskScoreContext,
  ): RiskScoreContext {
    val errors = listOf(
      ValidationErrorResponse(
        type = ValidationErrorType.NO_MATCHING_INPUT,
        message = "Error: ${throwable.message}",
        fields = emptyList(),
      ),
    )
    return context.copy(
      OPD = OPDObject(
        opdCheck = false,
        opdResult = null,
        validationError = errors,
      ),
    )
  }

  /**
   * All female related questions are unanswered / null
   */
  private fun hasAllFemaleQuestionsUnanswered(request: RiskScoreRequest): Boolean = request.gender == Gender.FEMALE &&
    allUnansweredQuestion(
      listOf(
        request.carryingOrUsingWeapon,
        request.violenceOrThreatOfViolence,
        request.excessiveOrSadisticViolence,
        request.offenceArson,
        request.offenderMotivations,
        request.offenceLinkedRiskOfSeriousHarm,
        request.accommodationLinkedRiskOfSeriousHarm,
        request.experienceOfChildhood,
        request.domesticAbuse,
        request.relationshipLinkedSeriousHarm,
      ),
    )

  /**
   * All male related questions are unanswered / null
   */
  private fun hasAllMaleQuestionsUnanswered(
    request: RiskScoreRequest,
  ): Boolean = request.gender == Gender.MALE &&
    allUnansweredQuestion(
      listOf(
        request.ageAtFirstSanction,
        request.violenceOrThreatOfViolence,
        request.excessiveOrSadisticViolence,
        request.impactOfOffendingOnOthers,
        request.financialRelianceOnOthers,
        request.manipulativePredatoryBehaviour,
        request.attitudesStableBehaviour,
        request.childhoodBehaviour,
        request.impulsivityBehaviour,
        request.controllingBehaviour,
        request.experienceOfChildhood,
        request.currentPsychologicalProblems,
        request.currentPsychiatricProblems,
        request.historyOfPsychiatricTreatment,
        request.medicationMentalHealth,
        request.patientSecureUnitOrHospital,
        request.currentPsychiatricTreatmentOrPending,
        request.obsessiveBehaviour,
        request.selfHarmSuicideAttempt,
        request.concernsAboutSuicidePast,
        request.concernsAboutSelfHarmPast,
        request.attitudeTowardsSupervision,
        request.assaultedOrThreatenedStaff,
        request.escapeOrAbsconded,
        request.controlIssues,
        request.breachOfTrust,
      ),
    )
}
