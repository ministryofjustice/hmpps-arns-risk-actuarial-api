package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validOVPRiskScoreRequest
import java.time.LocalDate

class OVPRiskProducerServiceTest {

  private val service: OVPRiskProducerService = OVPRiskProducerService()

  @Test
  fun `should return valid OVPObject for valid input LOW risk`() {
    val result = service.getRiskScore(
      validOVPRiskScoreRequest()
        .copy(
          totalNumberOfSanctions = 1 as Integer,
          totalNumberOfViolentSanctions = 0 as Integer,
          impactOfOffendingOnOthers = false,
        ),
      emptyContext(),
    )

    assertNotNull(result)
    assertTrue(result.OVP!!.validationError.isNullOrEmpty())
    assertEquals("1_0", result.OVP.algorithmVersion)
    assertEquals(5, result.OVP.provenViolentTypeReoffendingOneYear)
    assertEquals(9, result.OVP.provenViolentTypeReoffendingTwoYear)
    assertEquals(RiskBand.LOW, result.OVP.band)
    assertTrue(result.OVP.validationError.isNullOrEmpty())
  }

  @Test
  fun `should return valid OVPObject for valid input MEDIUM risk`() {
    val result = service.getRiskScore(validOVPRiskScoreRequest(), emptyContext())

    assertNotNull(result)
    assertTrue(result.OVP!!.validationError.isNullOrEmpty())
    assertEquals("1_0", result.OVP.algorithmVersion)
    assertEquals(15, result.OVP.provenViolentTypeReoffendingOneYear)
    assertEquals(26, result.OVP.provenViolentTypeReoffendingTwoYear)
    assertEquals(RiskBand.LOW, result.OVP.band)
    assertTrue(result.OVP.validationError.isNullOrEmpty())
  }

  @Test
  fun `should return valid OVPObject for valid input HIGH risk`() {
    val result = service.getRiskScore(
      validOVPRiskScoreRequest()
        .copy(
          totalNumberOfSanctions = 55 as Integer,
          totalNumberOfViolentSanctions = 40 as Integer,
          impactOfOffendingOnOthers = true,
          temperControl = ProblemLevel.SIGNIFICANT_PROBLEMS,
          alcoholIsCurrentUseAProblem = ProblemLevel.SOME_PROBLEMS,
          alcoholExcessive6Months = ProblemLevel.SOME_PROBLEMS,
        ),
      emptyContext(),
    )

    assertNotNull(result)
    assertTrue(result.OVP!!.validationError.isNullOrEmpty())
    assertEquals("1_0", result.OVP.algorithmVersion)
    assertEquals(42, result.OVP.provenViolentTypeReoffendingOneYear)
    assertEquals(58, result.OVP.provenViolentTypeReoffendingTwoYear)
    assertEquals(RiskBand.MEDIUM, result.OVP.band)
    assertTrue(result.OVP.validationError.isNullOrEmpty())
  }

  @Test
  fun `should return null OVPObject with error message for exceptions thrown before calculation`() {
    // Given
    val request = RiskScoreRequest(
      "1_0",
      null,
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
      "Gender",
      "Date of birth",
      "Date at start of followup",
      "Total number of sanctions",
      "Total number of violent sanctions",
      "Impact of offending on others",
      "Current accommodation",
      "Employment status",
      "Alcohol is current use a problem",
      "Alcohol excessive 6 months",
      "Current psychiatric treatment or pending",
      "Temper control",
      "Pro criminal attitudes",
    )

    // Then
    assertNotNull(result)
    assertEquals("1_0", result.OVP!!.algorithmVersion)
    assertNull(result.OVP.provenViolentTypeReoffendingOneYear)
    assertNull(result.OVP.provenViolentTypeReoffendingTwoYear)
    assertNull(result.OVP.band)
    assertEquals(1, result.OVP.validationError?.size)
    val error = result.OVP.validationError?.first()
    assertEquals(ValidationErrorType.MISSING_INPUT, error?.type)
    assertEquals("ERR5 - Field is Null", error?.message)
    assertEquals(expectedFields, error?.fields)
  }

  @Test
  fun `should return null OVPObject with error message for exceptions thrown during calculation`() {
    // When
    val result = service.getRiskScore(
      validOVPRiskScoreRequest().copy(
        dateOfBirth = LocalDate.of(2002, 12, 13),
        dateAtStartOfFollowup = LocalDate.of(2000, 12, 13),
      ),
      emptyContext(),
    )

    // Then
    assertNotNull(result)
    assertEquals("1_0", result.OVP!!.algorithmVersion)
    assertNull(result.OVP.provenViolentTypeReoffendingTwoYear)
    assertNull(result.OVP.provenViolentTypeReoffendingOneYear)
    assertNull(result.OVP.band)
    assertEquals(1, result.OVP.validationError?.size)
    val error = result.OVP.validationError?.first()
    assertEquals(ValidationErrorType.NO_MATCHING_INPUT, error?.type)
    assertEquals("Error: Invalid ageAtStartOfFollowup value: -2", error?.message)
  }
}
