package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyMST
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyOGP
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyOGRS3
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyOVP

@ExtendWith(MockitoExtension::class)
class RiskScoreServiceTest {

  @Mock
  private lateinit var ogrs3RiskProducerService: OGRS3RiskProducerService

  @Mock
  private lateinit var ovpRiskProducerService: OVPRiskProducerService

  @Mock
  private lateinit var ogpRiskProducerService: OGPRiskProducerService

  @Mock
  private lateinit var mstRiskProducerService: MSTRiskProducerService

  @InjectMocks
  private lateinit var riskScoreService: RiskScoreService

  @Disabled
  @Test
  fun `riskScoreProducer returns risk score response with algorithm version as 1_0`() {
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
    val context = emptyContext()

    val contextWithOgrs3 = context.copy(OGRS3 = emptyOGRS3())
    whenever(ogrs3RiskProducerService.getRiskScore(request, any()))
      .thenReturn(contextWithOgrs3)

    val contextWithOVP = context.copy(OVP = emptyOVP())
    whenever(ovpRiskProducerService.getRiskScore(request, any()))
      .thenReturn(contextWithOVP)

    val contextWithOGP = context.copy(OGP = emptyOGP())
    whenever(ogpRiskProducerService.getRiskScore(request, any()))
      .thenReturn(contextWithOGP)

    val contextWithMST = context.copy(MST = emptyMST())
    whenever(mstRiskProducerService.getRiskScore(request, any()))
      .thenReturn(contextWithMST)

    val result = riskScoreService.riskScoreProducer(request)
    Assertions.assertEquals("1_0", result.OGRS3!!.algorithmVersion)
  }
}
