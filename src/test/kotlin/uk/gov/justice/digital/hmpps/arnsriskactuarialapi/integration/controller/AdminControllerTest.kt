package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.controller

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.IntegrationTestBase

class AdminControllerTest : IntegrationTestBase() {

  @Test
  fun `postUpdateOffenceMappings returns 200 OK when called without Auth`() {
    webTestClient.post()
      .uri("/admin/update-offence-mapping")
      .exchange()
      .expectStatus()
      .isOk
  }
}
