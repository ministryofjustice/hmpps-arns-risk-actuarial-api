package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.RiskScoreResponse

@Service
class RiskScoreService {

  @Autowired
  lateinit var allReoffendingPredictorRiskProducerService: AllReoffendingPredictorRiskProducerService

  @Autowired
  lateinit var directContactSexualReoffendingPredictorRiskProducerService: DirectContactSexualReoffendingPredictorRiskProducerService

  @Autowired
  lateinit var violentReoffendingPredictorRiskProducerService: ViolentReoffendingPredictorRiskProducerService

  @Autowired
  lateinit var mstRiskProducerService: MSTRiskProducerService

  @Autowired
  lateinit var opdRiskProducerService: OPDRiskProducerService

  @Autowired
  lateinit var pniRiskProducerService: PNIRiskProducerService

  @Autowired
  lateinit var ldsRiskProducerService: LDSRiskProducerService

  @Autowired
  lateinit var seriousViolentReoffendingPredictorRiskProducerService: SeriousViolentReoffendingPredictorRiskProducerService

  @Autowired
  lateinit var rsrRiskProducerService: RSRRiskProducerService

  @Autowired
  lateinit var imagesAndIndirectContactSexualReoffendingPredictorRiskProducerService: ImagesAndIndirectContactSexualReoffendingPredictorRiskProducerService

  fun riskScoreProducer(riskScoreRequest: RiskScoreRequest): RiskScoreResponse = listOf(
    allReoffendingPredictorRiskProducerService,
    violentReoffendingPredictorRiskProducerService,
    directContactSexualReoffendingPredictorRiskProducerService,
    mstRiskProducerService,
    opdRiskProducerService,
    pniRiskProducerService,
    ldsRiskProducerService,
    seriousViolentReoffendingPredictorRiskProducerService,
    imagesAndIndirectContactSexualReoffendingPredictorRiskProducerService,
    rsrRiskProducerService,
  ).fold(RiskScoreContext(riskScoreRequest.version)) { context, service ->
    service.calculateRiskScore(riskScoreRequest, context)
  }.toRiskScoreResponse()
}
