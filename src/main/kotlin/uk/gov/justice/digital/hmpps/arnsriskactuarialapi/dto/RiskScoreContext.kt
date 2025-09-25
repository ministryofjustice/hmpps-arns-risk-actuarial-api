package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.LDSObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MSTObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp.OGPObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3.OGRS3Object
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd.OPDObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.osp.OSPDCObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ospiic.OSPIICObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ovp.OVPObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.rsr.RSRObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.SNSVObject

data class RiskScoreContext(
  val version: RiskScoreVersion,
  var OGRS3: OGRS3Object? = null,
  var OVP: OVPObject? = null,
  var OGP: OGPObject? = null,
  var MST: MSTObject? = null,
  var OPD: OPDObject? = null,
  var PNI: PNIObject? = null,
  var LDS: LDSObject? = null,
  var OSPDC: OSPDCObject? = null,
  var OSPIIC: OSPIICObject? = null,
  var SNSV: SNSVObject? = null,
  var RSR: RSRObject? = null,
)
