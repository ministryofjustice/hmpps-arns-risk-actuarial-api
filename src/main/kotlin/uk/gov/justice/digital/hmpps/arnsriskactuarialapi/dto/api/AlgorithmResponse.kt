package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api

import com.fasterxml.jackson.annotation.JsonValue

enum class AlgorithmResponse(@get:JsonValue output: String) {

  OGRS3("OGRS3"),
  OVP("OVP"),
  OGP("OGP"),
  OSPDC("OSP/DC"),
  OSPIIC("OSP/IIC"),
  SNSV("SNSV"),
  RSR("RSR"),
}
