package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.ScoreType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validSNSVDynamicRiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validSNSVStaticRiskScoreRequest

class SNSVRiskProducerServiceTest {

  private val service: SNSVRiskProducerService = SNSVRiskProducerService()

  @Test
  fun `getRiskScore should return valid SNSVObject with ScoreType STATIC`() {
    val result = service.getRiskScore(
      validSNSVStaticRiskScoreRequest(),
      emptyContext(),
    )

    assertNotNull(result)
    assertEquals(ScoreType.STATIC, result.SNSV!!.scoreType)
    assertTrue(result.SNSV!!.validationError.isNullOrEmpty())
  }

  @Test
  fun `getRiskScore should return valid SNSVObject with ScoreType DYNAMIC`() {
    val result = service.getRiskScore(
      validSNSVDynamicRiskScoreRequest(),
      emptyContext(),
    )

    assertNotNull(result)
    assertEquals(ScoreType.DYNAMIC, result.SNSV!!.scoreType)
    assertTrue(result.SNSV!!.validationError.isNullOrEmpty())
  }
}
