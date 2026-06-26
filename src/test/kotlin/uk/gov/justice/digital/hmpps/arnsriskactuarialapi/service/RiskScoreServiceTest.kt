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
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyAllReoffendingPredictor
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyDirectContactSexualReoffendingPredictor
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyImagesAndIndirectContactSexualReoffendingPredictor
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyLDS
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyMST
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyOPD
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyRSR
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptySeriousViolentReoffendingPredictor
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyViolentReoffendingPredictor
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.omittedPNI

@ExtendWith(MockitoExtension::class)
class RiskScoreServiceTest {

  @Mock
  private lateinit var allReoffendingPredictorRiskProducerService: AllReoffendingPredictorRiskProducerService

  @Mock
  private lateinit var violentReoffendingPredictorRiskProducerService: ViolentReoffendingPredictorRiskProducerService

  @Mock
  private lateinit var rsrRiskProducerService: RSRRiskProducerService

  @Mock
  private lateinit var seriousViolentReoffendingPredictorRiskProducerService: SeriousViolentReoffendingPredictorRiskProducerService

  @Mock
  private lateinit var directContactSexualReoffendingPredictorRiskProducerService: DirectContactSexualReoffendingPredictorRiskProducerService

  @Mock
  private lateinit var imagesAndIndirectContactSexualReoffendingPredictorRiskProducerService: ImagesAndIndirectContactSexualReoffendingPredictorRiskProducerService

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
      Pair(allReoffendingPredictorRiskProducerService) { ctx: RiskScoreContext ->
        ctx.apply {
          allReoffendingPredictor =
            emptyAllReoffendingPredictor()
        }
      },
      Pair(violentReoffendingPredictorRiskProducerService) { ctx: RiskScoreContext ->
        ctx.apply {
          violentReoffendingPredictor =
            emptyViolentReoffendingPredictor()
        }
      },
      Pair(seriousViolentReoffendingPredictorRiskProducerService) { ctx: RiskScoreContext ->
        ctx.apply {
          seriousViolentReoffendingPredictor = emptySeriousViolentReoffendingPredictor()
        }
      },
      Pair(imagesAndIndirectContactSexualReoffendingPredictorRiskProducerService) { ctx: RiskScoreContext ->
        ctx.apply {
          imagesAndIndirectContactSexualReoffendingPredictor =
            emptyImagesAndIndirectContactSexualReoffendingPredictor()
        }
      },
      Pair(directContactSexualReoffendingPredictorRiskProducerService) { ctx: RiskScoreContext -> ctx.apply { directContactSexualReoffendingPredictor = emptyDirectContactSexualReoffendingPredictor() } },
      Pair(rsrRiskProducerService) { ctx: RiskScoreContext -> ctx.apply { RSR = emptyRSR() } },
      Pair(mstRiskProducerService) { ctx: RiskScoreContext -> ctx.apply { MST = emptyMST() } },
      Pair(opdRiskProducerService) { ctx: RiskScoreContext -> ctx.apply { OPD = emptyOPD() } },
      Pair(pniRiskProducerService) { ctx: RiskScoreContext -> ctx.apply { PNI = omittedPNI() } },
      Pair(ldsRiskProducerService) { ctx: RiskScoreContext -> ctx.apply { LDS = emptyLDS() } },
      // add more Pairs for the other mocked risk producers here
    )

    for ((service, transform) in steps) {
      val contextBefore = context.copy()
      transform(context)
      whenever(service.calculateRiskScore(request, contextBefore)).thenReturn(context.copy())
    }

    val result = riskScoreService.riskScoreProducer(request)

    Assertions.assertNotNull(result)
  }
}
