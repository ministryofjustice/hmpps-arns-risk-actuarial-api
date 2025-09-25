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
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ageAtFirstSanctionOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.allUnansweredQuestion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.areEmotionalIssuesLinkedToRiskOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.areThinkingAndBehaviourIssuesLinkedToRiskOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.controllingOrAggressiveBehaviourOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.currentPsychologicalProblemsOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.didOffenceInvolveArsonOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.didOffenceInvolveCarryingOrUsingWeaponOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.didOffenceInvolveExcessiveUseOfViolenceOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.didOffenceInvolveViolenceOrThreatOfViolenceOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.difficultiesCopingOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.doesRecogniseImpactOfOffendingOnOthersOffendersScoreOpd
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.domesticAbuseOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.experienceOfChildhoodOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.hasAccommodationIssuesLinkedToRiskOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.hasSelfHarmOrAttemptedSuicideOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.historyOfMentalHealthDifficultiesOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.impulsivityProblemsOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isAnalysisOfOffenceIssuesLinkedToRiskOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isEvidenceOfChildhoodBehaviouralProblemsOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.manipulativeOrPredatoryBehaviourOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.offenceMotivationEmotionalStateOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.overRelianceOnOthersForFinancialSupportOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.presenceOfChildhoodDifficultiesOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.recklessnessAndRiskTakingBehaviourOffendersScoreOpd
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.relationshipIssuesLinkedToRiskOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.severeChallengingBehavioursOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validateOPD

private const val ODP_MALE_PERSONALITY_SCORE_MIN = 7
private const val ODP_MALE_INDICATOR_SCORE_MIN = 2
private const val ODP_FEMALE_SCORE_MIN = 10

@Service
class OPDRiskProducerService : BaseRiskScoreProducer() {

  @Autowired
  lateinit var offenceGroupParametersService: OffenceGroupParametersService

  override fun getRiskScore(
    request: RiskScoreRequest,
    context: RiskScoreContext,
  ): RiskScoreContext {
    // validation
    val errors = validateOPD(request)
    if (errors.isNotEmpty()) {
      return invalidInformationResult(context, errors)
    }

    val validatedRequest = mapRequestValidated(request)

    // checks
    if (hasAllMaleQuestionsUnanswered(request) || hasAllFemaleQuestionsUnanswered(request)) {
      return notApplicableResult(context)
    }
    val isViolentOrSexualType =
      offenceGroupParametersService.isViolentOrSexualType(validatedRequest.currentOffenceCode)
        ?: return invalidInformationResult(
          context,
          listOf(
            ValidationErrorType.OFFENCE_CODE_MAPPING_NOT_FOUND.asErrorResponse(
              listOf(RiskScoreRequest::currentOffenceCode.name),
            ),
          ),
        )

    if (!isOpdApplicable(validatedRequest, isViolentOrSexualType)) {
      return screenOutResult(context)
    }

    return getOPDResult(validatedRequest, context)
  }

  override fun applyErrorsToContextAndReturn(
    context: RiskScoreContext,
    validationErrorResponses: List<ValidationErrorResponse>,
  ): RiskScoreContext = context.apply {
    OPD = OPDObject(
      opdCheck = false,
      opdResult = null,
      validationError = validationErrorResponses,
    )
  }

