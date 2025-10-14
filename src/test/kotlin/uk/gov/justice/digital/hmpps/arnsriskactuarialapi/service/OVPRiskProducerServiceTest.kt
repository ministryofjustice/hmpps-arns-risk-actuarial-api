package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.FIXED_TEST_DATE
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validOVPRiskScoreRequest
import java.time.LocalDate
import kotlin.test.assertFailsWith

class OVPRiskProducerServiceTest {

  private val service: OVPRiskProducerService = OVPRiskProducerService()

  @Test
  fun `should return valid OVPObject for valid input LOW risk`() {
    val result = service.getRiskScore(
      validOVPRiskScoreRequest()
        .copy(
          totalNumberOfSanctionsForAllOffences = 1 as Integer,
          totalNumberOfViolentSanctions = 0 as Integer,
          doesRecogniseImpactOfOffendingOnOthers = false,
        ),
      emptyContext(),
    )

    assertNotNull(result)
    assertTrue(result.OVP!!.validationError.isNullOrEmpty())
    assertEquals(5, result.OVP!!.provenViolentTypeReoffendingOneYear)
    assertEquals(9, result.OVP!!.provenViolentTypeReoffendingTwoYear)
    assertEquals(RiskBand.LOW, result.OVP!!.band)
    assertTrue(result.OVP!!.validationError.isNullOrEmpty())
  }

  @Test
  fun `should return valid OVPObject for valid input MEDIUM risk`() {
    val result = service.getRiskScore(validOVPRiskScoreRequest(), emptyContext())

    assertNotNull(result)
    assertTrue(result.OVP!!.validationError.isNullOrEmpty())
    assertEquals(15, result.OVP!!.provenViolentTypeReoffendingOneYear)
    assertEquals(26, result.OVP!!.provenViolentTypeReoffendingTwoYear)
    assertEquals(RiskBand.LOW, result.OVP!!.band)
    assertTrue(result.OVP!!.validationError.isNullOrEmpty())
  }

  @Test
  fun `should return valid OVPObject for valid input HIGH risk`() {
    val result = service.getRiskScore(
      validOVPRiskScoreRequest()
        .copy(
          totalNumberOfSanctionsForAllOffences = 55 as Integer,
          totalNumberOfViolentSanctions = 40 as Integer,
          doesRecogniseImpactOfOffendingOnOthers = true,
          temperControl = ProblemLevel.SIGNIFICANT_PROBLEMS,
          currentAlcoholUseProblems = ProblemLevel.SOME_PROBLEMS,
          excessiveAlcoholUse = ProblemLevel.SOME_PROBLEMS,
        ),
      emptyContext(),
    )

    assertNotNull(result)
    assertTrue(result.OVP!!.validationError.isNullOrEmpty())
    assertEquals(42, result.OVP!!.provenViolentTypeReoffendingOneYear)
    assertEquals(58, result.OVP!!.provenViolentTypeReoffendingTwoYear)
    assertEquals(RiskBand.MEDIUM, result.OVP!!.band)
    assertTrue(result.OVP!!.validationError.isNullOrEmpty())
  }

  @Test
  fun `should return null OVPObject with error message for exceptions thrown before calculation`() {
    // Given
    val request = RiskScoreRequest(
      RiskScoreVersion.V1_0,
      null,
      FIXED_TEST_DATE,
      null,
      null,
      null,
      null,
      null,
      null,
    )
    // When
    val result = service.getRiskScore(request, emptyContext())

    val expectedFields = listOf(
      "gender",
      "dateOfBirth",
      "dateAtStartOfFollowupCalculated",
      "totalNumberOfSanctionsForAllOffences",
      "totalNumberOfViolentSanctions",
      "doesRecogniseImpactOfOffendingOnOthers",
      "isCurrentlyOfNoFixedAbodeOrTransientAccommodation",
      "isUnemployed",
      "currentAlcoholUseProblems",
      "excessiveAlcoholUse",
      "hasCurrentPsychiatricTreatment",
      "temperControl",
      "proCriminalAttitudes",
    )

    // Then
    assertNotNull(result)
    assertNull(result.OVP?.provenViolentTypeReoffendingOneYear)
    assertNull(result.OVP?.provenViolentTypeReoffendingTwoYear)
    assertNull(result.OVP?.band)
    assertEquals(1, result.OVP?.validationError?.size)
    val error = result.OVP?.validationError?.first()
    assertEquals(ValidationErrorType.MISSING_MANDATORY_INPUT, error?.type)
    assertEquals("Mandatory input field(s) missing", error?.message)
    assertEquals(expectedFields, error?.fields)
  }

  @Test
  fun `should throw exceptions during calculation`() {
    // When
    val exception = assertFailsWith<IllegalArgumentException>(
      block = {
        service.getRiskScore(
          validOVPRiskScoreRequest().copy(
            dateOfBirth = LocalDate.of(2002, 12, 13),
            dateAtStartOfFollowupCalculated = LocalDate.of(2000, 12, 13),
          ),
          emptyContext(),
        )
      },
    )

    // Then
    assertEquals("dateAtStartOfFollowup cannot be before date of birth.", exception.message)
  }
}
