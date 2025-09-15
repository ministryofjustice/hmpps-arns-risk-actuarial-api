package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.LDSObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MSTObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp.OGPObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3.OGRS3Object
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd.OPDObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ovp.OVPObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.rsr.RSRObject

@JsonPropertyOrder("version", alphabetic = true)
data class RiskScoreResponse(
  val version: RiskScoreVersion,
  val OGRS3: OGRS3Object?,
  val OVP: OVPObject?,
  val OGP: OGPObject?,
  val MST: MSTObject?,
  val OPD: OPDObject?,
  val PNI: PNIObject?,
  val LDS: LDSObject?,
  val RSR: RSRObject?,
)
