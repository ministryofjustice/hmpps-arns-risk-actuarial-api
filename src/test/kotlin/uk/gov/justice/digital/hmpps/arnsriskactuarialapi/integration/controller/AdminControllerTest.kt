package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeValues
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.IntegrationTestBase
import kotlin.test.assertEquals

class AdminControllerTest : IntegrationTestBase() {

  @Autowired
  protected lateinit var redisTemplate: RedisTemplate<String, OffenceCodeValues>

  @Test
  fun `postUpdateOffenceMappings returns 200 OK when called without Auth`() {
    webTestClient.post()
      .uri("/admin/update-offence-mapping")
      .exchange()
      .expectStatus()
      .isOk

    val keys = redisTemplate.keys("*")
    assertEquals(3028, keys.count())
  }
}
