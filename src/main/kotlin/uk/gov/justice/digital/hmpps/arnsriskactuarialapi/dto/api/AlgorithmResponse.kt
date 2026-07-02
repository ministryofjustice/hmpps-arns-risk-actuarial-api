package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api

import com.fasterxml.jackson.annotation.JsonValue

enum class AlgorithmResponse(@get:JsonValue output: String) {

  ALL_REOFFENDING_PREDICTOR("All Reoffending Predictor"),
  VIOLENT_REOFFENDING_PREDICTOR("Violent Reoffending Predictor"),
  DIRECT_CONTACT_SEXUAL_REOFFENDING_PREDICTOR("Direct Contact Seexual Reoffending Predictor"),
  IMAGES_AND_INDIRECT_CONTACT_SEXUAL_REOFFENDING_PREDICTOR("Images And Indirect Contact Sexual Reoffending Predictor"),
  SERIOUS_VIOLENT_REOFFENDING_PREDICTOR("Serious Violent Reoffending Predictor"),
  COMBINED_SERIOUS_REOFFENDING_PREDICTOR("Combined Serious Reoffending Predictor"),
}
