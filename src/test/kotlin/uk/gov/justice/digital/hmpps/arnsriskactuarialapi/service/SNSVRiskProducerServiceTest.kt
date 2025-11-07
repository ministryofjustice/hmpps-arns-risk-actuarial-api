package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.SupervisionStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.ScoreType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validSNSVDynamicRiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validSNSVStaticRiskScoreRequest
import java.time.LocalDate
import java.time.Month
import kotlin.test.assertFailsWith

@ExtendWith(MockitoExtension::class)
class SNSVRiskProducerServiceTest {

  @Mock
  lateinit var offenceGroupParametersService: OffenceGroupParametersService

  @InjectMocks
  lateinit var service: SNSVRiskProducerService

  @Test
  fun `getSNSVScoreType should return STATIC_WITH_VALIDATION_ERRORS when snsv type not specified`() {
    val result = service.getSNSVScoreType(
      validSNSVStaticRiskScoreRequest(),
    )

    assertNotNull(result)
    assertEquals(ScoreType.STATIC_WITH_VALIDATION_ERRORS, result)
  }

  @Test
  fun `getSNSVScoreType should return DYNAMIC when snsv type not specified`() {
    val result = service.getSNSVScoreType(
      validSNSVDynamicRiskScoreRequest(),
    )

    assertNotNull(result)
    assertEquals(ScoreType.DYNAMIC, result)
  }

  @Test
  fun `getSNSVScoreType should return STATIC when snsv type specified`() {
    val result = service.getSNSVScoreType(
      validSNSVDynamicRiskScoreRequest().copy(snsvStaticOrDynamic = StaticOrDynamic.STATIC),
    )

    assertNotNull(result)
    assertEquals(ScoreType.STATIC, result)
  }

  @Test
  fun `getSNSVScoreType should return DYNAMIC when snsv type specified`() {
    val result = service.getSNSVScoreType(
      validSNSVStaticRiskScoreRequest().copy(snsvStaticOrDynamic = StaticOrDynamic.DYNAMIC),
    )

    assertNotNull(result)
    assertEquals(ScoreType.DYNAMIC, result)
  }

  @ParameterizedTest
  @CsvSource(value = ["COMMUNITY, REMAND"])
  fun `getRiskScore should return valid SNSVObject with ScoreType DYNAMIC`(supervisionStatus: SupervisionStatus) {
    whenever(offenceGroupParametersService.getSNSVDynamicWeighting("02700")).thenReturn(0.123)
    whenever(offenceGroupParametersService.getSNSVVATPDynamicWeighting("02700")).thenReturn(0.123)

    val result = service.getRiskScore(
      validSNSVDynamicRiskScoreRequest().copy(supervisionStatus = supervisionStatus),
      emptyContext(),
    )

    assertNotNull(result)
    assertEquals(ScoreType.DYNAMIC, result.SNSV!!.scoreType)
    assertEquals(0.0027603294949487, result.SNSV!!.snsvScore!!, 1E-8)
  }

  @ParameterizedTest
  @CsvSource(value = ["COMMUNITY, REMAND"])
  fun `getRiskScore should return valid SNSVObject with ScoreType STATIC`(supervisionStatus: SupervisionStatus) {
    whenever(offenceGroupParametersService.getSNSVStaticWeighting("02700")).thenReturn(0.123)
    whenever(offenceGroupParametersService.getSNSVVATPStaticWeighting("02700")).thenReturn(0.123)

    val result = service.getRiskScore(
      validSNSVStaticRiskScoreRequest().copy(supervisionStatus = supervisionStatus),
      emptyContext(),
    )

    assertNotNull(result)
    assertEquals(ScoreType.STATIC_WITH_VALIDATION_ERRORS, result.SNSV!!.scoreType)
    assertEquals(0.0021598644399697, result.SNSV!!.snsvScore!!, 1E-8)
  }

  @Test
  fun `getRiskScore should return validation error SNSVObject with ScoreType STATIC and no offence code mapping`() {
    whenever(offenceGroupParametersService.getSNSVStaticWeighting("02700")).thenReturn(null)
    whenever(offenceGroupParametersService.getSNSVVATPStaticWeighting("02700")).thenReturn(null)

    val result = service.getRiskScore(
      validSNSVStaticRiskScoreRequest().copy(supervisionStatus = SupervisionStatus.COMMUNITY),
      emptyContext(),
    )

    assertNotNull(result)
    assertEquals(1, result.SNSV!!.validationError?.size)
    assertEquals(ValidationErrorType.OFFENCE_CODE_MAPPING_NOT_FOUND, result.SNSV!!.validationError?.first()?.type)
    assertEquals("No offence code to actuarial weighting mapping found for 02700", result.SNSV!!.validationError?.first()?.message)
    assertEquals(listOf("currentOffenceCode"), result.SNSV!!.validationError?.first()?.fields)
  }

  @Test
  fun `getRiskScore should default to ScoreType STATIC with invalid domesticViolencePerpetrator params`() {
    val result = service.getRiskScore(
      validSNSVDynamicRiskScoreRequest().copy(
        evidenceOfDomesticAbuse = null,
      ),
      emptyContext(),
    )

    assertNotNull(result)
    assertEquals(ScoreType.STATIC_WITH_VALIDATION_ERRORS, result.SNSV!!.scoreType)
    assertEquals(0.0020745554019918, result.SNSV!!.snsvScore!!, 1E-8)
  }

  @Test
  fun `getRiskScore should hit calculation error and throw with DYNAMIC score type`() {
    val exception = assertFailsWith<IllegalArgumentException>(
      block = {
        service.getRiskScore(
          validSNSVDynamicRiskScoreRequest().copy(dateOfBirth = LocalDate.of(2025, Month.JANUARY, 1)),
          emptyContext(),
        )
      },
    )

    assertEquals("Age at date at start of followup cannot be less than 10", exception.message)
  }

  @Test
  fun `getRiskScore should hit calculation error and throw with STATIC score type`() {
    val exception = assertFailsWith<IllegalArgumentException>(
      block = {
        service.getRiskScore(
          validSNSVStaticRiskScoreRequest().copy(
            dateOfBirth = LocalDate.of(2025, Month.JANUARY, 1),
            dateAtStartOfFollowupUserInput = LocalDate.of(2027, 1, 1),
          ),
          emptyContext(),
        )
      },
    )

    assertEquals("Age at date at start of followup cannot be less than 10", exception.message)
  }
}
