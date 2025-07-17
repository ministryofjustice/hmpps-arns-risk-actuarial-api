package uk.gov.justice.digital.hmpps.arnsriskactuarialapi

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3.OGRS3Object
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ovp.OVPObject

fun emptyOVP(): OVPObject = OVPObject("1_0", null, null, null, null)

fun emptyOGRS3(): OGRS3Object = OGRS3Object("1_0", null, null, null, null)
