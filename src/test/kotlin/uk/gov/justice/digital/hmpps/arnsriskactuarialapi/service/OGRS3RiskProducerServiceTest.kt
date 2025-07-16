package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class OGRS3RiskProducerServiceTest {

  @Mock
  lateinit var offenceGroupParametersService: OffenceGroupParametersService

  @InjectMocks
  lateinit var ogrs3RiskProducerService: OGRS3RiskProducerService

  @Test
  fun `should return valid OGRS3Object for valid input`() {
    // Given
    whenever(offenceGroupParametersService.getOGRS3Weighting("05110")).thenReturn(2.0)

    // When
    val result = ogrs3RiskProducerService.getRiskScore(validRiskScoreRequest())

    // Then
    assertNotNull(result)
    assertEquals("1_0", result.algorithmVersion)
    assertEquals(64, result.ogrs3OneYear)
    assertEquals(79, result.ogrs3TwoYear)
    assertEquals(RiskBand.HIGH, result.band)
    assertTrue(result.validationError.isNullOrEmpty())
  }

  @Test
  fun `should return null OGRS3Object with error message for exceptions thrown before calculation`() {
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
    val result = ogrs3RiskProducerService.getRiskScore(request)

    val expectedFields = listOf(
      "Gender",
      "Date of birth",
      "Date of current conviction",
      "Date at start of followup",
      "Total number of sanctions",
      "Age at first sanction",
      "Current offence",
    )

    // Then
    assertNotNull(result)
    assertEquals("1_0", result.algorithmVersion)
    assertNull(result.ogrs3OneYear)
    assertNull(result.ogrs3TwoYear)
    assertNull(result.band)
    assertEquals(1, result.validationError?.size)
    val error = result.validationError?.first()
    assertEquals(ValidationErrorType.MISSING_INPUT, error?.type)
    assertEquals("ERR5 - Field is Null", error?.message)
    assertEquals(expectedFields, error?.fields)
  }

  @Test
  fun `should return null OGRS3Object with error message for exceptions thrown during calculation`() {
    // When
    val result = ogrs3RiskProducerService.getRiskScore(
      validRiskScoreRequest().copy(
        dateOfBirth = LocalDate.of(2002, 12, 13),
        dateOfCurrentConviction = LocalDate.of(2000, 12, 13),
      ),
    )

    // Then
    assertNotNull(result)
    assertEquals("1_0", result.algorithmVersion)
    assertNull(result.ogrs3OneYear)
    assertNull(result.ogrs3TwoYear)
    assertNull(result.band)
    assertEquals(1, result.validationError?.size)
    val error = result.validationError?.first()
    assertEquals(ValidationErrorType.NO_MATCHING_INPUT, error?.type)
    assertEquals("Error: Conviction date cannot be before date of birth.", error?.message)
  }

  private fun validRiskScoreRequest(): RiskScoreRequest = RiskScoreRequest(
    "1_0",
    Gender.MALE,
    LocalDate.of(1964, 10, 15),
    LocalDate.of(2014, 12, 13),
    LocalDate.of(2027, 12, 12),
    10 as Integer?,
    30 as Integer?,
    "05110",
  )
}
