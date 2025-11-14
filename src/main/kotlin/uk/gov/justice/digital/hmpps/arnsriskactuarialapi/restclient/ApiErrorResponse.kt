package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.restclient

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApiErrorResponse(
  val status: Int? = 0,
  val developerMessage: String? = null,
)
