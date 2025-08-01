package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.*
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion

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
  private lateinit var opdRiskProducerService: OPDRiskProducerService

  @Mock
  private lateinit var pniRiskProducerService: PNIRiskProducerService

  @Mock
  private lateinit var ldsRiskProducerService: LDSRiskProducerService

  @InjectMocks
  private lateinit var riskScoreService: RiskScoreService

  @Test
  fun `riskScoreProducer returns risk score response with algorithm version as 1_0`() {
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

    val context = emptyContext()

    val steps = listOf(
      Pair(ogrs3RiskProducerService, { ctx: RiskScoreContext -> ctx.apply { OGRS3 = emptyOGRS3() } }),
      Pair(ovpRiskProducerService, { ctx: RiskScoreContext -> ctx.apply { OVP = emptyOVP() } }),
      Pair(ogpRiskProducerService, { ctx: RiskScoreContext -> ctx.apply { OGP = emptyOGP() } }),
      Pair(mstRiskProducerService, { ctx: RiskScoreContext -> ctx.apply { MST = emptyMST() } }),
      Pair(opdRiskProducerService, { ctx: RiskScoreContext -> ctx.apply { OPD = emptyOPD() } }),
      Pair(pniRiskProducerService, { ctx: RiskScoreContext -> ctx.apply { PNI = omittedPNI() } }),
      Pair(ldsRiskProducerService, { ctx: RiskScoreContext -> ctx.apply { LDS = emptyLDS() } }),
      // add more Pairs for the other mocked risk producers here
    )

    for ((service, transform) in steps) {
      val contextBefore = context.copy()
      transform(context)
      whenever(service.getRiskScore(any(), eq(contextBefore)))
        .thenReturn(context.copy())
    }

    val result = riskScoreService.riskScoreProducer(request)

    Assertions.assertNotNull(result.OGRS3)
    Assertions.assertNotNull(result.OVP)
    Assertions.assertNotNull(result.OGP)
    Assertions.assertNotNull(result.MST)
    Assertions.assertNotNull(result.OPD)
    Assertions.assertNotNull(result.LDS)
  }
}
