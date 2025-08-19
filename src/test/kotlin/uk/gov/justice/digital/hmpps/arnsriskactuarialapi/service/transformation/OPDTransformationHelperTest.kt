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
  fun `didOffenceInvolveViolenceOrThreatOfViolenceOffendersScore returns correct score`() {
    val requestTrue = opdRequestValidated().copy(didOffenceInvolveViolenceOrThreatOfViolence = true)
    val requestFalse = opdRequestValidated().copy(didOffenceInvolveViolenceOrThreatOfViolence = false)

    assertEquals(1, didOffenceInvolveViolenceOrThreatOfViolenceOffendersScore(requestTrue))
    assertEquals(0, didOffenceInvolveViolenceOrThreatOfViolenceOffendersScore(requestFalse))
  }

  @Test
  fun `didOffenceInvolveExcessiveUseOfViolenceOffendersScore returns correct scores`() {
    val r1 = opdRequestValidated().copy(didOffenceInvolveExcessiveUseOfViolence = true, didOffenceInvolveViolenceOrThreatOfViolence = true)
    val r2 = opdRequestValidated().copy(didOffenceInvolveExcessiveUseOfViolence = true, didOffenceInvolveViolenceOrThreatOfViolence = false)
    val r3 = opdRequestValidated().copy(didOffenceInvolveExcessiveUseOfViolence = false)

    assertEquals(1, didOffenceInvolveExcessiveUseOfViolenceOffendersScore(r1))
    assertEquals(2, didOffenceInvolveExcessiveUseOfViolenceOffendersScore(r2))
    assertEquals(0, didOffenceInvolveExcessiveUseOfViolenceOffendersScore(r3))
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
        hasHistoryOfPsychiatricTreatment = false,
        hasBeenOnMedicationForMentalHealthProblems = false,
        hasEverBeenInSpecialHospitalOrRegionalSecureUnit = false,
        hasCurrentPsychiatricTreatment = false,
        hasDisplayedObsessiveBehaviourLinkedToOffending = false,
      )
    assertEquals(0, historyOfMentalHealthDifficultiesOffendersScore(request))
  }

  @Test
  fun `hasSelfHarmOrAttemptedSuicideOffendersScore returns 1 if hasSelfHarmOrAttemptedSuicide is true`() {
    val request = opdRequestValidated().copy(
      hasSelfHarmOrAttemptedSuicide = true,
    )
    assertEquals(1, hasSelfHarmOrAttemptedSuicideOffendersScore(request))
  }

  @Test
  fun `hasSelfHarmOrAttemptedSuicideOffendersScore returns 0 if hasSelfHarmOrAttemptedSuicide false`() {
    val request = opdRequestValidated().copy(
      hasSelfHarmOrAttemptedSuicide = false,
    )
    assertEquals(0, hasSelfHarmOrAttemptedSuicideOffendersScore(request))
  }

  @Test
  fun `severeChallengingBehavioursOffendersScore returns 1 for attitude problem`() {
    val request = opdRequestValidated().copy(attitudeTowardsSupervisionOrLicence = ProblemLevel.SIGNIFICANT_PROBLEMS)
    assertEquals(1, severeChallengingBehavioursOffendersScore(request))
  }

  @Test
  fun `severeChallengingBehavioursOffendersScore returns 1 for other risk flags`() {
    val request = opdRequestValidated().copy(hasControlIssues = true)
    assertEquals(1, severeChallengingBehavioursOffendersScore(request))
  }

  @Test
  fun `severeChallengingBehavioursOffendersScore returns 0 for no issues`() {
    val request = opdRequestValidated().copy(
      attitudeTowardsSupervisionOrLicence = ProblemLevel.NO_PROBLEMS,
      hasAssaultedOrThreatenedStaff = false,
      hasEscapedOrAbsconded = false,
      hasControlIssues = false,
    )
    assertEquals(0, severeChallengingBehavioursOffendersScore(request))
  }

  @Test
  fun `doesRecogniseImpactOfOffendingOnOthersOffendersScoreOpd returns correct score`() {
    val withProblem = opdRequestValidated().copy(doesRecogniseImpactOfOffendingOnOthers = true)
    val noProblem = opdRequestValidated().copy(doesRecogniseImpactOfOffendingOnOthers = false)

    assertEquals(0, doesRecogniseImpactOfOffendingOnOthersOffendersScoreOpd(withProblem))
    assertEquals(1, doesRecogniseImpactOfOffendingOnOthersOffendersScoreOpd(noProblem))
  }

  @Test
  fun `overRelianceOnOthersForFinancialSupportOffendersScore returns correct score`() {
    val some = opdRequestValidated().copy(overRelianceOnOthersForFinancialSupport = ProblemLevel.SOME_PROBLEMS)
    val none = opdRequestValidated().copy(overRelianceOnOthersForFinancialSupport = ProblemLevel.NO_PROBLEMS)
    val nullValue = opdRequestValidated().copy(overRelianceOnOthersForFinancialSupport = null)

    assertEquals(1, overRelianceOnOthersForFinancialSupportOffendersScore(some))
    assertEquals(0, overRelianceOnOthersForFinancialSupportOffendersScore(none))
    assertEquals(0, overRelianceOnOthersForFinancialSupportOffendersScore(nullValue))
  }

  @Test
  fun `manipulativeOrPredatoryBehaviourOffendersScore returns correct score`() {
    val sig = opdRequestValidated().copy(manipulativeOrPredatoryBehaviour = ProblemLevel.SIGNIFICANT_PROBLEMS)
    val none = opdRequestValidated().copy(manipulativeOrPredatoryBehaviour = ProblemLevel.NO_PROBLEMS)

    assertEquals(1, manipulativeOrPredatoryBehaviourOffendersScore(sig))
    assertEquals(0, manipulativeOrPredatoryBehaviourOffendersScore(none))
  }

  @Test
  fun `recklessnessAndRiskTakingBehaviourOffendersScoreOpd returns correct score`() {
    val some = opdRequestValidated().copy(recklessnessAndRiskTakingBehaviour = ProblemLevel.SOME_PROBLEMS)
    val none = opdRequestValidated().copy(recklessnessAndRiskTakingBehaviour = ProblemLevel.NO_PROBLEMS)

    assertEquals(1, recklessnessAndRiskTakingBehaviourOffendersScoreOpd(some))
    assertEquals(0, recklessnessAndRiskTakingBehaviourOffendersScoreOpd(none))
  }

  @Test
  fun `isEvidenceOfChildhoodBehaviouralProblemsOffendersScore returns correct score`() {
    val trueValue = opdRequestValidated().copy(isEvidenceOfChildhoodBehaviouralProblems = true)
    val falseValue = opdRequestValidated().copy(isEvidenceOfChildhoodBehaviouralProblems = false)

    assertEquals(1, isEvidenceOfChildhoodBehaviouralProblemsOffendersScore(trueValue))
    assertEquals(0, isEvidenceOfChildhoodBehaviouralProblemsOffendersScore(falseValue))
  }

  @Test
  fun `impulsivityProblemsOffendersScore returns correct score`() {
    val sig = opdRequestValidated().copy(impulsivityProblems = ProblemLevel.SIGNIFICANT_PROBLEMS)
    val none = opdRequestValidated().copy(impulsivityProblems = ProblemLevel.NO_PROBLEMS)

    assertEquals(1, impulsivityProblemsOffendersScore(sig))
    assertEquals(0, impulsivityProblemsOffendersScore(none))
  }

  @Test
  fun `presenceOfChildhoodDifficultiesOffendersScore returns correct score`() {
    val some = opdRequestValidated().copy(experienceOfChildhood = ProblemLevel.SOME_PROBLEMS)
    val none = opdRequestValidated().copy(experienceOfChildhood = ProblemLevel.NO_PROBLEMS)

    assertEquals(1, presenceOfChildhoodDifficultiesOffendersScore(some))
    assertEquals(0, presenceOfChildhoodDifficultiesOffendersScore(none))
  }

  @Test
  fun `controllingOrAggressiveBehaviourOffendersScore returns correct score`() {
    val sig = opdRequestValidated().copy(controllingOrAggressiveBehaviour = ProblemLevel.SIGNIFICANT_PROBLEMS)
    val nullValue = opdRequestValidated().copy(controllingOrAggressiveBehaviour = null)

    assertEquals(1, controllingOrAggressiveBehaviourOffendersScore(sig))
    assertEquals(0, controllingOrAggressiveBehaviourOffendersScore(nullValue))
  }

  @DisplayName("Test domesticAbuseOffendersScore using CsvSource")
  @ParameterizedTest(name = "evidenceOfDomesticAbuse={0}, partner={1}, family={2} => expectedScore={3}")
  @CsvSource(
    // evidenceOfDomesticAbuse, partner, family, expectedScore
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
    evidenceOfDomesticAbuse: Boolean,
    partner: Boolean?,
    family: Boolean?,
    expected: Int,
  ) {
    val request = opdRequestValidated().copy(
      evidenceOfDomesticAbuse = evidenceOfDomesticAbuse,
      domesticAbuseAgainstPartner = partner,
      domesticAbuseAgainstFamily = family,
    )
    assertEquals(expected, domesticAbuseOffendersScore(request))
  }
}
