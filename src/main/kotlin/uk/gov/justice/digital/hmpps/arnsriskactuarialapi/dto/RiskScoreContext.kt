package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.LDSObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MSTObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp.OGPObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3.OGRS3Object
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd.OPDObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.osp.OSPDCObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ovp.OVPObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.rsr.RSRObject

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
  var RSR: RSRObject? = null,
)

fun RiskScoreContext.toRiskScoreResponse(): RiskScoreResponse = RiskScoreResponse(
  this.version,
  this.OGRS3,
  this.OVP,
  this.OGP,
  this.MST,
  this.OPD,
  this.PNI,
  this.LDS,
  this.RSR,
)
