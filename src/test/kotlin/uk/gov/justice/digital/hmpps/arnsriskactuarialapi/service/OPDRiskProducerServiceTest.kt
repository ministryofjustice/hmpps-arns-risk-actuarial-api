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

@ExtendWith(MockitoExtension::class)
class OPDRiskProducerServiceTest {

  @Mock
  lateinit var offenceGroupParametersService: OffenceGroupParametersService

  @InjectMocks
  lateinit var service: OPDRiskProducerService

  @Test
  fun `should handle exceptions with error result`() {
    whenever(offenceGroupParametersService.isViolentOrSexualType("02504"))
      .thenThrow(IllegalArgumentException("Something"))

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffence = "02504",
      gender = Gender.MALE,
      overallRiskForAssessment = RiskBand.HIGH,
      custodialSentence = true,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(false, result.opdCheck)
    assertEquals(null, result.opdResult)

    assertTrue(result.validationError?.isNotEmpty() == true)
    assertEquals(
      ValidationErrorResponse(
        type = ValidationErrorType.NO_MATCHING_INPUT,
        message = "Error: Something",
        fields = emptyList(),
      ),
      result.validationError?.first(),
    )
  }

  @Test
  fun `should calculate OPD with an valid request, eligible male SCREEN_OUT`() {
    whenever(offenceGroupParametersService.isViolentOrSexualType("02504")).thenReturn(true)

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffence = "02504",
      gender = Gender.MALE,
      overallRiskForAssessment = RiskBand.HIGH,
      custodialSentence = true,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(true, result.opdCheck)
    assertEquals(OPDResult.SCREEN_OUT, result.opdResult)
    assertTrue(result.validationError?.isEmpty() == true)
  }

  @Test
  fun `should calculate OPD with an valid request, eligible male SCREEN_IN with override`() {
    whenever(offenceGroupParametersService.isViolentOrSexualType("02504")).thenReturn(true)

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffence = "02504",
      gender = Gender.MALE,
      overallRiskForAssessment = RiskBand.HIGH,
      custodialSentence = true,
      opdOverride = true,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(true, result.opdCheck)
    assertEquals(OPDResult.SCREEN_IN, result.opdResult)
  }

  @Test
  fun `should calculate OPD with an valid request, eligible male SCREEN_IN`() {
    whenever(offenceGroupParametersService.isViolentOrSexualType("02504")).thenReturn(true)

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffence = "02504",
      gender = Gender.MALE,
      overallRiskForAssessment = RiskBand.HIGH,
      custodialSentence = true,
      medicationMentalHealth = true,
      historyOfPsychiatricTreatment = true,
      currentPsychiatricTreatmentOrPending = true,
      selfHarmSuicideAttempt = true,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(true, result.opdCheck)
    assertEquals(OPDResult.SCREEN_IN, result.opdResult)
    assertTrue(result.validationError?.isEmpty() == true)
  }

  @Test
  fun `should not calculate OPD with an valid request, not-eligible  female`() {
    whenever(offenceGroupParametersService.isViolentOrSexualType("02504")).thenReturn(true)

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffence = "02504",
      gender = Gender.FEMALE,
      overallRiskForAssessment = RiskBand.LOW,
      eligibleForMappa = false,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(false, result.opdCheck)
  }

  @Test
  fun `should not calculate OPD with an valid request, not-eligible  male`() {
    whenever(offenceGroupParametersService.isViolentOrSexualType("02504")).thenReturn(true)

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffence = "02504",
      gender = Gender.MALE,
      overallRiskForAssessment = RiskBand.LOW,
      custodialSentence = false,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(false, result.opdCheck)
  }

