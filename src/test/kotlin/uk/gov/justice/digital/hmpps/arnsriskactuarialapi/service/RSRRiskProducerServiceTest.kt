package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.osp.OSPDCObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ospiic.OSPIICObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.SNSVObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.ScoreType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext

class RSRRiskProducerServiceTest {

  private val service = RSRRiskProducerService()

  @Test
  fun `should calculate RSR score when no validation errors`() {
    val context = emptyContext().copy(
      OSPDC = OSPDCObject(
        ospdcScore = 0.0512312312,
        ospdcBand = RiskBand.LOW,
        pointScore = null,
        validationError = emptyList(),
        femaleVersion = false,
        sexualOffenceHistory = false,
        ospRiskReduction = null,
      ),
      OSPIIC = OSPIICObject(
        score = 0.0212312312,
        band = RiskBand.MEDIUM,
        sexualOffenceHistory = false,
        femaleVersion = true,
        validationError = emptyList(),
      ),
      SNSV = SNSVObject(snsvScore = 0.0312312312, scoreType = ScoreType.DYNAMIC, validationError = emptyList()),
    )

    val result = service.getRiskScore(RiskScoreRequest(), context)

    val rsr = result.RSR!!
    assertEquals(RiskBand.LOW, rsr.ospdcBand)
    assertEquals(5.12, rsr.ospdcScore)
    assertEquals(RiskBand.MEDIUM, rsr.ospiicBand)
    assertEquals(2.12, rsr.ospiicScore)
    assertEquals(10.36, rsr.rsrScore)
    assertNotNull(rsr.rsrBand)
    assertEquals(ScoreType.DYNAMIC, rsr.scoreType)
    assertTrue(rsr.validationError?.isEmpty() == true)
  }

  @Test
  fun `should return null RSR and null scoreType when blocking errors exist `() {
    val errors = listOf(
      ValidationErrorResponse(
        type = ValidationErrorType.MISSING_INPUT,
        message = "ERR",
        fields = listOf(RiskScoreRequest::isCurrentOffenceSexuallyMotivated.name),
      ),
    )
    val context = emptyContext().copy(
      OSPDC = OSPDCObject(
        ospdcScore = 0.02378462734,
        ospdcBand = RiskBand.LOW,
        pointScore = null,
        validationError = errors,
        ospRiskReduction = null,
        femaleVersion = false,
        sexualOffenceHistory = true,
      ),
    )

    val result = service.getRiskScore(RiskScoreRequest(), context)

    val rsr = result.RSR!!
    assertNull(rsr.rsrScore)
    assertNull(rsr.rsrBand)
    assertNull(rsr.scoreType)
    assertEquals(errors, rsr.validationError)
  }

  @Test
  fun `should handle null OSPDC and OSPIIC inputs gracefully`() {
    val context = emptyContext()

    val result = service.getRiskScore(RiskScoreRequest(), context)

    val rsr = result.RSR!!
    assertEquals(0.0, rsr.ospdcScore)
    assertEquals(RiskBand.NOT_APPLICABLE, rsr.ospdcBand)
    assertEquals(0.0, rsr.ospiicScore)
    assertEquals(RiskBand.NOT_APPLICABLE, rsr.ospiicBand)
    assertEquals(0.0, rsr.rsrScore)
    assertNotNull(rsr.rsrBand)
    assertNull(rsr.scoreType)
    assertTrue(rsr.validationError?.isEmpty() == true)
  }

  @Test
  fun `hasBlockingErrors should detect errors that block RSR score`() {
    val service = RSRRiskProducerService()

    val blockingError = listOf(
      ValidationErrorResponse(
        type = ValidationErrorType.MISSING_INPUT,
        message = "ERR",
        fields = listOf(RiskScoreRequest::isCurrentOffenceSexuallyMotivated.name),
      ),
    )
    val nonBlockingError = listOf(
      ValidationErrorResponse(
        type = ValidationErrorType.MISSING_INPUT,
        message = "ERR",
        fields = listOf(RiskScoreRequest::hasEverCommittedSexualOffence.name),
      ),
    )
    assertTrue(service.hasBlockingErrors(blockingError))
    assertFalse(service.hasBlockingErrors(nonBlockingError))
  }
}