  private fun mapRequestValidated(request: RiskScoreRequest): OPDRequestValidated = OPDRequestValidated(
    overallRiskForAssessment = request.overallRiskForAssessment!!,
    highestRiskLevelOverAllAssessments = request.highestRiskLevelOverAllAssessments!!,
    gender = request.gender!!,
    currentOffenceCode = request.currentOffenceCode!!,
    hasCustodialSentence = request.hasCustodialSentence!!,
    currentPsychologicalProblems = request.currentPsychologicalProblems,
    experienceOfChildhood = request.experienceOfChildhood,
    difficultiesCoping = request.difficultiesCoping,
    domesticAbuseAgainstPartner = request.domesticAbuseAgainstPartner,
    domesticAbuseAgainstFamily = request.domesticAbuseAgainstFamily,
    ageAtFirstSanction = request.ageAtFirstSanction?.toInt(),
    overRelianceOnOthersForFinancialSupport = request.overRelianceOnOthersForFinancialSupport,
    manipulativeOrPredatoryBehaviour = request.manipulativeOrPredatoryBehaviour,
    isEvidenceOfChildhoodBehaviouralProblems = request.isEvidenceOfChildhoodBehaviouralProblems ?: false,
    currentPsychiatricProblems = request.currentPsychiatricProblems,
    recklessnessAndRiskTakingBehaviour = request.recklessnessAndRiskTakingBehaviour,
    impulsivityProblems = request.impulsivityProblems,
    attitudeTowardsSupervisionOrLicence = request.attitudeTowardsSupervisionOrLicence,
    hasCurrentPsychiatricTreatment = request.hasCurrentPsychiatricTreatment,
    controllingOrAggressiveBehaviour = request.controllingOrAggressiveBehaviour,
    applyOPDOverride = request.applyOPDOverride ?: false,
    isEligibleForMappa = request.isEligibleForMappa ?: false,
    didOffenceInvolveCarryingOrUsingWeapon = request.didOffenceInvolveCarryingOrUsingWeapon ?: false,
    didOffenceInvolveViolenceOrThreatOfViolence = request.didOffenceInvolveViolenceOrThreatOfViolence ?: false,
    didOffenceInvolveExcessiveUseOfViolence = request.didOffenceInvolveExcessiveUseOfViolence ?: false,
    didOffenceInvolveArson = request.didOffenceInvolveArson ?: false,
    offenceMotivationEmotionalState = request.offenceMotivationEmotionalState ?: false,
    isAnalysisOfOffenceIssuesLinkedToRisk = request.isAnalysisOfOffenceIssuesLinkedToRisk ?: false,
    hasAccommodationIssuesLinkedToRisk = request.hasAccommodationIssuesLinkedToRisk ?: false,
    evidenceOfDomesticAbuse = request.evidenceOfDomesticAbuse ?: false,
    relationshipIssuesLinkedToRisk = request.relationshipIssuesLinkedToRisk ?: false,
    areEmotionalIssuesLinkedToRisk = request.areEmotionalIssuesLinkedToRisk ?: false,
    areThinkingAndBehaviourIssuesLinkedToRisk = request.areThinkingAndBehaviourIssuesLinkedToRisk
      ?: false,
    hasHistoryOfPsychiatricTreatment = request.hasHistoryOfPsychiatricTreatment ?: false,
    hasBeenOnMedicationForMentalHealthProblems = request.hasBeenOnMedicationForMentalHealthProblems ?: false,
    hasEverBeenInSpecialHospitalOrRegionalSecureUnit = request.hasEverBeenInSpecialHospitalOrRegionalSecureUnit ?: false,
    hasDisplayedObsessiveBehaviourLinkedToOffending = request.hasDisplayedObsessiveBehaviourLinkedToOffending ?: false,
    hasSelfHarmOrAttemptedSuicide = request.hasSelfHarmOrAttemptedSuicide ?: false,
    hasAssaultedOrThreatenedStaff = request.hasAssaultedOrThreatenedStaff ?: false,
    hasEscapedOrAbsconded = request.hasEscapedOrAbsconded ?: false,
    hasControlIssues = request.hasControlIssues ?: false,
    doesRecogniseImpactOfOffendingOnOthers = request.doesRecogniseImpactOfOffendingOnOthers ?: false,
  )

  /**
   * Minimum set of criteria to progress with scoring
   */
  private fun isOpdApplicable(validatedRequest: OPDRequestValidated, isViolentOrSexualType: Boolean): Boolean {
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
    request.hasCustodialSentence

  private fun isOpdApplicableFemale(
    request: OPDRequestValidated,
  ): Boolean = (request.overallRiskForAssessment in arrayOf(RiskBand.HIGH, RiskBand.VERY_HIGH)) ||
    request.isEligibleForMappa

  /**
   * Validation failed, will contain errors.
   */
  private fun invalidInformationResult(
    context: RiskScoreContext,
    errors: List<ValidationErrorResponse>,
  ): RiskScoreContext = context.apply { OPD = OPDObject(opdCheck = false, opdResult = null, validationError = errors) }

  /**
   * Based on information provided or lack thereof, the OPD is not applicable, does not contain errors.
   */
  private fun notApplicableResult(
    context: RiskScoreContext,
  ): RiskScoreContext = context.apply {
    OPD = OPDObject(opdCheck = false, opdResult = null, validationError = emptyList())
  }

  private fun screenOutResult(
    context: RiskScoreContext,
  ): RiskScoreContext = context.apply {
    OPD = OPDObject(opdCheck = true, opdResult = OPDResult.SCREEN_OUT, validationError = emptyList())
  }

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
            didOffenceInvolveViolenceOrThreatOfViolenceOffendersScore(validatedRequest),
            didOffenceInvolveExcessiveUseOfViolenceOffendersScore(validatedRequest),
            doesRecogniseImpactOfOffendingOnOthersOffendersScoreOpd(validatedRequest),
            overRelianceOnOthersForFinancialSupportOffendersScore(validatedRequest),
            manipulativeOrPredatoryBehaviourOffendersScore(validatedRequest),
            recklessnessAndRiskTakingBehaviourOffendersScoreOpd(validatedRequest),
            isEvidenceOfChildhoodBehaviouralProblemsOffendersScore(validatedRequest),
            impulsivityProblemsOffendersScore(validatedRequest),
            controllingOrAggressiveBehaviourOffendersScore(validatedRequest),
          ).sum()

