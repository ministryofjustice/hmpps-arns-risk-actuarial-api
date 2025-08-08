package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.opdRequestValidated

class OPDTransformationHelperTest {

  @Test
  fun `ageAtFirstSanctionOffendersScore returns 1 for age less than 18`() {
    val request = opdRequestValidated().copy(ageAtFirstSanction = 16)
    assertEquals(1, ageAtFirstSanctionOffendersScore(request))
  }

  @Test
  fun `ageAtFirstSanctionOffendersScore returns 0 for age over 18`() {
    val request = opdRequestValidated().copy(ageAtFirstSanction = 25)
    assertEquals(0, ageAtFirstSanctionOffendersScore(request))
  }

  @Test
  fun `violenceOrThreatOfViolenceOffendersScore returns correct score`() {
    val requestTrue = opdRequestValidated().copy(excessiveOrSadisticViolence = true)
    val requestFalse = opdRequestValidated().copy(excessiveOrSadisticViolence = false)

    assertEquals(1, violenceOrThreatOfViolenceOffendersScore(requestTrue))
    assertEquals(0, violenceOrThreatOfViolenceOffendersScore(requestFalse))
  }

  @Test
  fun `excessiveOrSadisticViolenceOffendersScore returns correct scores`() {
    val r1 = opdRequestValidated().copy(excessiveOrSadisticViolence = true, violenceOrThreatOfViolence = true)
    val r2 = opdRequestValidated().copy(excessiveOrSadisticViolence = true, violenceOrThreatOfViolence = false)
    val r3 = opdRequestValidated().copy(excessiveOrSadisticViolence = false)

    assertEquals(1, excessiveOrSadisticViolenceOffendersScore(r1))
    assertEquals(2, excessiveOrSadisticViolenceOffendersScore(r2))
    assertEquals(0, excessiveOrSadisticViolenceOffendersScore(r3))
  }

  @Test
  fun `scoreFromProblemLevel helper returns correct values`() {
    assertEquals(0, scoreFromProblemLevel(ProblemLevel.NO_PROBLEMS))
    assertEquals(1, scoreFromProblemLevel(ProblemLevel.SOME_PROBLEMS))
    assertEquals(1, scoreFromProblemLevel(ProblemLevel.SIGNIFICANT_PROBLEMS))
    assertEquals(0, scoreFromProblemLevel(null))
  }

  @Test
  fun `scoreFromBoolean helper returns correct values`() {
    assertEquals(1, scoreFromBoolean(true))
    assertEquals(0, scoreFromBoolean(false))
  }

  @Test
  fun `allUnansweredQuestion returns true if all null`() {
    val fields = listOf(null, null, null)
    assertTrue(allUnansweredQuestion(fields))
  }

  @Test
  fun `allUnansweredQuestion returns false if any non-null`() {
    val fields = listOf(null, "value", null)
    assertFalse(allUnansweredQuestion(fields))
  }

  @Test
  fun `historyOfMentalHealthDifficultiesOffendersScore returns 1 if any problem present`() {
    val request = opdRequestValidated()
      .copy(currentPsychiatricProblems = ProblemLevel.SOME_PROBLEMS)
    assertEquals(1, historyOfMentalHealthDifficultiesOffendersScore(request))
  }

  @Test
  fun `historyOfMentalHealthDifficultiesOffendersScore returns 0 if no problem`() {
    val request = opdRequestValidated()
      .copy(
        currentPsychologicalProblems = ProblemLevel.NO_PROBLEMS,
        currentPsychiatricProblems = ProblemLevel.NO_PROBLEMS,
        historyOfPsychiatricTreatment = false,
        medicationMentalHealth = false,
        patientSecureUnitOrHospital = false,
        currentPsychiatricTreatmentOrPending = false,
        obsessiveBehaviour = false,
      )
    assertEquals(0, historyOfMentalHealthDifficultiesOffendersScore(request))
  }

  @Test
  fun `selfHarmSuicideAttemptOffendersScore returns 1 if any true`() {
    val request = opdRequestValidated().copy(
      selfHarmSuicideAttempt = true,
      concernsAboutSuicidePast = false,
      concernsAboutSelfHarmPast = false,
    )
    assertEquals(1, selfHarmSuicideAttemptOffendersScore(request))
  }

  @Test
  fun `selfHarmSuicideAttemptOffendersScore returns 0 if all false`() {
    val request = opdRequestValidated().copy(
      selfHarmSuicideAttempt = false,
      concernsAboutSuicidePast = false,
      concernsAboutSelfHarmPast = false,
    )
    assertEquals(0, selfHarmSuicideAttemptOffendersScore(request))
  }

  @Test
  fun `severeChallengingBehavioursOffendersScore returns 1 for attitude problem`() {
    val request = opdRequestValidated().copy(attitudeTowardsSupervision = ProblemLevel.SIGNIFICANT_PROBLEMS)
    assertEquals(1, severeChallengingBehavioursOffendersScore(request))
  }

  @Test
  fun `severeChallengingBehavioursOffendersScore returns 1 for other risk flags`() {
    val request = opdRequestValidated().copy(controlIssuesOrBreachOfTrust = true)
    assertEquals(1, severeChallengingBehavioursOffendersScore(request))
  }

