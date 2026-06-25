package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.ActuarialPredictorsResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.AlgorithmResponse.ALL_REOFFENDING_PREDICTOR
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.AlgorithmResponse.IMAGES_AND_INDIRECT_CONTACT_SEXUAL_REOFFENDING_PREDICTOR
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.AlgorithmResponse.OSPDC
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.AlgorithmResponse.RSR
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.AlgorithmResponse.SERIOUS_VIOLENT_REOFFENDING_PREDICTOR
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.AlgorithmResponse.VIOLENT_REOFFENDING_PREDICTOR
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.AllPredictorPredictorOutputResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.DirectContactSexualPredictorOutputResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.IndirectContactSexualPredictorPredictorOutputResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.PredictorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.RiskScoreResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.ScoreTypeResponse.COMBINED
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.ScoreTypeResponse.STATIC
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.SeriousPredictorComponentScores
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.SeriousPredictorPredictorOutputResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.SeriousViolencePredictorPredictorOutputResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.ViolentPredictorPredictorOutputResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.toRiskBandResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.LDSObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MSTObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd.OPDObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.toScoreTypeResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.asDoublePercentage

fun RiskScoreContext.toRiskScoreResponse(): RiskScoreResponse = RiskScoreResponse(
  version = this.version,
  actuarialPredictors = buildActuarialPredictorsResponse(this),
  lds = buildPredictorResponseForLDS(this),
  mst = buildPredictorResponseForMST(this),
  opd = buildPredictorResponseForOPD(this),
  pni = buildPredictorResponseForPNI(this),
)

private fun buildPredictorResponseForPNI(riskScoreContext: RiskScoreContext): PNIObject? = riskScoreContext.PNI

private fun buildPredictorResponseForLDS(riskScoreContext: RiskScoreContext): LDSObject? = riskScoreContext.LDS

private fun buildPredictorResponseForOPD(riskScoreContext: RiskScoreContext): OPDObject? = riskScoreContext.OPD

private fun buildPredictorResponseForMST(riskScoreContext: RiskScoreContext): MSTObject? = riskScoreContext.MST

private fun buildActuarialPredictorsResponse(riskScoreContext: RiskScoreContext): ActuarialPredictorsResponse = ActuarialPredictorsResponse(
  buildPredictorResponseForAllPredictor(riskScoreContext),
  buildPredictorResponseForViolentPredictor(riskScoreContext),
  buildPredictorResponseForDirectContactSexualPredictor(riskScoreContext),
  buildPredictorResponseForIndirectContactSexualPredictor(riskScoreContext),
  buildPredictorResponseForSeriousViolencePredictor(riskScoreContext),
  buildPredictorResponseForSeriousPredictor(riskScoreContext),
)

private fun buildPredictorResponseForAllPredictor(riskScoreContext: RiskScoreContext): PredictorResponse<AllPredictorPredictorOutputResponse> {
  val allReoffendingPredictor = riskScoreContext.allReoffendingPredictor!!
  return PredictorResponse(
    ALL_REOFFENDING_PREDICTOR,
    allReoffendingPredictor.staticOrDynamic.toScoreTypeResponse(),
    output = AllPredictorPredictorOutputResponse(
      allReoffendingPredictor.band.toRiskBandResponse(),
      allReoffendingPredictor.score,
    ),
    validationErrors = allReoffendingPredictor.validationErrors ?: emptyList(),
    featureValues = allReoffendingPredictor.featureValues ?: emptyMap(),
  )
}

private fun buildPredictorResponseForViolentPredictor(riskScoreContext: RiskScoreContext): PredictorResponse<ViolentPredictorPredictorOutputResponse> {
  val violentReoffendingPredictor = riskScoreContext.violentReoffendingPredictor!!
  return PredictorResponse(
    VIOLENT_REOFFENDING_PREDICTOR,
    violentReoffendingPredictor.staticOrDynamic.toScoreTypeResponse(),
    output = ViolentPredictorPredictorOutputResponse(
      violentReoffendingPredictor.band.toRiskBandResponse(),
      violentReoffendingPredictor.score,
    ),
    validationErrors = violentReoffendingPredictor.validationErrors ?: emptyList(),
    featureValues = violentReoffendingPredictor.featureValues ?: emptyMap(),
  )
}