        val opdMaleIndicatorScore =
          listOf(
            presenceOfChildhoodDifficultiesOffendersScore(validatedRequest),
            historyOfMentalHealthDifficultiesOffendersScore(validatedRequest),
            hasSelfHarmOrAttemptedSuicideOffendersScore(validatedRequest),
            severeChallengingBehavioursOffendersScore(validatedRequest),
          ).sum()

        if (opdMalePersonalityScore >= ODP_MALE_PERSONALITY_SCORE_MIN ||
          opdMaleIndicatorScore >= ODP_MALE_INDICATOR_SCORE_MIN
        ) {
          OPDResult.SCREEN_IN
        } else {
          OPDResult.SCREEN_OUT
        }
      }

      Gender.FEMALE -> {
        val opdFemaleScore = listOf(
          didOffenceInvolveCarryingOrUsingWeaponOffendersScore(validatedRequest),
          didOffenceInvolveViolenceOrThreatOfViolenceOffendersScore(validatedRequest),
          didOffenceInvolveExcessiveUseOfViolenceOffendersScore(validatedRequest),
          didOffenceInvolveArsonOffendersScore(validatedRequest),
          offenceMotivationEmotionalStateOffendersScore(validatedRequest),
          isAnalysisOfOffenceIssuesLinkedToRiskOffendersScore(validatedRequest),
          hasAccommodationIssuesLinkedToRiskOffendersScore(validatedRequest),
          experienceOfChildhoodOffendersScore(validatedRequest),
          domesticAbuseOffendersScore(validatedRequest),
          relationshipIssuesLinkedToRiskOffendersScore(validatedRequest),
          difficultiesCopingOffendersScore(validatedRequest),
          currentPsychologicalProblemsOffendersScore(validatedRequest),
          hasSelfHarmOrAttemptedSuicideOffendersScore(validatedRequest),
          areEmotionalIssuesLinkedToRiskOffendersScore(validatedRequest),
          areThinkingAndBehaviourIssuesLinkedToRiskOffendersScore(validatedRequest),
        ).sum()

        if (opdFemaleScore >= ODP_FEMALE_SCORE_MIN
        ) {
          OPDResult.SCREEN_IN
        } else {
          OPDResult.SCREEN_OUT
        }
      }
    }
    val opdOverride = if (opdResult == OPDResult.SCREEN_OUT) {
      validatedRequest.applyOPDOverride
    } else {
      null
    }
    return context.apply {
      OPD = OPDObject(opdCheck = true, opdResult = opdResult, opdOverride = opdOverride, validationError = emptyList())
    }
  }

  /**
   * All female related questions are unanswered / null
   */
  private fun hasAllFemaleQuestionsUnanswered(request: RiskScoreRequest): Boolean = request.gender == Gender.FEMALE &&
    allUnansweredQuestion(
      listOf(
        request.didOffenceInvolveCarryingOrUsingWeapon,
        request.didOffenceInvolveViolenceOrThreatOfViolence,
        request.didOffenceInvolveExcessiveUseOfViolence,
        request.didOffenceInvolveArson,
        request.offenceMotivationEmotionalState,
        request.isAnalysisOfOffenceIssuesLinkedToRisk,
        request.hasAccommodationIssuesLinkedToRisk,
        request.experienceOfChildhood,
        request.evidenceOfDomesticAbuse,
        request.relationshipIssuesLinkedToRisk,
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
        request.didOffenceInvolveViolenceOrThreatOfViolence,
        request.didOffenceInvolveExcessiveUseOfViolence,
        request.doesRecogniseImpactOfOffendingOnOthers,
        request.overRelianceOnOthersForFinancialSupport,
        request.manipulativeOrPredatoryBehaviour,
        request.recklessnessAndRiskTakingBehaviour,
        request.isEvidenceOfChildhoodBehaviouralProblems,
        request.impulsivityProblems,
        request.controllingOrAggressiveBehaviour,
        request.experienceOfChildhood,
        request.currentPsychologicalProblems,
        request.currentPsychiatricProblems,
        request.hasHistoryOfPsychiatricTreatment,
        request.hasBeenOnMedicationForMentalHealthProblems,
        request.hasEverBeenInSpecialHospitalOrRegionalSecureUnit,
        request.hasCurrentPsychiatricTreatment,
        request.hasDisplayedObsessiveBehaviourLinkedToOffending,
        request.hasSelfHarmOrAttemptedSuicide,
        request.attitudeTowardsSupervisionOrLicence,
        request.hasAssaultedOrThreatenedStaff,
        request.hasEscapedOrAbsconded,
        request.hasControlIssues,
      ),
    )
}
