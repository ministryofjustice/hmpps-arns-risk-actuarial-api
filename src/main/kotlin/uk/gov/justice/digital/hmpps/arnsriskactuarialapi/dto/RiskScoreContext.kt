package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.allreoffendingpredictor.AllReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.combinedseriousreoffendingpredictor.CombinedSeriousReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.directContactSexualReoffendingPredictor.DirectContactSexualReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.imagesandIndirectcontactsexualreoffendingpredictor.ImagesAndIndirectContactSexualReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.LDSObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MSTObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd.OPDObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.seriousviolentreoffendingpredictor.SeriousViolentReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.violentreoffendingpredictor.ViolentReoffendingPredictorObject

data class RiskScoreContext(
  val version: RiskScoreVersion,
  var allReoffendingPredictor: AllReoffendingPredictorObject? = null,
  var violentReoffendingPredictor: ViolentReoffendingPredictorObject? = null,
  var directContactSexualReoffendingPredictor: DirectContactSexualReoffendingPredictorObject? = null,
  var imagesAndIndirectContactSexualReoffendingPredictor: ImagesAndIndirectContactSexualReoffendingPredictorObject? = null,
  var seriousViolentReoffendingPredictor: SeriousViolentReoffendingPredictorObject? = null,
  var combinedSeriousReoffendingPredictorObject: CombinedSeriousReoffendingPredictorObject? = null,
  var MST: MSTObject? = null,
  var OPD: OPDObject? = null,
  var PNI: PNIObject? = null,
  var LDS: LDSObject? = null,
)
