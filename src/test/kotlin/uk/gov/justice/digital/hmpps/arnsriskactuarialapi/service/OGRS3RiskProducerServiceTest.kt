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
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.FIXED_TEST_DATE
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import java.time.LocalDate
import kotlin.test.assertFailsWith

@ExtendWith(MockitoExtension::class)
class OGRS3RiskProducerServiceTest {

  @Mock
  lateinit var offenceGroupParametersService: OffenceGroupParametersService

  @InjectMocks
  lateinit var ogrs3RiskProducerService: OGRS3RiskProducerService

  @Test
  fun `should return validation error when age at conviction is less than minimum conviction age`() {
    // When
    val result = ogrs3RiskProducerService.getRiskScore(
      validRiskScoreRequest().copy(
        dateOfBirth = LocalDate.of(2000, 1, 1),
        dateOfCurrentConviction = LocalDate.of(2009, 1, 1),
        dateAtStartOfFollowupCalculated = LocalDate.of(2026, 12, 6),
        totalNumberOfSanctionsForAllOffences = Integer.valueOf(2) as Integer?,
        ageAtFirstSanction = Integer.valueOf(9) as Integer?,
        currentOffenceCode = "02700",
      ),
      emptyContext(),
    )

    // Then
    assertNotNull(result)
    assertNull(result.OGRS3?.ogrs3OneYear)
    assertNull(result.OGRS3?.ogrs3TwoYear)
    assertNull(result.OGRS3?.band)
    assertEquals(1, result.OGRS3?.validationError?.size)
    assertEquals(ValidationErrorType.AGE_AT_CURRENT_CONVICTION_LESS_THAN_TEN, result.OGRS3?.validationError?.get(0)?.type)
  }

  @Test
  fun `should return validation error when age at first sanction is greater than age of current conviction`() {
    // When
    val result = ogrs3RiskProducerService.getRiskScore(
      validRiskScoreRequest().copy(
        dateOfBirth = LocalDate.of(1990, 1, 1),
        dateOfCurrentConviction = LocalDate.of(2009, 1, 1),
        dateAtStartOfFollowupCalculated = LocalDate.of(2026, 12, 6),
        totalNumberOfSanctionsForAllOffences = Integer.valueOf(2) as Integer?,
        ageAtFirstSanction = Integer.valueOf(40) as Integer?,
        currentOffenceCode = "02700",
      ),
      emptyContext(),
    )

    // Then
    assertNotNull(result)
    assertNull(result.OGRS3?.ogrs3OneYear)
    assertNull(result.OGRS3?.ogrs3TwoYear)
    assertNull(result.OGRS3?.band)
    assertEquals(1, result.OGRS3?.validationError?.size)
    assertEquals(ValidationErrorType.AGE_AT_FIRST_SANCTION_AFTER_AGE_AT_CURRENT_CONVICTION, result.OGRS3?.validationError?.get(0)?.type)
  }

  @Test
  fun `should return validation error when no offence code mapping is found`() {
    // Given
    whenever(offenceGroupParametersService.getOGRS3Weighting("02700")).thenReturn(null)

    // When
    val result = ogrs3RiskProducerService.getRiskScore(
      validRiskScoreRequest().copy(
        dateOfBirth = LocalDate.of(1965, 12, 7),
        dateOfCurrentConviction = LocalDate.of(2025, 5, 13),
        dateAtStartOfFollowupCalculated = LocalDate.of(2026, 12, 6),
        totalNumberOfSanctionsForAllOffences = Integer.valueOf(2) as Integer?,
        ageAtFirstSanction = Integer.valueOf(47) as Integer?,
        currentOffenceCode = "02700",
      ),
      emptyContext(),
    )

    // Then
    assertNotNull(result)
    assertNull(result.OGRS3?.ogrs3OneYear)
    assertNull(result.OGRS3?.ogrs3TwoYear)
    assertNull(result.OGRS3?.band)
    assertEquals(1, result.OGRS3?.validationError?.size)
    assertEquals(ValidationErrorType.OFFENCE_CODE_MAPPING_NOT_FOUND, result.OGRS3?.validationError?.get(0)?.type)
    assertEquals("No offence code to actuarial weighting mapping found for 02700", result.OGRS3?.validationError?.get(0)?.message)
    assertEquals(listOf("currentOffenceCode"), result.OGRS3?.validationError?.get(0)?.fields)
  }

