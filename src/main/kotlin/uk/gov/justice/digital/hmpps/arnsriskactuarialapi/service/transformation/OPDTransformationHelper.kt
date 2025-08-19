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

fun financialRelianceOnOthersOffendersScore(request: OPDRequestValidated) = scoreFromProblemLevel(request.financialRelianceOnOthers)

fun manipulativePredatoryBehaviourOffendersScore(request: OPDRequestValidated) = scoreFromProblemLevel(request.manipulativePredatoryBehaviour)

fun recklessnessAndRiskTakingBehaviourOffendersScoreOpd(request: OPDRequestValidated) = scoreFromProblemLevel(request.recklessnessAndRiskTakingBehaviour)

fun childhoodBehaviourOffendersScore(request: OPDRequestValidated) = scoreFromBoolean(request.childhoodBehaviour)

fun impulsivityProblemsOffendersScore(request: OPDRequestValidated) = scoreFromProblemLevel(request.impulsivityProblems)

fun presenceOfChildhoodDifficultiesOffendersScore(request: OPDRequestValidated) = if (scoreFromProblemLevel(request.experienceOfChildhood) == 1 || request.childhoodBehaviour) 1 else 0

fun controllingOrAggressiveBehaviourOffendersScore(request: OPDRequestValidated) = scoreFromProblemLevel(request.controllingOrAggressiveBehaviour)

fun historyOfMentalHealthDifficultiesOffendersScore(request: OPDRequestValidated): Int {
  val hasProblems =
    request.currentPsychologicalProblems in listOf(ProblemLevel.SOME_PROBLEMS, ProblemLevel.SIGNIFICANT_PROBLEMS) ||
      request.currentPsychiatricProblems in listOf(ProblemLevel.SOME_PROBLEMS, ProblemLevel.SIGNIFICANT_PROBLEMS) ||
      request.historyOfPsychiatricTreatment ||
      request.medicationMentalHealth ||
      request.patientSecureUnitOrHospital ||
      request.hasCurrentPsychiatricTreatment == true ||
      request.obsessiveBehaviour

  return if (hasProblems) 1 else 0
}

fun selfHarmSuicideAttemptOffendersScore(input: OPDRequestValidated) = if (input.selfHarmSuicideAttempt == true) 1 else 0

fun severeChallengingBehavioursOffendersScore(input: OPDRequestValidated): Int {
  val hasAttitudeProblems =
    input.attitudeTowardsSupervision in listOf(ProblemLevel.SOME_PROBLEMS, ProblemLevel.SIGNIFICANT_PROBLEMS)

  val hasOtherRiskFlags = listOf(
    input.assaultedOrThreatenedStaff,
    input.escapeOrAbsconded,
    input.controlIssuesOrBreachOfTrust,
  ).any { it == true }

  return if (hasAttitudeProblems || hasOtherRiskFlags) 1 else 0
}

fun didOffenceInvolveCarryingOrUsingWeaponOffendersScore(request: OPDRequestValidated) = scoreFromBoolean(request.didOffenceInvolveCarryingOrUsingWeapon)

fun didOffenceInvolveArsonOffendersScore(request: OPDRequestValidated) = scoreFromBoolean(request.didOffenceInvolveArson)

fun offenceMotivationEmotionalStateOffendersScore(request: OPDRequestValidated) = scoreFromBoolean(request.offenceMotivationEmotionalState)

fun offenceLinkedRiskOfSeriousHarmOffendersScore(request: OPDRequestValidated) = scoreFromBoolean(request.offenceLinkedRiskOfSeriousHarm)

fun accommodationLinkedRiskOfSeriousHarmOffendersScore(request: OPDRequestValidated) = scoreFromBoolean(request.accommodationLinkedRiskOfSeriousHarm)

fun experienceOfChildhoodOffendersScore(request: OPDRequestValidated) = scoreFromProblemLevel(request.experienceOfChildhood)

fun relationshipLinkedSeriousHarmOffendersScore(request: OPDRequestValidated) = scoreFromBoolean(request.relationshipLinkedSeriousHarm)

fun difficultiesCopingOffendersScore(request: OPDRequestValidated) = scoreFromProblemLevel(request.difficultiesCoping)

fun currentPsychologicalProblemsOffendersScore(request: OPDRequestValidated) = scoreFromProblemLevel(request.currentPsychologicalProblems)

fun wellbeingEmotionalLinkedRiskOfSeriousHarmOffendersScore(request: OPDRequestValidated) = scoreFromBoolean(request.wellbeingEmotionalLinkedRiskOfSeriousHarm)

fun thinkingAndBehaviourLinedToRiskOfSeriousHarmOffendersScore(request: OPDRequestValidated) = scoreFromBoolean(request.thinkingAndBehaviourLinedToRiskOfSeriousHarm)

fun domesticAbuseOffendersScore(request: OPDRequestValidated): Int = when {
  !request.domesticAbuse -> 0
  (request.domesticAbusePartner == true) || (request.domesticAbuseFamily == true) -> 1
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
