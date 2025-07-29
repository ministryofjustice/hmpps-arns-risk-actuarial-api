package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
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
  fun `should calculate OPD with an valid request, eligible  male`() {
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

    val result = service.getRiskScore(request, context).OPD
    assertNotNull(result)
    assertEquals(true, result.opdEligibility)
    assertTrue(result.validationError?.isEmpty() == true)
    // TODO: check score
  }

  @Test
  fun `should calculate OPD with an valid request, eligible  female`() {
    whenever(offenceGroupParametersService.isViolentOrSexualType("02504")).thenReturn(true)

    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      currentOffence = "02504",
      gender = Gender.FEMALE,
      overallRiskForAssessment = RiskBand.HIGH,
      eligibleForMappa = true,
    )

    val result = service.getRiskScore(request, context).OPD
    assertNotNull(result)
    assertEquals(true, result.opdEligibility)
    assertTrue(result.validationError?.isEmpty() == true)
    // TODO: check score
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

    val result = service.getRiskScore(request, context).OPD
    assertEquals(false, result?.opdEligibility)
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

    val result = service.getRiskScore(request, context).OPD
    assertEquals(false, result?.opdEligibility)
  }

  @Test
  fun `should calculate empty OPD with an invalid request`() {
    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest().copy(
      gender = null,
    )

    val result = service.getRiskScore(request, context).OPD
    assertNotNull(result)
    assertEquals(false, result.opdEligibility)
    assertTrue(result.validationError?.isNotEmpty() == true)
    val error = result.validationError?.first()
    assertEquals(ValidationErrorType.MISSING_INPUT, error?.type)
    assertEquals("ERR5 - Field is Null", error?.message)
    assertTrue(error?.fields?.contains("Gender") == true)
  }
}