  @Test
  fun `should not calculate OPD with an valid request, all null answers for male`() {
    whenever(offenceGroupParametersService.isViolentOrSexualType("02504")).thenReturn(true)

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffence = "02504",
      gender = Gender.MALE,
      custodialSentence = true,
      overallRiskForAssessment = RiskBand.VERY_HIGH,
      ageAtFirstSanction = null,
      violenceOrThreatOfViolence = null,
      excessiveOrSadisticViolence = null,
      impactOfOffendingOnOthers = null,
      financialRelianceOnOthers = null,
      manipulativePredatoryBehaviour = null,
      attitudesStableBehaviour = null,
      childhoodBehaviour = null,
      impulsivityBehaviour = null,
      controllingBehaviour = null,
      experienceOfChildhood = null,
      currentPsychologicalProblems = null,
      currentPsychiatricProblems = null,
      historyOfPsychiatricTreatment = null,
      medicationMentalHealth = null,
      patientSecureUnitOrHospital = null,
      currentPsychiatricTreatmentOrPending = null,
      obsessiveBehaviour = null,
      selfHarmSuicideAttempt = null,
      concernsAboutSuicidePast = null,
      concernsAboutSelfHarmPast = null,
      attitudeTowardsSupervision = null,
      assaultedOrThreatenedStaff = null,
      escapeOrAbsconded = null,
      controlIssues = null,
      breachOfTrust = null,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(OPDObject(false, null, emptyList()), result)
  }

  @Test
  fun `should not calculate OPD with an valid request, all null answers for female`() {
    whenever(offenceGroupParametersService.isViolentOrSexualType("02504")).thenReturn(true)

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffence = "02504",
      gender = Gender.FEMALE,
      overallRiskForAssessment = RiskBand.VERY_HIGH,
      eligibleForMappa = true,
      carryingOrUsingWeapon = null,
      violenceOrThreatOfViolence = null,
      excessiveOrSadisticViolence = null,
      offenceArson = null,
      offenderMotivations = null,
      offenceLinkedRiskOfSeriousHarm = null,
      accommodationLinkedRiskOfSeriousHarm = null,
      experienceOfChildhood = null,
      domesticAbuse = null,
      relationshipLinkedSeriousHarm = null,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(OPDObject(false, null, emptyList()), result)
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
        type = ValidationErrorType.MISSING_INPUT,
        message = "ERR5 - Field is Null",
        fields = listOf("Gender"),
      ),
      result.validationError?.first(),
    )
  }

  @Test
  fun `should calculate OPD with an valid request, eligible female SCREEN_OUT`() {
    whenever(offenceGroupParametersService.isViolentOrSexualType("02504")).thenReturn(true)

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffence = "02504",
      gender = Gender.FEMALE,
      carryingOrUsingWeapon = false,
      violenceOrThreatOfViolence = false,
      opdOverride = false,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(true, result.opdCheck)
    assertEquals(OPDResult.SCREEN_OUT, result.opdResult)
    assertTrue(result.validationError?.isEmpty() == true)
  }

  @Test
  fun `should calculate OPD with an valid request, eligible female SCREEN_IN with override`() {
    whenever(offenceGroupParametersService.isViolentOrSexualType("02504")).thenReturn(true)

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffence = "02504",
      gender = Gender.FEMALE,
      carryingOrUsingWeapon = false,
      violenceOrThreatOfViolence = false,
      opdOverride = true,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(true, result.opdCheck)
    assertEquals(OPDResult.SCREEN_IN, result.opdResult)
  }

  @Test
  fun `should calculate OPD with an valid request, eligible female SCREEN_IN`() {
    whenever(offenceGroupParametersService.isViolentOrSexualType("02504")).thenReturn(true)

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffence = "02504",
      gender = Gender.FEMALE,
      carryingOrUsingWeapon = true,
      violenceOrThreatOfViolence = true,
      offenceArson = true,
      experienceOfChildhood = ProblemLevel.SIGNIFICANT_PROBLEMS,
      accommodationLinkedRiskOfSeriousHarm = true,
      thinkingAndBehaviourLinedToRiskOfSeriousHarm = true,
      difficultiesCoping = ProblemLevel.SIGNIFICANT_PROBLEMS,
      selfHarmSuicideAttempt = true,
      wellbeingEmotionalLinkedRiskOfSeriousHarm = true,
      excessiveOrSadisticViolence = true,
      opdOverride = false,
    )

    val result = service.getRiskScore(request, context).OPD!!
    assertEquals(true, result.opdCheck)
    assertEquals(OPDResult.SCREEN_IN, result.opdResult)
    assertTrue(result.validationError?.isEmpty() == true)
  }
}
