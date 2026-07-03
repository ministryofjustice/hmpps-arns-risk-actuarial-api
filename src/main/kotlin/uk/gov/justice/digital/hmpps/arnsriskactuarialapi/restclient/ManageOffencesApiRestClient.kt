package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.restclient

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeDetails

@Component
class ManageOffencesApiRestClient(
  private val manageOffencesApiWebClient: WebClient,
) {

  val log: Logger = LoggerFactory.getLogger(this::class.java)

  fun getActuarialMapping(): Map<String, OffenceCodeDetails> {
    val path = "/offences/actuarial-mapping"
    return manageOffencesApiWebClient
      .get()
      .uri(path)
      .retrieve()
      .onStatus({ it.is4xxClientError }) {
        log.error("4xx Error retrieving actuarial mapping: ${it.statusCode().value()}")
        handle4xxError(
          it,
          HttpMethod.GET,
          path,
          ExternalService.MANAGE_OFFENCES_API,
        )
      }
      .onStatus({ it.is5xxServerError }) {
        log.error("5xx Error retrieving actuarial mapping: ${it.statusCode().value()}")
        handle5xxError(
          "Failed to retrieve actuarial mapping: ${it.statusCode().value()}",
          HttpMethod.GET,
          path,
          ExternalService.MANAGE_OFFENCES_API,
        )
      }
      .bodyToMono<Map<String, OffenceCodeDetails>>()
      .block().also { log.info("Retrieved actuarial mapping containing ${it?.size ?: 0} entries") }!!
  }
}
