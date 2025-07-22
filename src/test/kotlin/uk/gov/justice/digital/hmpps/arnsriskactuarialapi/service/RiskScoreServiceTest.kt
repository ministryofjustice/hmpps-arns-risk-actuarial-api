package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyMST
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyOGP
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyOGRS3
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyOVP
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.omittedPNI

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

  @Mock
  private lateinit var pniRiskProducerService: PNIRiskProducerService

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

    var context = emptyContext()

    val steps = listOf(
      Pair(ogrs3RiskProducerService, { ctx: RiskScoreContext -> ctx.copy(OGRS3 = emptyOGRS3()) }),
      Pair(ovpRiskProducerService, { ctx: RiskScoreContext -> ctx.copy(OVP = emptyOVP()) }),
      Pair(ogpRiskProducerService, { ctx: RiskScoreContext -> ctx.copy(OGP = emptyOGP()) }),
      Pair(mstRiskProducerService, { ctx: RiskScoreContext -> ctx.copy(MST = emptyMST()) }),
      Pair(pniRiskProducerService, { ctx: RiskScoreContext -> ctx.copy(PNI = omittedPNI()) }),
      // add more Pairs for the other mocked risk producers here
    )
    for ((service, transform) in steps) {
      val nextContext = transform(context)
      whenever(service.getRiskScore(request, context)).thenReturn(nextContext)
      context = nextContext
    }

    val result = riskScoreService.riskScoreProducer(request)
    Assertions.assertNotNull(result.OGRS3)
    Assertions.assertNotNull(result.OVP)
    Assertions.assertNotNull(result.OGP)
    Assertions.assertNotNull(result.MST)
    Assertions.assertNotNull(result.PNI)
  }
}
