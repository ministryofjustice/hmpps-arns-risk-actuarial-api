package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreDependency
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
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

    whenever(ogrs3RiskProducerService.getRiskScore(request))
      .thenReturn(emptyOGRS3())

    whenever(ovpRiskProducerService.getRiskScore(request))
      .thenReturn(emptyOVP())

    whenever(ogpRiskProducerService.getRiskScore(request, RiskScoreDependency().copy(OGRS3 = emptyOGRS3())))
      .thenReturn(emptyOGP())

    whenever(mstRiskProducerService.getRiskScore(request))
      .thenReturn(emptyMST())

    val result = riskScoreService.riskScoreProducer(request)
    Assertions.assertEquals("1_0", result.OGRS3.algorithmVersion)
  }
}
