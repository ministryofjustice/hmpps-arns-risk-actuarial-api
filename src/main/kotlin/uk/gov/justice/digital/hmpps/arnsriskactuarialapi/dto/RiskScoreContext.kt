package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.LDSObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MSTObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp.OGPObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3.OGRS3Object
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd.OPDObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ovp.OVPObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIObject

data class RiskScoreContext(
  val version: RiskScoreVersion,
  val OGRS3: OGRS3Object? = null,
  val OVP: OVPObject? = null,
  val OGP: OGPObject? = null,
  val MST: MSTObject? = null,
  val OPD: OPDObject? = null,
  val PNI: PNIObject? = null,
  val LDS: LDSObject? = null,
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
)
