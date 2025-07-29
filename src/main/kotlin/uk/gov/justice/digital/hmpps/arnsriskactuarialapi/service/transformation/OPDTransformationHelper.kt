package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd.OPDRequestValidated

fun ageAtFirstSanctionOffendersScore(request: OPDRequestValidated) = when (request.ageAtFirstSanction) {
  in 0..18 -> 1
  else -> 0
}

fun violenceOrThreatOfViolenceOffendersScore(request: OPDRequestValidated) = scoreFromBoolean(request.excessiveOrSadisticViolence)

fun excessiveOrSadisticViolenceOffendersScore(request: OPDRequestValidated) = when (request.excessiveOrSadisticViolence) {
  true -> if (request.violenceOrThreatOfViolence) {
    1
  } else {
    2
  }

  false -> 0
}

fun impactOfOffendingOnOthersOffendersScoreOpd(request: OPDRequestValidated) = scoreFromBoolean(request.impactOfOffendingOnOthers)

fun financialRelianceOnOthersOffendersScore(request: OPDRequestValidated) = scoreFromProblemLevel(request.financialRelianceOnOthers)

fun manipulativePredatoryBehaviourOffendersScore(request: OPDRequestValidated) = scoreFromProblemLevel(request.manipulativePredatoryBehaviour)

fun attitudesStableBehaviourOffendersScoreOpd(request: OPDRequestValidated) = scoreFromProblemLevel(request.attitudesStableBehaviour)

fun childhoodBehaviourOffendersScore(request: OPDRequestValidated) = scoreFromProblemLevel(request.childhoodBehaviour)

fun impulsivityBehaviourOffendersScore(request: OPDRequestValidated) = scoreFromProblemLevel(request.impulsivityBehaviour)

fun presenceOfChildhoodDifficultiesOffendersScore(request: OPDRequestValidated) = scoreFromProblemLevel(request.experienceOfChildhood)

fun controllingBehaviourOffendersScore(request: OPDRequestValidated) = scoreFromProblemLevel(request.controllingBehaviour)

fun historyOfMentalHealthDifficultiesOffendersScore(request: OPDRequestValidated): Int {
  val hasProblems = request.currentPsychologicalProblems in listOf(ProblemLevel.SOME_PROBLEMS, ProblemLevel.SIGNIFICANT_PROBLEMS) ||
    request.currentPsychiatricProblems in listOf(ProblemLevel.SOME_PROBLEMS, ProblemLevel.SIGNIFICANT_PROBLEMS) ||
    request.historyOfPsychiatricTreatment ||
    request.medicationMentalHealth ||
    request.patientSecureUnitOrHospital ||
    request.currentPsychiatricTreatmentOrPending == true ||
    request.obsessiveBehaviour

  return if (hasProblems) 1 else 0
}

fun selfHarmSuicideAttemptOffendersScore(input: OPDRequestValidated): Int {
  val fields = listOf(
    input.selfHarmSuicideAttempt,
    input.concernsAboutSuicidePast,
    input.concernsAboutSelfHarmPast,
  )

  return if (fields.any { it == true }) 1 else 0
}

fun severeChallengingBehavioursOffendersScore(input: OPDRequestValidated): Int {
  val hasAttitudeProblems =
    input.attitudeTowardsSupervision in listOf(ProblemLevel.SOME_PROBLEMS, ProblemLevel.SIGNIFICANT_PROBLEMS)

  val hasOtherRiskFlags = listOf(
    input.assaultedOrThreatenedStaff,
    input.escapeOrAbsconded,
    input.controlIssues,
    input.breachOfTrust,
  ).any { it == true }

  return if (hasAttitudeProblems || hasOtherRiskFlags) 1 else 0
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
