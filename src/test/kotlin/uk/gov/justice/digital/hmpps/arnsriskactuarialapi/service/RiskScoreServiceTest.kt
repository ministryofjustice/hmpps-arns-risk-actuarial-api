package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.FIXED_TEST_DATE
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyLDS
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyMST
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyOGP
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyOGRS3
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyOPD
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyOSPDC
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyOVP
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyRSR
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
  private lateinit var opdRiskProducerService: OPDRiskProducerService

  @Mock
  private lateinit var pniRiskProducerService: PNIRiskProducerService

  @Mock
  private lateinit var ldsRiskProducerService: LDSRiskProducerService

  @Mock
  private lateinit var ospdcRiskProducerService: OSPDCRiskProducerService

  @Mock
  private lateinit var rsrRiskProducerService: RSRRiskProducerService

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
      Pair(ogrs3RiskProducerService) { ctx: RiskScoreContext -> ctx.apply { OGRS3 = emptyOGRS3() } },
      Pair(ovpRiskProducerService) { ctx: RiskScoreContext -> ctx.apply { OVP = emptyOVP() } },
      Pair(ogpRiskProducerService) { ctx: RiskScoreContext -> ctx.apply { OGP = emptyOGP() } },
      Pair(mstRiskProducerService) { ctx: RiskScoreContext -> ctx.apply { MST = emptyMST() } },
      Pair(opdRiskProducerService) { ctx: RiskScoreContext -> ctx.apply { OPD = emptyOPD() } },
      Pair(pniRiskProducerService) { ctx: RiskScoreContext -> ctx.apply { PNI = omittedPNI() } },
      Pair(ldsRiskProducerService) { ctx: RiskScoreContext -> ctx.apply { LDS = emptyLDS() } },
      Pair(ospdcRiskProducerService) { ctx: RiskScoreContext -> ctx.apply { OSPDC = emptyOSPDC() } },
      Pair(rsrRiskProducerService) { ctx: RiskScoreContext -> ctx.apply { RSR = emptyRSR() } },

      // add more Pairs for the other mocked risk producers here
    )

    for ((service, transform) in steps) {
      val contextBefore = context.copy()
      transform(context)
      whenever(service.getRiskScore(request, contextBefore)).thenReturn(context.copy())
    }

    val result = riskScoreService.riskScoreProducer(request)

    Assertions.assertNotNull(result.OGRS3)
    Assertions.assertNotNull(result.OVP)
    Assertions.assertNotNull(result.OGP)
    Assertions.assertNotNull(result.MST)
    Assertions.assertNotNull(result.OPD)
    Assertions.assertNotNull(result.LDS)
    Assertions.assertNotNull(result.RSR)
  }
}
