package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeValues

class OffenceCodeCacheServiceTest {

  private val redisTemplate: RedisTemplate<String, Any> = mock()
  private val valueOperations: ValueOperations<String, Any> = mock()
  private val service: OffenceCodeCacheService = OffenceCodeCacheService(redisTemplate)

  @Test
  fun `deleteAll should delete keys when keys are found`() {
    val keys = setOf("offence_code_mapping_00100", "offence_code_mapping_00101")
    whenever(redisTemplate.keys(service.offenceCodeMappingPrefixPattern)).thenReturn(keys)

    service.deleteAll()

    verify(redisTemplate).delete(keys)
  }

  @Test
  fun `deleteAll should not delete when no keys are found`() {
    whenever(redisTemplate.keys(service.offenceCodeMappingPrefixPattern)).thenReturn(emptySet())

    service.deleteAll()

    verify(redisTemplate, never()).delete(any<String>())
  }

  @Test
  fun `saveAll should save all mappings with correct prefix`() {
    whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)

    val offenceCodeMappings = mapOf(
      "00100" to OffenceCodeValues(
        ogrs3Weighting = 0.0,
        snsvStaticWeighting = 0.01927038224,
        snsvDynamicWeighting = -0.006538498404,
        snsvVatpStaticWeighting = 0.238802610774108,
        snsvVatpDynamicWeighting = 0.204895023669854,
        opdViolenceSexFlag = true,
      ),
      "08801" to OffenceCodeValues(
        ogrs3Weighting = null,
        snsvStaticWeighting = 0.0841789642942883,
        snsvDynamicWeighting = 0.0819545573517356,
        snsvVatpStaticWeighting = 0.0,
        snsvVatpDynamicWeighting = 0.0,
        opdViolenceSexFlag = false,
      ),
      "99999" to OffenceCodeValues(
        ogrs3Weighting = null,
        snsvStaticWeighting = null,
        snsvDynamicWeighting = null,
        snsvVatpStaticWeighting = null,
        snsvVatpDynamicWeighting = null,
        opdViolenceSexFlag = null,
      ),
    )

    service.saveAll(offenceCodeMappings)

    val expectedOffenceCodeMappingsMap = mapOf(
      "offence_code_mapping_00100" to OffenceCodeValues(
        ogrs3Weighting = 0.0,
        snsvStaticWeighting = 0.01927038224,
        snsvDynamicWeighting = -0.006538498404,
        snsvVatpStaticWeighting = 0.238802610774108,
        snsvVatpDynamicWeighting = 0.204895023669854,
        opdViolenceSexFlag = true,
      ),
      "offence_code_mapping_08801" to OffenceCodeValues(
        ogrs3Weighting = null,
        snsvStaticWeighting = 0.0841789642942883,
        snsvDynamicWeighting = 0.0819545573517356,
        snsvVatpStaticWeighting = 0.0,
        snsvVatpDynamicWeighting = 0.0,
        opdViolenceSexFlag = false,
      ),
      "offence_code_mapping_99999" to OffenceCodeValues(
        ogrs3Weighting = null,
        snsvStaticWeighting = null,
        snsvDynamicWeighting = null,
        snsvVatpStaticWeighting = null,
        snsvVatpDynamicWeighting = null,
        opdViolenceSexFlag = null,
      ),
    )

    verify(valueOperations).multiSet(expectedOffenceCodeMappingsMap)
  }
}