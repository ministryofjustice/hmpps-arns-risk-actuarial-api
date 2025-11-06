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
  fun `sync should add new keys, update existing keys, and delete obsolete keys`() {
    val existingKeys = setOf("offence_code_mapping_00100", "offence_code_mapping_00101", "offence_code_mapping_01212")
    whenever(redisTemplate.keys(service.offenceCodeMappingPrefixPattern)).thenReturn(existingKeys)
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

    service.sync(offenceCodeMappings)

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

    val keysToDelete = setOf("offence_code_mapping_00101", "offence_code_mapping_01212")
    verify(redisTemplate).delete(keysToDelete)
  }
}