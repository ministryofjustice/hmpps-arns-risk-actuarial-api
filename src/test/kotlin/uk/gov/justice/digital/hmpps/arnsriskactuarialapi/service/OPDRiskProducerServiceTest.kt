package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyOPD
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validOPDRiskScoreRequest

class OPDRiskProducerServiceTest {

  private val service = OPDRiskProducerService()

  @Test
  fun `should calculate empty OPD with an valid request`() { // TODO will change in next transformation ticket
    val context = emptyContext().copy(
      OPD = emptyOPD(),
    )
    val request = validOPDRiskScoreRequest()

    val result = service.getRiskScore(request, context).OPD
    assertNotNull(result)
    assertEquals(false, result.opdEligibility)
    assertTrue(result.validationError?.isEmpty() == true)
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
