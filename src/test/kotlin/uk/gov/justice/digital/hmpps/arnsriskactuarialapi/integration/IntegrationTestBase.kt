package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.config.RedisContainer
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.wiremock.ManageOffencesApiExtension
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.OffenceCodeService
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper

@ExtendWith(HmppsAuthApiExtension::class, ManageOffencesApiExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class IntegrationTestBase {

  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  @Autowired
  lateinit var offenceCodeService: OffenceCodeService

  @BeforeAll
  fun setup() {
    offenceCodeService.updateOffenceCodeMappings()
  }

  companion object {
    private val redisContainer = RedisContainer.instance

    @JvmStatic
    @DynamicPropertySource
    fun properties(registry: DynamicPropertyRegistry) {
      redisContainer?.run {
        registry.add("spring.data.redis.host", redisContainer::getHost)
        registry.add("spring.data.redis.port") { redisContainer.getMappedPort(6379).toString() }
        registry.add("spring.data.redis.ssl.enabled") { false }
      }
    }
  }

  internal fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  protected fun stubPingWithResponse(status: Int) {
    hmppsAuth.stubHealthPing(status)
  }
}