  @Test
  fun `severeChallengingBehavioursOffendersScore returns 0 for no issues`() {
    val request = opdRequestValidated().copy(
      attitudeTowardsSupervision = ProblemLevel.NO_PROBLEMS,
      assaultedOrThreatenedStaff = false,
      escapeOrAbsconded = false,
      controlIssuesOrBreachOfTrust = false,
    )
    assertEquals(0, severeChallengingBehavioursOffendersScore(request))
  }

  @Test
  fun `impactOfOffendingOnOthersOffendersScoreOpd returns correct score`() {
    val withProblem = opdRequestValidated().copy(impactOfOffendingOnOthers = true)
    val noProblem = opdRequestValidated().copy(impactOfOffendingOnOthers = false)

    assertEquals(1, impactOfOffendingOnOthersOffendersScoreOpd(withProblem))
    assertEquals(0, impactOfOffendingOnOthersOffendersScoreOpd(noProblem))
  }

  @Test
  fun `financialRelianceOnOthersOffendersScore returns correct score`() {
    val some = opdRequestValidated().copy(financialRelianceOnOthers = ProblemLevel.SOME_PROBLEMS)
    val none = opdRequestValidated().copy(financialRelianceOnOthers = ProblemLevel.NO_PROBLEMS)
    val nullValue = opdRequestValidated().copy(financialRelianceOnOthers = null)

    assertEquals(1, financialRelianceOnOthersOffendersScore(some))
    assertEquals(0, financialRelianceOnOthersOffendersScore(none))
    assertEquals(0, financialRelianceOnOthersOffendersScore(nullValue))
  }

  @Test
  fun `manipulativePredatoryBehaviourOffendersScore returns correct score`() {
    val sig = opdRequestValidated().copy(manipulativePredatoryBehaviour = ProblemLevel.SIGNIFICANT_PROBLEMS)
    val none = opdRequestValidated().copy(manipulativePredatoryBehaviour = ProblemLevel.NO_PROBLEMS)

    assertEquals(1, manipulativePredatoryBehaviourOffendersScore(sig))
    assertEquals(0, manipulativePredatoryBehaviourOffendersScore(none))
  }

  @Test
  fun `attitudesStableBehaviourOffendersScoreOpd returns correct score`() {
    val some = opdRequestValidated().copy(attitudesStableBehaviour = ProblemLevel.SOME_PROBLEMS)
    val none = opdRequestValidated().copy(attitudesStableBehaviour = ProblemLevel.NO_PROBLEMS)

    assertEquals(1, attitudesStableBehaviourOffendersScoreOpd(some))
    assertEquals(0, attitudesStableBehaviourOffendersScoreOpd(none))
  }

  @Test
  fun `childhoodBehaviourOffendersScore returns correct score`() {
    val some = opdRequestValidated().copy(childhoodBehaviour = ProblemLevel.SOME_PROBLEMS)
    val nullValue = opdRequestValidated().copy(childhoodBehaviour = null)

    assertEquals(1, childhoodBehaviourOffendersScore(some))
    assertEquals(0, childhoodBehaviourOffendersScore(nullValue))
  }

  @Test
  fun `impulsivityBehaviourOffendersScore returns correct score`() {
    val sig = opdRequestValidated().copy(impulsivityBehaviour = ProblemLevel.SIGNIFICANT_PROBLEMS)
    val none = opdRequestValidated().copy(impulsivityBehaviour = ProblemLevel.NO_PROBLEMS)

    assertEquals(1, impulsivityBehaviourOffendersScore(sig))
    assertEquals(0, impulsivityBehaviourOffendersScore(none))
  }

  @Test
  fun `presenceOfChildhoodDifficultiesOffendersScore returns correct score`() {
    val some = opdRequestValidated().copy(experienceOfChildhood = ProblemLevel.SOME_PROBLEMS)
    val none = opdRequestValidated().copy(experienceOfChildhood = ProblemLevel.NO_PROBLEMS)

    assertEquals(1, presenceOfChildhoodDifficultiesOffendersScore(some))
    assertEquals(0, presenceOfChildhoodDifficultiesOffendersScore(none))
  }

  @Test
  fun `controllingBehaviourOffendersScore returns correct score`() {
    val sig = opdRequestValidated().copy(controllingBehaviour = ProblemLevel.SIGNIFICANT_PROBLEMS)
    val nullValue = opdRequestValidated().copy(controllingBehaviour = null)

    assertEquals(1, controllingBehaviourOffendersScore(sig))
    assertEquals(0, controllingBehaviourOffendersScore(nullValue))
  }

  @DisplayName("Test domesticAbuseOffendersScore using CsvSource")
  @ParameterizedTest(name = "domesticAbuse={0}, partner={1}, family={2} => expectedScore={3}")
  @CsvSource(
    // domesticAbuse, partner, family, expectedScore
    "false, , , 0",
    "false, true, true, 0", // this should never happen but testing anyway
    "true, , , 0",
    "true, false, false, 0",
    "true, true, false, 1",
    "true, false, true, 1",
    "true, true, true, 1",
    "true, , true, 1",
    "true, true, , 1",
  )
  fun testDomesticAbuseOffendersScore(
    domesticAbuse: Boolean,
    partner: Boolean?,
    family: Boolean?,
    expected: Int,
  ) {
    val request = opdRequestValidated().copy(
      domesticAbuse = domesticAbuse,
      domesticAbusePartner = partner,
      domesticAbuseFamily = family,
    )
    assertEquals(expected, domesticAbuseOffendersScore(request))
  }
}
