package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd.OPDRequestValidated

fun ageAtFirstSanctionOffendersScore(request: OPDRequestValidated) = when (request.ageAtFirstSanction) {
  in 0..17 -> 1
  else -> 0
}

fun didOffenceInvolveViolenceOrThreatOfViolenceOffendersScore(request: OPDRequestValidated) = scoreFromBoolean(request.didOffenceInvolveViolenceOrThreatOfViolence)

fun didOffenceInvolveExcessiveUseOfViolenceOffendersScore(request: OPDRequestValidated) = when (request.didOffenceInvolveExcessiveUseOfViolence) {
  true -> if (request.didOffenceInvolveViolenceOrThreatOfViolence) {
    1
  } else {
    2
  }

  false -> 0
}

fun doesRecogniseImpactOfOffendingOnOthersOffendersScoreOpd(request: OPDRequestValidated) = invertedScoreFromBoolean(request.doesRecogniseImpactOfOffendingOnOthers)

fun overRelianceOnOthersForFinancialSupportOffendersScore(request: OPDRequestValidated) = scoreFromProblemLevel(request.overRelianceOnOthersForFinancialSupport)

fun manipulativeOrPredatoryBehaviourOffendersScore(request: OPDRequestValidated) = scoreFromProblemLevel(request.manipulativeOrPredatoryBehaviour)

fun recklessnessAndRiskTakingBehaviourOffendersScoreOpd(request: OPDRequestValidated) = scoreFromProblemLevel(request.recklessnessAndRiskTakingBehaviour)

fun isEvidenceOfChildhoodBehaviouralProblemsOffendersScore(request: OPDRequestValidated) = scoreFromBoolean(request.isEvidenceOfChildhoodBehaviouralProblems)

fun impulsivityProblemsOffendersScore(request: OPDRequestValidated) = scoreFromProblemLevel(request.impulsivityProblems)

fun presenceOfChildhoodDifficultiesOffendersScore(request: OPDRequestValidated) = if (scoreFromProblemLevel(request.experienceOfChildhood) == 1 || request.isEvidenceOfChildhoodBehaviouralProblems) 1 else 0

fun controllingOrAggressiveBehaviourOffendersScore(request: OPDRequestValidated) = scoreFromProblemLevel(request.controllingOrAggressiveBehaviour)

fun historyOfMentalHealthDifficultiesOffendersScore(request: OPDRequestValidated): Int {
  val hasProblems =
    request.currentPsychologicalProblems in listOf(ProblemLevel.SOME_PROBLEMS, ProblemLevel.SIGNIFICANT_PROBLEMS) ||
      request.currentPsychiatricProblems in listOf(ProblemLevel.SOME_PROBLEMS, ProblemLevel.SIGNIFICANT_PROBLEMS) ||
      request.hasHistoryOfPsychiatricTreatment ||
      request.hasBeenOnMedicationForMentalHealthProblems ||
      request.hasEverBeenInSpecialHospitalOrRegionalSecureUnit ||
      request.hasCurrentPsychiatricTreatment == true ||
      request.hasDisplayedObsessiveBehaviourLinkedToOffending

  return if (hasProblems) 1 else 0
}

fun hasSelfHarmOrAttemptedSuicideOffendersScore(input: OPDRequestValidated) = if (input.hasSelfHarmOrAttemptedSuicide == true) 1 else 0

fun severeChallengingBehavioursOffendersScore(input: OPDRequestValidated): Int {
  val hasAttitudeProblems =
    input.attitudeTowardsSupervisionOrLicence in listOf(ProblemLevel.SOME_PROBLEMS, ProblemLevel.SIGNIFICANT_PROBLEMS)

  val hasOtherRiskFlags = listOf(
    input.hasAssaultedOrThreatenedStaff,
    input.hasEscapedOrAbsconded,
    input.hasControlIssues,
  ).any { it == true }

  return if (hasAttitudeProblems || hasOtherRiskFlags) 1 else 0
}

fun didOffenceInvolveCarryingOrUsingWeaponOffendersScore(request: OPDRequestValidated) = scoreFromBoolean(request.didOffenceInvolveCarryingOrUsingWeapon)

fun didOffenceInvolveArsonOffendersScore(request: OPDRequestValidated) = scoreFromBoolean(request.didOffenceInvolveArson)

fun offenceMotivationEmotionalStateOffendersScore(request: OPDRequestValidated) = scoreFromBoolean(request.offenceMotivationEmotionalState)

fun isAnalysisOfOffenceIssuesLinkedToRiskOffendersScore(request: OPDRequestValidated) = scoreFromBoolean(request.isAnalysisOfOffenceIssuesLinkedToRisk)

fun hasAccommodationIssuesLinkedToRiskOffendersScore(request: OPDRequestValidated) = scoreFromBoolean(request.hasAccommodationIssuesLinkedToRisk)

fun experienceOfChildhoodOffendersScore(request: OPDRequestValidated) = scoreFromProblemLevel(request.experienceOfChildhood)

fun relationshipIssuesLinkedToRiskOffendersScore(request: OPDRequestValidated) = scoreFromBoolean(request.relationshipIssuesLinkedToRisk)

fun difficultiesCopingOffendersScore(request: OPDRequestValidated) = scoreFromProblemLevel(request.difficultiesCoping)

fun currentPsychologicalProblemsOffendersScore(request: OPDRequestValidated) = scoreFromProblemLevel(request.currentPsychologicalProblems)

fun areEmotionalIssuesLinkedToRiskOffendersScore(request: OPDRequestValidated) = scoreFromBoolean(request.areEmotionalIssuesLinkedToRisk)

fun areThinkingAndBehaviourIssuesLinkedToRiskOffendersScore(request: OPDRequestValidated) = scoreFromBoolean(request.areThinkingAndBehaviourIssuesLinkedToRisk)

fun domesticAbuseOffendersScore(request: OPDRequestValidated): Int = when {
  !request.evidenceOfDomesticAbuse -> 0
  (request.domesticAbuseAgainstPartner == true) || (request.domesticAbuseAgainstFamily == true) -> 1
  else -> 0
}

fun allUnansweredQuestion(fields: List<Any?>): Boolean = fields.all { it == null }

fun scoreFromProblemLevel(level: ProblemLevel?): Int = when (level) {
  ProblemLevel.NO_PROBLEMS -> 0
  ProblemLevel.SOME_PROBLEMS, ProblemLevel.SIGNIFICANT_PROBLEMS -> 1
  null -> 0
}

fun scoreFromBoolean(boolean: Boolean): Int = when (boolean) {
  true -> 1
  false -> 0
}

fun invertedScoreFromBoolean(boolean: Boolean): Int = when (boolean) {
  true -> 0
  false -> 1
}
