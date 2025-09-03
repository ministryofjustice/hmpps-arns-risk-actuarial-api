package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.toRiskScoreResponse

@Service
class RiskScoreService {

  @Autowired
  lateinit var ogrs3RiskProducerService: OGRS3RiskProducerService

  @Autowired
  lateinit var ovpRiskProducerService: OVPRiskProducerService

  @Autowired
  lateinit var ogpRiskProducerService: OGPRiskProducerService

  @Autowired
  lateinit var mstRiskProducerService: MSTRiskProducerService

  @Autowired
  lateinit var opdRiskProducerService: OPDRiskProducerService

  @Autowired
  lateinit var pniRiskProducerService: PNIRiskProducerService

  @Autowired
  lateinit var ldsRiskProducerService: LDSRiskProducerService

  @Autowired
  lateinit var ospdcRiskProducerService: OSPDCRiskProducerService

  @Autowired
  lateinit var snsvRiskProducerService: SNSVRiskProducerService

  @Autowired
  lateinit var rsrRiskProducerService: RSRRiskProducerService

  @Autowired
  lateinit var ospiicRiskProducerService: OSPIICRiskProducerService

  fun riskScoreProducer(riskScoreRequest: RiskScoreRequest): RiskScoreResponse {
    val listOfVersion1Predictors = listOf(
      ogrs3RiskProducerService,
      ovpRiskProducerService,
      ogpRiskProducerService,
      mstRiskProducerService,
      opdRiskProducerService,
      pniRiskProducerService,
      ldsRiskProducerService,
      ospdcRiskProducerService,
      snsvRiskProducerService,
      ospiicRiskProducerService,
      rsrRiskProducerService,
    )
    val listOfVersion2Predictors = listOf(
      // TODO: ogrs4RiskProducerService,
      ovpRiskProducerService,
      ogpRiskProducerService,
      mstRiskProducerService,
      opdRiskProducerService,
      pniRiskProducerService,
      ldsRiskProducerService,
      ospdcRiskProducerService,
      snsvRiskProducerService,
      ospiicRiskProducerService,
      rsrRiskProducerService,
    )
    val versionedPredictorsList = when (riskScoreRequest.version) {
      RiskScoreVersion.V1_0 -> listOfVersion1Predictors
      RiskScoreVersion.V2_0 -> listOfVersion2Predictors
      else -> emptyList()
    }
    return versionedPredictorsList.fold(RiskScoreContext(riskScoreRequest.version)) { context, service ->
      service.getRiskScore(riskScoreRequest, context)
    }.toRiskScoreResponse()
  }
}
