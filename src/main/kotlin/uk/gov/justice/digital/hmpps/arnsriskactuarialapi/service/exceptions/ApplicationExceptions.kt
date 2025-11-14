package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.exceptions

import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.restclient.ExternalService

class ExternalApiEntityNotFoundException(
  msg: String? = "",
  val method: HttpMethod,
  val url: String,
  val client: ExternalService,
) : RuntimeException(msg)

class ExternalApiAuthorisationException(
  msg: String? = "",
  val method: HttpMethod,
  val url: String,
  val client: ExternalService,
) : RuntimeException(msg)

class ExternalApiForbiddenException(
  msg: String? = "",
  val method: HttpMethod,
  val url: String,
  val client: ExternalService,
  var moreInfo: List<String> = emptyList(),
) : RuntimeException(msg)

class ExternalApiUnknownException(
  msg: String? = "",
  val method: HttpMethod,
  val url: String,
  val client: ExternalService,
) : RuntimeException(msg)
