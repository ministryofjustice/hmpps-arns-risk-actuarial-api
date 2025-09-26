package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.ActuarialPredictorsResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.AlgorithmResponse.OGP
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.AlgorithmResponse.OGRS3
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.AlgorithmResponse.OSPDC
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.AlgorithmResponse.OSPIIC
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.AlgorithmResponse.OVP
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.AlgorithmResponse.RSR
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.AlgorithmResponse.SNSV
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.AllPredictorPredictorOutputResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.DirectContactSexualPredictorOutputResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.IndirectContactSexualPredictorPredictorOutputResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.PredictorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.RiskScoreResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.ScoreTypeResponse.COMBINED
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.ScoreTypeResponse.DYNAMIC
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.ScoreTypeResponse.STATIC
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.SeriousPredictorPredictorOutputResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.SeriousViolencePredictorPredictorOutputResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.ViolentOrNonViolentPredictorPredictorOutputResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.toRiskBandResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.toScoreTypeResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.LDSObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MSTObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd.OPDObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIObject

fun RiskScoreContext.toRiskScoreResponse(): RiskScoreResponse = RiskScoreResponse(
  this.version,
  buildActuarialPredictorsResponse(this),
  buildPredictorResponseForPNI(this),
  buildPredictorResponseForLDS(this),
  buildPredictorResponseForOPD(this),
  buildPredictorResponseForMST(this),
)

private fun buildPredictorResponseForPNI(riskScoreContext: RiskScoreContext): PNIObject? = riskScoreContext.PNI

private fun buildPredictorResponseForLDS(riskScoreContext: RiskScoreContext): LDSObject? = riskScoreContext.LDS

private fun buildPredictorResponseForOPD(riskScoreContext: RiskScoreContext): OPDObject? = riskScoreContext.OPD

private fun buildPredictorResponseForMST(riskScoreContext: RiskScoreContext): MSTObject? = riskScoreContext.MST

private fun buildActuarialPredictorsResponse(riskScoreContext: RiskScoreContext): ActuarialPredictorsResponse = ActuarialPredictorsResponse(
  buildPredictorResponseForAllPredictor(riskScoreContext),
  buildPredictorResponseForViolentPredictor(riskScoreContext),
  buildPredictorResponseForNonViolentPredictor(riskScoreContext),
  buildPredictorResponseForDirectContactSexualPredictor(riskScoreContext),
  buildPredictorResponseForIndirectContactSexualPredictor(riskScoreContext),
  buildPredictorResponseForSeriousViolencePredictor(riskScoreContext),
  buildPredictorResponseForSeriousPredictor(riskScoreContext),
)

private fun buildPredictorResponseForAllPredictor(riskScoreContext: RiskScoreContext): PredictorResponse {
  val ogrS3 = riskScoreContext.OGRS3!!
  return PredictorResponse(
    OGRS3,
    STATIC,
    output = AllPredictorPredictorOutputResponse(
      ogrS3.band.toRiskBandResponse(),
      ogrS3.ogrs3OneYear,
      ogrS3.ogrs3TwoYear,
    ),
    validationErrors = ogrS3.validationError ?: emptyList(),
  )
}

private fun buildPredictorResponseForViolentPredictor(riskScoreContext: RiskScoreContext): PredictorResponse {
  val ovp = riskScoreContext.OVP!!
  return PredictorResponse(
    OVP,
    DYNAMIC,
    output = ViolentOrNonViolentPredictorPredictorOutputResponse(
      ovp.band.toRiskBandResponse(),
      ovp.provenViolentTypeReoffendingOneYear,
      ovp.provenViolentTypeReoffendingTwoYear,
      ovp.pointScore,
    ),
    validationErrors = ovp.validationError ?: emptyList(),
  )
}

private fun buildPredictorResponseForNonViolentPredictor(riskScoreContext: RiskScoreContext): PredictorResponse {
  val ogp = riskScoreContext.OGP!!
  return PredictorResponse(
    OGP,
    DYNAMIC,
    output = ViolentOrNonViolentPredictorPredictorOutputResponse(
      ogp.bandOGP.toRiskBandResponse(),
      ogp.ogpReoffendingOneYear,
      ogp.ogpReoffendingTwoYear,
      ogp.totalOGPScore,
    ),
    validationErrors = ogp.validationError ?: emptyList(),
  )
}

private fun buildPredictorResponseForDirectContactSexualPredictor(riskScoreContext: RiskScoreContext): PredictorResponse {
  val ospdc = riskScoreContext.OSPDC!!
  return PredictorResponse(
    OSPDC,
    STATIC,
    output = DirectContactSexualPredictorOutputResponse(
      ospdc.ospdcBand.toRiskBandResponse(),
      ospdc.pointScore,
      ospdc.ospdcScore,
      ospdc.femaleVersion,
      ospdc.sexualOffenceHistory,
      ospdc.ospRiskReduction,
    ),
    validationErrors = ospdc.validationError ?: emptyList(),
  )
}

private fun buildPredictorResponseForIndirectContactSexualPredictor(riskScoreContext: RiskScoreContext): PredictorResponse {
  val ospiic = riskScoreContext.OSPIIC!!
  return PredictorResponse(
    OSPIIC,
    STATIC,
    output = IndirectContactSexualPredictorPredictorOutputResponse(
      ospiic.band.toRiskBandResponse(),
      ospiic.score,
      ospiic.femaleVersion,
      ospiic.sexualOffenceHistory,
    ),
    validationErrors = ospiic.validationError ?: emptyList(),
  )
}

private fun buildPredictorResponseForSeriousViolencePredictor(riskScoreContext: RiskScoreContext): PredictorResponse {
  val snsv = riskScoreContext.SNSV!!
  return PredictorResponse(
    SNSV,
    snsv.scoreType.toScoreTypeResponse(),
    output = SeriousViolencePredictorPredictorOutputResponse(
      null,
      snsv.snsvScore,
    ),
    validationErrors = snsv.validationError ?: emptyList(),
  )
}

private fun buildPredictorResponseForSeriousPredictor(riskScoreContext: RiskScoreContext): PredictorResponse {
  val rsr = riskScoreContext.RSR!!
  return PredictorResponse(
    RSR,
    COMBINED,
    output = SeriousPredictorPredictorOutputResponse(
      rsr.rsrBand.toRiskBandResponse(),
      rsr.rsrScore,
      rsr.femaleVersion,
      rsr.sexualOffenceHistory,
      listOf(
        buildPredictorResponseForDirectContactSexualPredictor(riskScoreContext),
        buildPredictorResponseForIndirectContactSexualPredictor(riskScoreContext),
        buildPredictorResponseForSeriousViolencePredictor(riskScoreContext),
      ),
    ),
    validationErrors = rsr.validationError ?: emptyList(),
  )
}