private fun buildPredictorResponseForDirectContactSexualPredictor(riskScoreContext: RiskScoreContext): PredictorResponse<DirectContactSexualPredictorOutputResponse> {
  val ospdc = riskScoreContext.OSPDC!!
  return PredictorResponse(
    OSPDC,
    STATIC,
    output = DirectContactSexualPredictorOutputResponse(
      ospdc.ospdcBand.toRiskBandResponse(),
      ospdc.pointScore,
      ospdc.ospdcScore?.asDoublePercentage(),
      ospdc.femaleVersion,
      ospdc.sexualOffenceHistory,
      ospdc.ospRiskReduction,
    ),
    validationErrors = ospdc.validationError ?: emptyList(),
    // TODO: Uncomment during ACT-558
//    featureValues = ospdc.featureValues ?: emptyMap(),
  )
}

private fun buildPredictorResponseForIndirectContactSexualPredictor(riskScoreContext: RiskScoreContext): PredictorResponse<IndirectContactSexualPredictorPredictorOutputResponse> {
  val imagesAndIndirectContactSexualReoffendingPredictor = riskScoreContext.imagesAndIndirectContactSexualReoffendingPredictor!!
  return PredictorResponse(
    IMAGES_AND_INDIRECT_CONTACT_SEXUAL_REOFFENDING_PREDICTOR,
    STATIC,
    output = IndirectContactSexualPredictorPredictorOutputResponse(
      band = imagesAndIndirectContactSexualReoffendingPredictor.band.toRiskBandResponse(),
      score = imagesAndIndirectContactSexualReoffendingPredictor.score,
      femaleVersion = imagesAndIndirectContactSexualReoffendingPredictor.femaleVersion,
      hasSexualOffenceHistory = imagesAndIndirectContactSexualReoffendingPredictor.hasEverCommittedSexualOffence,
    ),
    validationErrors = imagesAndIndirectContactSexualReoffendingPredictor.validationErrors ?: emptyList(),
    featureValues = imagesAndIndirectContactSexualReoffendingPredictor.featureValues ?: emptyMap(),
  )
}

private fun buildPredictorResponseForSeriousViolencePredictor(riskScoreContext: RiskScoreContext): PredictorResponse<SeriousViolencePredictorPredictorOutputResponse> {
  val seriousViolentReoffendingPredictor = riskScoreContext.seriousViolentReoffendingPredictor!!
  return PredictorResponse(
    SERIOUS_VIOLENT_REOFFENDING_PREDICTOR,
    seriousViolentReoffendingPredictor.staticOrDynamic.toScoreTypeResponse(),
    output = SeriousViolencePredictorPredictorOutputResponse(
      seriousViolentReoffendingPredictor.band.toRiskBandResponse(),
      seriousViolentReoffendingPredictor.score,
    ),
    validationErrors = seriousViolentReoffendingPredictor.validationErrors ?: emptyList(),
    featureValues = seriousViolentReoffendingPredictor.featureValues ?: emptyMap(),
  )
}

private fun buildPredictorResponseForSeriousPredictor(riskScoreContext: RiskScoreContext): PredictorResponse<SeriousPredictorPredictorOutputResponse> {
  val rsr = riskScoreContext.RSR!!
  return PredictorResponse(
    RSR,
    COMBINED,
    output = SeriousPredictorPredictorOutputResponse(
      rsr.rsrBand.toRiskBandResponse(),
      rsr.rsrScore,
      rsr.femaleVersion,
      rsr.sexualOffenceHistory,
      componentScores = SeriousPredictorComponentScores(
        buildPredictorResponseForDirectContactSexualPredictor(riskScoreContext),
        buildPredictorResponseForIndirectContactSexualPredictor(riskScoreContext),
        buildPredictorResponseForSeriousViolencePredictor(riskScoreContext),
      ),
    ),
    validationErrors = rsr.validationError ?: emptyList(),
  )
}
