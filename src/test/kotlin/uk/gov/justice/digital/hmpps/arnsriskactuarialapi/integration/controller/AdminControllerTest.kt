package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.controller

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeValues
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.IntegrationTestBase
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

    val actualOffenceCodeMappings: Map<String, OffenceCodeValues> = keys.associateWith { key -> redisTemplate.opsForValue().get(key)!! }

    val offenceCodeValuesOffenceCodeValues00100 = actualOffenceCodeMappings.get("offence_code_mapping_00100")
    assertTrue(offenceCodeValuesOffenceCodeValues00100?.opdViolenceSexFlag!!)
    assertEquals(0.0, offenceCodeValuesOffenceCodeValues00100.ogrs3Weighting.value)
    assertNull(offenceCodeValuesOffenceCodeValues00100.ogrs3Weighting.error)
    assertEquals(-0.006538498404, offenceCodeValuesOffenceCodeValues00100.snsvDynamicWeighting.value)
    assertEquals(0.01927038224, offenceCodeValuesOffenceCodeValues00100.snsvStaticWeighting.value)
    assertEquals(0.204895023669854, offenceCodeValuesOffenceCodeValues00100.snsvVatpDynamicWeighting.value)
    assertEquals(0.238802610774108, offenceCodeValuesOffenceCodeValues00100.snsvVatpStaticWeighting.value)

    val offenceCodeValuesOffenceCodeValues08800 = actualOffenceCodeMappings.get("offence_code_mapping_08800")
    assertTrue(offenceCodeValuesOffenceCodeValues08800?.opdViolenceSexFlag!!)
    assertNull(offenceCodeValuesOffenceCodeValues08800.ogrs3Weighting.value)
    assertEquals(OffenceCodeError.NEED_DETAILS_OF_EXACT_OFFENCE, offenceCodeValuesOffenceCodeValues08800.ogrs3Weighting.error)
    assertEquals(-0.211172350332101, offenceCodeValuesOffenceCodeValues08800.snsvDynamicWeighting.value)
    assertEquals(-0.0455228893277184, offenceCodeValuesOffenceCodeValues08800.snsvStaticWeighting.value)
    assertEquals(0.0, offenceCodeValuesOffenceCodeValues08800.snsvVatpDynamicWeighting.value)
    assertEquals(0.0, offenceCodeValuesOffenceCodeValues08800.snsvVatpStaticWeighting.value)

    val offenceCodeValuesOffenceCodeValues11618 = actualOffenceCodeMappings.get("offence_code_mapping_11618")
    assertFalse(offenceCodeValuesOffenceCodeValues11618?.opdViolenceSexFlag!!)
    assertEquals(-0.0607, offenceCodeValuesOffenceCodeValues11618.ogrs3Weighting.value)
    assertNull(offenceCodeValuesOffenceCodeValues11618.ogrs3Weighting.error)
    assertEquals(0.123617390798186, offenceCodeValuesOffenceCodeValues11618.snsvDynamicWeighting.value)
    assertEquals(-0.215779995107354, offenceCodeValuesOffenceCodeValues11618.snsvStaticWeighting.value)
    assertEquals(0.0, offenceCodeValuesOffenceCodeValues11618.snsvVatpDynamicWeighting.value)
    assertEquals(0.0, offenceCodeValuesOffenceCodeValues11618.snsvVatpStaticWeighting.value)

    val offenceCodeValuesOffenceCodeValues99968 = actualOffenceCodeMappings.get("offence_code_mapping_99968")
    assertFalse(offenceCodeValuesOffenceCodeValues99968?.opdViolenceSexFlag!!)
    assertEquals(-0.0607, offenceCodeValuesOffenceCodeValues99968.ogrs3Weighting.value)
    assertNull(offenceCodeValuesOffenceCodeValues99968.ogrs3Weighting.error)
    assertEquals(0.123617390798186, offenceCodeValuesOffenceCodeValues99968.snsvDynamicWeighting.value)
    assertEquals(-0.215779995107354, offenceCodeValuesOffenceCodeValues99968.snsvStaticWeighting.value)
    assertEquals(0.0, offenceCodeValuesOffenceCodeValues99968.snsvVatpDynamicWeighting.value)
    assertEquals(0.0, offenceCodeValuesOffenceCodeValues99968.snsvVatpStaticWeighting.value)
  }
}
