package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.ZoneOffset

class ManageOffencesApiExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val manageOffences = ManageOffencesMockServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    manageOffences.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    manageOffences.resetRequests()
  }

  override fun afterAll(context: ExtensionContext) {
    manageOffences.stop()
  }
}

class ManageOffencesMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 6379
  }

  fun stubActuarialMapping() {
    val responsePath = "wiremock-manage-offences-api/mappings/get-actuarial-mapping.json"

    stubFor(
      post(urlEqualTo("/actuarial-mapping"))
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(Files.readString(Paths.get(responsePath))),
        ),
    )
  }
}
