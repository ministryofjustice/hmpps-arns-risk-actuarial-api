package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.controller

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.ActuarialCategory
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeDetails
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.IntegrationTestBase
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdminControllerTest : IntegrationTestBase() {

  @Autowired
  protected lateinit var redisTemplate: RedisTemplate<String, OffenceCodeDetails>

  @BeforeAll
  fun clearRedisCache() {
    clearOffenceCodeCache()
  }

  @Test
  fun `postUpdateOffenceMappings returns 200 OK when called without Auth`() {
    webTestClient.post()
      .uri("/admin/update-offence-mapping")
      .exchange()
      .expectStatus()
      .isOk

    val keys = redisTemplate.keys("*")
    assertEquals(3223, keys.count())

    val actualOffenceCodeMappings: Map<String, OffenceCodeDetails> = keys.associateWith { key -> redisTemplate.opsForValue().get(key)!! }

    val offenceCodeValuesOffenceCodeValues00100 = actualOffenceCodeMappings["offence_code_mapping_00100"]
    assertEquals("Violence against the person", offenceCodeValuesOffenceCodeValues00100?.parentGroupDescription)
    assertEquals("Murder", offenceCodeValuesOffenceCodeValues00100?.categoryDescription)
    assertEquals("Murder    [Use this code only if you are unable to determine which subcoded Offence applies]", offenceCodeValuesOffenceCodeValues00100?.subCategoryDescription)
    assertEquals(ActuarialCategory.VIOLENCE_AGAINST_THE_PERSON_ABH_PLUS, offenceCodeValuesOffenceCodeValues00100?.actuarialCategory)
    assertTrue(offenceCodeValuesOffenceCodeValues00100?.flags?.opdViolenceSex!!)
    assertTrue(offenceCodeValuesOffenceCodeValues00100.flags.isViolentSanction)

    val offenceCodeValuesOffenceCodeValues08800 = actualOffenceCodeMappings["offence_code_mapping_08800"]
    assertEquals("Sexual offences", offenceCodeValuesOffenceCodeValues08800?.parentGroupDescription)
    assertEquals("Miscellaneous sexual offences", offenceCodeValuesOffenceCodeValues08800?.categoryDescription)
    assertEquals("Miscellaneous sexual offences    [Use this code only if you are unable to determine which subcoded Offence applies]", offenceCodeValuesOffenceCodeValues08800?.subCategoryDescription)
    assertEquals(ActuarialCategory.SEXUAL_NOT_AGAINST_CHILD, offenceCodeValuesOffenceCodeValues08800?.actuarialCategory)
    assertTrue(offenceCodeValuesOffenceCodeValues08800?.flags?.opdViolenceSex!!)
    assertFalse(offenceCodeValuesOffenceCodeValues08800.flags.isViolentSanction)

    val offenceCodeValuesOffenceCodeValues11618 = actualOffenceCodeMappings["offence_code_mapping_11618"]
    assertEquals("Other summary offences", offenceCodeValuesOffenceCodeValues11618?.parentGroupDescription)
    assertEquals("Fishery Laws, offences against", offenceCodeValuesOffenceCodeValues11618?.categoryDescription)
    assertEquals("Offences against Conservation of Seals Act 1970", offenceCodeValuesOffenceCodeValues11618?.subCategoryDescription)
    assertEquals(ActuarialCategory.OTHER_OFFENCES, offenceCodeValuesOffenceCodeValues11618?.actuarialCategory)
    assertFalse(offenceCodeValuesOffenceCodeValues11618?.flags?.opdViolenceSex!!)
    assertFalse(offenceCodeValuesOffenceCodeValues11618.flags.isViolentSanction)

    val offenceCodeValuesOffenceCodeValues99968 = actualOffenceCodeMappings["offence_code_mapping_99968"]
    assertEquals("Other indictable", offenceCodeValuesOffenceCodeValues99968?.parentGroupDescription)
    assertEquals("Libel", offenceCodeValuesOffenceCodeValues99968?.categoryDescription)
    assertEquals("Libel", offenceCodeValuesOffenceCodeValues99968?.subCategoryDescription)
    assertEquals(ActuarialCategory.OTHER_OFFENCES, offenceCodeValuesOffenceCodeValues99968?.actuarialCategory)
    assertFalse(offenceCodeValuesOffenceCodeValues99968?.flags?.opdViolenceSex!!)
    assertFalse(offenceCodeValuesOffenceCodeValues99968.flags.isViolentSanction)
  }

  private fun clearOffenceCodeCache() {
    val keys = redisTemplate.keys("offence_code_mapping_*")
    if (!keys.isNullOrEmpty()) {
      redisTemplate.delete(keys)
    }
  }
}
