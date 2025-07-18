package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp.OGPOutput
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3.OGRS3Object
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ovp.OVPObject

data class RiskScoreResponse(
  val OGRS3: OGRS3Object,
  val OVP: OVPObject,
  val OGP: OGPOutput,
)
