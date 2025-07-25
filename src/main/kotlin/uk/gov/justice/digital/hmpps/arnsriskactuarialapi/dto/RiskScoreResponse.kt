package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MSTObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp.OGPObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3.OGRS3Object
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ovp.OVPObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIObject

data class RiskScoreResponse(
  val version: RiskScoreVersion,
  val OGRS3: OGRS3Object?,
  val OVP: OVPObject?,
  val OGP: OGPObject?,
  val MST: MSTObject?,
  val PNI: PNIObject?,
)