  @Test
  fun `should return valid OGRS3Object for valid input ACT-62 scenario 1`() {
    // Given
    whenever(offenceGroupParametersService.getOGRS3Weighting("02700")).thenReturn(0.7606)

    // When
    val result = ogrs3RiskProducerService.getRiskScore(
      validRiskScoreRequest().copy(
        dateOfBirth = LocalDate.of(1965, 12, 7),
        dateOfCurrentConviction = LocalDate.of(2025, 5, 13),
        dateAtStartOfFollowupCalculated = LocalDate.of(2026, 12, 6),
        totalNumberOfSanctionsForAllOffences = Integer.valueOf(2) as Integer?,
        ageAtFirstSanction = Integer.valueOf(47) as Integer?,
        currentOffenceCode = "02700",
      ),
      emptyContext(),
    )

    // Then
    assertNotNull(result)
    assertEquals(8, result.OGRS3?.ogrs3OneYear)
    assertEquals(16, result.OGRS3?.ogrs3TwoYear)
    assertEquals(RiskBand.LOW, result.OGRS3?.band)
    assertTrue(result.OGRS3?.validationError.isNullOrEmpty())
  }

  @Test
  fun `should return valid OGRS3Object for valid input ACT-62 scenario 2`() {
    // Given
    whenever(offenceGroupParametersService.getOGRS3Weighting("11618")).thenReturn(0.1599)

    // When
    val result = ogrs3RiskProducerService.getRiskScore(
      validRiskScoreRequest().copy(
        dateOfBirth = LocalDate.of(1991, 7, 17),
        dateOfCurrentConviction = LocalDate.of(2021, 8, 5),
        dateAtStartOfFollowupCalculated = LocalDate.of(2021, 12, 12),
        totalNumberOfSanctionsForAllOffences = Integer.valueOf(2) as Integer?,
        ageAtFirstSanction = Integer.valueOf(21) as Integer?,
        currentOffenceCode = "11618",
      ),
      emptyContext(),
    )

    // Then
    assertNotNull(result)
    assertEquals(11, result.OGRS3?.ogrs3OneYear)
    assertEquals(20, result.OGRS3?.ogrs3TwoYear)
    assertEquals(RiskBand.LOW, result.OGRS3?.band)
    assertTrue(result.OGRS3?.validationError.isNullOrEmpty())
  }

  @Test
  fun `should return null OGRS3Object with error message for exceptions thrown before calculation`() {
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
    val result = ogrs3RiskProducerService.getRiskScore(request, emptyContext())

    val expectedFields = listOf(
      "gender",
      "dateOfBirth",
      "dateOfCurrentConviction",
      "dateAtStartOfFollowupCalculated",
      "totalNumberOfSanctionsForAllOffences",
      "ageAtFirstSanction",
      "currentOffenceCode",
    )

    // Then
    assertNotNull(result)
    assertNull(result.OGRS3?.ogrs3OneYear)
    assertNull(result.OGRS3?.ogrs3TwoYear)
    assertNull(result.OGRS3?.band)
    assertEquals(1, result.OGRS3?.validationError?.size)
    val error = result.OGRS3?.validationError?.first()
    assertEquals(ValidationErrorType.MISSING_MANDATORY_INPUT, error?.type)
    assertEquals("Mandatory input field(s) missing", error?.message)
    assertEquals(expectedFields, error?.fields)
  }

  @Test
  fun `should throw with error message for exceptions thrown during calculation`() {
    // When
    val exception = assertFailsWith<IllegalArgumentException>(
      block = {
        ogrs3RiskProducerService.getRiskScore(
          validRiskScoreRequest().copy(
            dateOfBirth = LocalDate.of(2002, 12, 13),
            dateOfCurrentConviction = LocalDate.of(2000, 12, 13),
          ),
          emptyContext(),
        )
      },
    )

    // Then
    assertEquals("dateOfCurrentConviction cannot be on or before date of birth.", exception.message)
  }

  private fun validRiskScoreRequest(): RiskScoreRequest = RiskScoreRequest(
    RiskScoreVersion.V1_0,
    Gender.MALE,
    FIXED_TEST_DATE,
    LocalDate.of(1964, 10, 15),
    LocalDate.of(2027, 12, 12),
    10 as Integer?,
    30 as Integer?,
    "05110",
    dateAtStartOfFollowupCalculated = LocalDate.of(2014, 12, 13),
  )
}
