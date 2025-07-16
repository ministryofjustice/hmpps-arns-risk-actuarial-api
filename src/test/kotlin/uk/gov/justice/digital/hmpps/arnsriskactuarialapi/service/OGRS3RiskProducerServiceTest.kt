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
    whenever(offenceGroupParametersService.getOGRS3Weighting("051101")).thenReturn(2.0)

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
  fun `should return null OGRS3Object with error message for exceptions thrown during calculation`() {
    // Given
    whenever(offenceGroupParametersService.getOGRS3Weighting("123")).thenThrow(
      NoSuchElementException("123 not found"),
    )

    // When
    val result = ogrs3RiskProducerService.getRiskScore(validRiskScoreRequest().copy(currentOffence = "123"))

    // Then
    assertNotNull(result)
    assertEquals("1_0", result.algorithmVersion)
    assertNull(result.ogrs3OneYear)
    assertNull(result.ogrs3TwoYear)
    assertNull(result.band)
    assertEquals(1, result.validationError?.size)
    val error = result.validationError?.first()
    assertEquals(ValidationErrorType.NO_MATCHING_INPUT, error?.type)
    assertEquals("Error: 123 not found", error?.message)
  }

  private fun validRiskScoreRequest(): RiskScoreRequest = RiskScoreRequest(
    "1_0",
    Gender.MALE,
    LocalDate.of(1964, 10, 15),
    LocalDate.of(2014, 12, 13),
    LocalDate.of(2027, 12, 12),
    10,
    30,
    "051101",
  )
}
