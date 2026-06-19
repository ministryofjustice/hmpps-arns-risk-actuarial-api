package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api

import com.fasterxml.jackson.annotation.JsonValue

enum class AlgorithmResponse(@get:JsonValue output: String) {

  ALL_REOFFENDING_PREDICTOR("All Reoffending Predictor"),
  VIOLENT_REOFFENDING_PREDICTOR("Violent Reoffending Predictor"),
  OSPDC("OSP/DC"),
  OSPIIC("OSP/IIC"),
  SNSV("SNSV"),
  RSR("RSR"),
}
