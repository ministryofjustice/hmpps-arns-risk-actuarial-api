package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.allreoffendingpredictor.AllReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.LDSObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MSTObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd.OPDObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.osp.OSPDCObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ospiic.OSPIICObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ovp.OVPObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.rsr.RSRObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.seriousviolentreoffendingpredictor.SeriousViolentReoffendingPredictorObject

data class RiskScoreContext(
  val version: RiskScoreVersion,
  var allReoffendingPredictor: AllReoffendingPredictorObject? = null,
  var OVP: OVPObject? = null,
  var MST: MSTObject? = null,
  var OPD: OPDObject? = null,
  var PNI: PNIObject? = null,
  var LDS: LDSObject? = null,
  var OSPDC: OSPDCObject? = null,
  var OSPIIC: OSPIICObject? = null,
  var seriousViolentReoffendingPredictor: SeriousViolentReoffendingPredictorObject? = null,
  var RSR: RSRObject? = null,
)
