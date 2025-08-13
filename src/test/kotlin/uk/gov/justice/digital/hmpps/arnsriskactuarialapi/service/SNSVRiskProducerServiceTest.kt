package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.ScoreType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validSNSVDynamicRiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validSNSVStaticRiskScoreRequest
import java.time.LocalDate
import java.time.Month

@ExtendWith(MockitoExtension::class)
class SNSVRiskProducerServiceTest {

  @Mock
  lateinit var offenceGroupParametersService: OffenceGroupParametersService

  @InjectMocks
  lateinit var service: SNSVRiskProducerService

  @Test
  fun `isSNSVDynamic should return ScoreType STATIC`() {
    val result = service.isSNSVDynamic(
      validSNSVStaticRiskScoreRequest(),
    )

    assertNotNull(result)
    assertEquals(ScoreType.STATIC, result)
  }

  @Test
  fun `getRiskScore should return ScoreType DYNAMIC`() {
    val result = service.isSNSVDynamic(
      validSNSVDynamicRiskScoreRequest(),
    )

    assertNotNull(result)
    assertEquals(ScoreType.DYNAMIC, result)
  }

  @Test
  fun `getRiskScore should return valid SNSVObject with ScoreType STATIC`() {
    whenever(offenceGroupParametersService.getSNSVStaticWeighting("02700")).thenReturn(0.123)
    whenever(offenceGroupParametersService.getSNSVVATPStaticWeighting("02700")).thenReturn(0.123)

    val result = service.getRiskScore(
      validSNSVStaticRiskScoreRequest(),
      emptyContext(),
    )

    assertNotNull(result)
    assertEquals(ScoreType.STATIC, result.SNSV!!.scoreType)
    assertEquals(0.002818580403289646, result.SNSV!!.snsvScore)
  }

  @Test
  fun `getRiskScore should hit calculation error and return UNEXPECTED_VALUE with STATIC score type`() {
    val result = service.getRiskScore(
      validSNSVStaticRiskScoreRequest().copy(dateOfBirth = LocalDate.of(2025, Month.JANUARY, 1)),
      emptyContext(),
    )

    assertNotNull(result)
    assertEquals(ScoreType.STATIC, result.SNSV!!.scoreType)
    assertEquals(1, result.SNSV?.validationError?.size)
    val error = result.SNSV?.validationError?.first()
    assertEquals(ValidationErrorType.UNEXPECTED_VALUE, error?.type)
    assertEquals("Error: Age at assessment date cannot be less than 10", error?.message)
  }
}
