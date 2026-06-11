package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd.OPDObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd.OPDResult
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyOPD
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validOPDRiskScoreRequest
import kotlin.test.assertFailsWith

@ExtendWith(MockitoExtension::class)
class OPDRiskProducerServiceTest {

  @Mock
  lateinit var offenceCodeCacheService: OffenceCodeCacheService

  @InjectMocks
  lateinit var service: OPDRiskProducerService

  @Test
  fun `should throw exceptions`() {
    whenever(offenceCodeCacheService.isViolentOrSexualType("02504"))
      .thenThrow(IllegalArgumentException("Something"))

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffenceCode = "02504",
      gender = Gender.MALE,
      overallRiskForAssessment = RiskBand.HIGH,
      hasCustodialSentence = true,
    )

    val exception = assertFailsWith<IllegalArgumentException>(
      block = { service.getRiskScore(request, context).OPD!! },
    )

    assertEquals("Something", exception.message)
  }

  @Test
  fun `should calculate OPD with an valid request, eligible male SCREEN_OUT`() {
    whenever(offenceCodeCacheService.isViolentOrSexualType("02504")).thenReturn(true)

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffenceCode = "02504",
      gender = Gender.MALE,
      overallRiskForAssessment = RiskBand.HIGH,
      hasCustodialSentence = true,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(true, result.opdCheck)
    assertEquals(OPDResult.SCREEN_OUT, result.opdResult)
    assertTrue(result.validationError?.isEmpty() == true)
  }

  @Test
  fun `should calculate OPD with an valid request, eligible male SCREEN_OUT with override TRUE`() {
    whenever(offenceCodeCacheService.isViolentOrSexualType("02504")).thenReturn(true)

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffenceCode = "02504",
      gender = Gender.MALE,
      overallRiskForAssessment = RiskBand.HIGH,
      hasCustodialSentence = true,
      applyOPDOverride = true,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(true, result.opdCheck)
    assertEquals(OPDResult.SCREEN_OUT, result.opdResult)
    assertEquals(true, result.opdOverride)
  }

  @Test
  fun `should calculate OPD with an valid request, eligible male SCREEN_IN`() {
    whenever(offenceCodeCacheService.isViolentOrSexualType("02504")).thenReturn(true)

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffenceCode = "02504",
      gender = Gender.MALE,
      overallRiskForAssessment = RiskBand.HIGH,
      hasCustodialSentence = true,
      hasBeenOnMedicationForMentalHealthProblems = true,
      hasHistoryOfPsychiatricTreatment = true,
      hasCurrentPsychiatricTreatment = true,
      hasSelfHarmOrAttemptedSuicide = true,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(true, result.opdCheck)
    assertEquals(OPDResult.SCREEN_IN, result.opdResult)
    assertTrue(result.validationError?.isEmpty() == true)
  }

  @Test
  fun `should skip OPD calculation with an valid request, SCREEN_OUT because because of low risk for female`() {
    whenever(offenceCodeCacheService.isViolentOrSexualType("02504")).thenReturn(true)

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffenceCode = "02504",
      gender = Gender.FEMALE,
      overallRiskForAssessment = RiskBand.LOW,
      isEligibleForMappa = false,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(true, result.opdCheck)
    assertEquals(OPDResult.SCREEN_OUT, result.opdResult)
  }

  @Test
  fun `should skip OPD calculation with an valid request, SCREEN_OUT because of not custodial for female`() {
    whenever(offenceCodeCacheService.isViolentOrSexualType("02504")).thenReturn(true)

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffenceCode = "02504",
      gender = Gender.FEMALE,
      overallRiskForAssessment = RiskBand.HIGH,
      isEligibleForMappa = false,
      hasCustodialSentence = false,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(true, result.opdCheck)
    assertEquals(OPDResult.SCREEN_OUT, result.opdResult)
  }

  @Test
  fun `should skip OPD calculation with an valid request, SCREEN_OUT because of not violent for female`() {
    whenever(offenceCodeCacheService.isViolentOrSexualType("02504")).thenReturn(false)

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffenceCode = "02504",
      gender = Gender.FEMALE,
      overallRiskForAssessment = RiskBand.HIGH,
      isEligibleForMappa = false,
      hasCustodialSentence = true,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(true, result.opdCheck)
    assertEquals(OPDResult.SCREEN_OUT, result.opdResult)
  }

  @Test
  fun `should skip OPD calculation with an valid request, SCREEN_OUT because low risk for  male`() {
    whenever(offenceCodeCacheService.isViolentOrSexualType("02504")).thenReturn(true)

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffenceCode = "02504",
      gender = Gender.MALE,
      overallRiskForAssessment = RiskBand.LOW,
      hasCustodialSentence = true,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(true, result.opdCheck)
    assertEquals(OPDResult.SCREEN_OUT, result.opdResult)
  }

  @Test
  fun `should skip OPD calculation with an valid request, SCREEN_OUT because not custodial sentence for male`() {
    whenever(offenceCodeCacheService.isViolentOrSexualType("02504")).thenReturn(true)

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffenceCode = "02504",
      gender = Gender.MALE,
      overallRiskForAssessment = RiskBand.HIGH,
      hasCustodialSentence = false,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(true, result.opdCheck)
    assertEquals(OPDResult.SCREEN_OUT, result.opdResult)
  }

  @Test
  fun `should skip OPD calculation with an valid request, SCREEN_OUT because not violent offence for male`() {
    whenever(offenceCodeCacheService.isViolentOrSexualType("02504")).thenReturn(false)

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffenceCode = "02504",
      gender = Gender.MALE,
      overallRiskForAssessment = RiskBand.HIGH,
      hasCustodialSentence = true,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(true, result.opdCheck)
    assertEquals(OPDResult.SCREEN_OUT, result.opdResult)
  }

  @Test
  fun `should not calculate OPD with an valid request, all null answers for male`() {
    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffenceCode = "02504",
      gender = Gender.MALE,
      hasCustodialSentence = true,
      overallRiskForAssessment = RiskBand.VERY_HIGH,
      ageAtFirstSanction = null,
      didOffenceInvolveViolenceOrThreatOfViolence = null,
      didOffenceInvolveExcessiveUseOfViolence = null,
      doesRecogniseImpactOfOffendingOnOthers = null,
      overRelianceOnOthersForFinancialSupport = null,
      manipulativeOrPredatoryBehaviour = null,
      recklessnessAndRiskTakingBehaviour = null,
      isEvidenceOfChildhoodBehaviouralProblems = null,
      impulsivityProblems = null,
      controllingOrAggressiveBehaviour = null,
      experienceOfChildhood = null,
      currentPsychologicalProblems = null,
      currentPsychiatricProblems = null,
      hasHistoryOfPsychiatricTreatment = null,
      hasBeenOnMedicationForMentalHealthProblems = null,
      hasEverBeenInSpecialHospitalOrRegionalSecureUnit = null,
      hasCurrentPsychiatricTreatment = null,
      hasDisplayedObsessiveBehaviourLinkedToOffending = null,
      hasSelfHarmOrAttemptedSuicide = null,
      attitudeTowardsSupervisionOrLicence = null,
      hasAssaultedOrThreatenedStaff = null,
      hasEscapedOrAbsconded = null,
      hasControlIssues = null,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(OPDObject(false, null, null, emptyList()), result)
  }

  @Test
  fun `should not calculate OPD with an valid request, all null answers for female`() {
    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffenceCode = "02504",
      gender = Gender.FEMALE,
      overallRiskForAssessment = RiskBand.VERY_HIGH,
      isEligibleForMappa = true,
      didOffenceInvolveCarryingOrUsingWeapon = null,
      didOffenceInvolveViolenceOrThreatOfViolence = null,
      didOffenceInvolveExcessiveUseOfViolence = null,
      didOffenceInvolveArson = null,
      offenceMotivationEmotionalState = null,
      isAnalysisOfOffenceIssuesLinkedToRisk = null,
      hasAccommodationIssuesLinkedToRisk = null,
      experienceOfChildhood = null,
      evidenceOfDomesticAbuse = null,
      relationshipIssuesLinkedToRisk = null,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(OPDObject(false, null, null, emptyList()), result)
  }

  @Test
  fun `should calculate empty OPD with an invalid request`() {
    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      gender = null,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(false, result.opdCheck)
    assertTrue(result.validationError?.isNotEmpty() == true)
    assertEquals(
      ValidationErrorResponse(
        type = ValidationErrorType.MISSING_MANDATORY_INPUT,
        message = "Mandatory input field(s) missing",
        fields = listOf("gender"),
      ),
      result.validationError?.first(),
    )
  }

  @Test
  fun `should calculate OPD with an valid request, eligible female SCREEN_OUT`() {
    whenever(offenceCodeCacheService.isViolentOrSexualType("02504")).thenReturn(true)

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffenceCode = "02504",
      gender = Gender.FEMALE,
      didOffenceInvolveCarryingOrUsingWeapon = false,
      didOffenceInvolveViolenceOrThreatOfViolence = false,
      applyOPDOverride = false,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(true, result.opdCheck)
    assertEquals(OPDResult.SCREEN_OUT, result.opdResult)
    assertTrue(result.validationError?.isEmpty() == true)
  }

  @Test
  fun `should calculate OPD with an valid request, eligible female SCREEN_OUT with override TRUE`() {
    whenever(offenceCodeCacheService.isViolentOrSexualType("02504")).thenReturn(true)

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffenceCode = "02504",
      gender = Gender.FEMALE,
      didOffenceInvolveCarryingOrUsingWeapon = false,
      didOffenceInvolveViolenceOrThreatOfViolence = false,
      applyOPDOverride = true,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(true, result.opdCheck)
    assertEquals(OPDResult.SCREEN_OUT, result.opdResult)
    assertEquals(true, result.opdOverride)
  }

  @Test
  fun `should calculate OPD with an valid request, eligible female SCREEN_OUT with override FALSE`() {
    whenever(offenceCodeCacheService.isViolentOrSexualType("02504")).thenReturn(true)

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffenceCode = "02504",
      gender = Gender.FEMALE,
      didOffenceInvolveCarryingOrUsingWeapon = false,
      didOffenceInvolveViolenceOrThreatOfViolence = false,
      applyOPDOverride = false,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(true, result.opdCheck)
    assertEquals(OPDResult.SCREEN_OUT, result.opdResult)
    assertEquals(false, result.opdOverride)
  }

  @Test
  fun `should calculate OPD with an valid request, eligible female SCREEN_IN`() {
    whenever(offenceCodeCacheService.isViolentOrSexualType("02504")).thenReturn(true)

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffenceCode = "02504",
      gender = Gender.FEMALE,
      didOffenceInvolveCarryingOrUsingWeapon = true,
      didOffenceInvolveViolenceOrThreatOfViolence = true,
      didOffenceInvolveArson = true,
      experienceOfChildhood = ProblemLevel.SIGNIFICANT_PROBLEMS,
      hasAccommodationIssuesLinkedToRisk = true,
      areThinkingAndBehaviourIssuesLinkedToRisk = true,
      difficultiesCoping = ProblemLevel.SIGNIFICANT_PROBLEMS,
      hasSelfHarmOrAttemptedSuicide = true,
      areEmotionalIssuesLinkedToRisk = true,
      didOffenceInvolveExcessiveUseOfViolence = true,
      applyOPDOverride = false,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(true, result.opdCheck)
    assertEquals(OPDResult.SCREEN_IN, result.opdResult)
    assertTrue(result.validationError?.isEmpty() == true)
  }
}
