package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeValues
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeWeighting
import kotlin.test.assertEquals

class OffenceCodeCacheServiceTest {

  private val redisTemplate: RedisTemplate<String, OffenceCodeValues> = mock()
  private val valueOperations: ValueOperations<String, OffenceCodeValues> = mock()
  private val service: OffenceCodeCacheService = OffenceCodeCacheService(redisTemplate)

  @Test
  fun `sync should add new keys, update existing keys, and delete obsolete keys`() {
    val existingKeys = setOf("offence_code_mapping_00100", "offence_code_mapping_00101", "offence_code_mapping_01212")
    whenever(redisTemplate.keys(service.offenceCodeMappingPrefixPattern)).thenReturn(existingKeys)
    whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)

    val offenceCodeMappings = mapOf(
      "00100" to OffenceCodeValues(
        ogrs3Weighting = OffenceCodeWeighting(0.0, null),
        snsvStaticWeighting = OffenceCodeWeighting(0.01927038224, null),
        snsvDynamicWeighting = OffenceCodeWeighting(-0.006538498404, null),
        snsvVatpStaticWeighting = OffenceCodeWeighting(0.238802610774108, null),
        snsvVatpDynamicWeighting = OffenceCodeWeighting(0.204895023669854, null),
        opdViolenceSexFlag = true,
      ),
      "08801" to OffenceCodeValues(
        ogrs3Weighting = OffenceCodeWeighting(null, OffenceCodeError.NEED_DETAILS_OF_EXACT_OFFENCE),
        snsvStaticWeighting = OffenceCodeWeighting(0.0841789642942883, null),
        snsvDynamicWeighting = OffenceCodeWeighting(0.0819545573517356, null),
        snsvVatpStaticWeighting = OffenceCodeWeighting(0.0, null),
        snsvVatpDynamicWeighting = OffenceCodeWeighting(0.0, null),
        opdViolenceSexFlag = false,
      ),
      "99999" to OffenceCodeValues(
        ogrs3Weighting = OffenceCodeWeighting(null, null),
        snsvStaticWeighting = OffenceCodeWeighting(null, null),
        snsvDynamicWeighting = OffenceCodeWeighting(null, null),
        snsvVatpStaticWeighting = OffenceCodeWeighting(null, null),
        snsvVatpDynamicWeighting = OffenceCodeWeighting(null, null),
        opdViolenceSexFlag = null,
      ),
    )

    service.sync(offenceCodeMappings)

    val expectedOffenceCodeMappingsMap = mapOf(
      "offence_code_mapping_00100" to OffenceCodeValues(
        ogrs3Weighting = OffenceCodeWeighting(0.0, null),
        snsvStaticWeighting = OffenceCodeWeighting(0.01927038224, null),
        snsvDynamicWeighting = OffenceCodeWeighting(-0.006538498404, null),
        snsvVatpStaticWeighting = OffenceCodeWeighting(0.238802610774108, null),
        snsvVatpDynamicWeighting = OffenceCodeWeighting(0.204895023669854, null),
        opdViolenceSexFlag = true,
      ),
      "offence_code_mapping_08801" to OffenceCodeValues(
        ogrs3Weighting = OffenceCodeWeighting(null, OffenceCodeError.NEED_DETAILS_OF_EXACT_OFFENCE),
        snsvStaticWeighting = OffenceCodeWeighting(0.0841789642942883, null),
        snsvDynamicWeighting = OffenceCodeWeighting(0.0819545573517356, null),
        snsvVatpStaticWeighting = OffenceCodeWeighting(0.0, null),
        snsvVatpDynamicWeighting = OffenceCodeWeighting(0.0, null),
        opdViolenceSexFlag = false,
      ),
      "offence_code_mapping_99999" to OffenceCodeValues(
        ogrs3Weighting = OffenceCodeWeighting(null, null),
        snsvStaticWeighting = OffenceCodeWeighting(null, null),
        snsvDynamicWeighting = OffenceCodeWeighting(null, null),
        snsvVatpStaticWeighting = OffenceCodeWeighting(null, null),
        snsvVatpDynamicWeighting = OffenceCodeWeighting(null, null),
        opdViolenceSexFlag = null,
      ),
    )

    verify(valueOperations).multiSet(expectedOffenceCodeMappingsMap)

    val keysToDelete = setOf("offence_code_mapping_00101", "offence_code_mapping_01212")
    verify(redisTemplate).delete(keysToDelete)
  }

  @Test
  fun `getOGRS3Weighting should get OGRS3 weighting`() {
    whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
    whenever(redisTemplate.opsForValue().get("offence_code_mapping_00100")).thenReturn(sampleOffenceCodeMappingValues())
    val ogrs3Weighting = service.getOgrs3Weighting("00100")
    assertEquals(0.0, ogrs3Weighting?.value)
    assertEquals(OffenceCodeError.NEED_DETAILS_OF_EXACT_OFFENCE, ogrs3Weighting?.error)
  }

  @Test
  fun `isViolentOrSexualType should get OPD Sexual Violence flag`() {
    whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
    whenever(redisTemplate.opsForValue().get("offence_code_mapping_00100")).thenReturn(sampleOffenceCodeMappingValues())
    assertTrue(service.isViolentOrSexualType("00100")!!)
  }

  @Test
  fun `getSnsvStaticWeightingValue should get SNSV Static weighting value`() {
    whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
    whenever(redisTemplate.opsForValue().get("offence_code_mapping_00100")).thenReturn(sampleOffenceCodeMappingValues())
    assertEquals(0.01927038224, service.getSnsvStaticWeightingValue("00100"))
  }

  @Test
  fun `getSnsvDynamicWeightingValue should get SNSV Dynamic weighting value`() {
    whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
    whenever(redisTemplate.opsForValue().get("offence_code_mapping_00100")).thenReturn(sampleOffenceCodeMappingValues())
    assertEquals(-0.006538498404, service.getSnsvDynamicWeightingValue("00100"))
  }

  @Test
  fun `getSnsvVatpStaticWeightingValue should get SNSV VATP weighting value`() {
    whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
    whenever(redisTemplate.opsForValue().get("offence_code_mapping_00100")).thenReturn(sampleOffenceCodeMappingValues())
    assertEquals(0.238802610774108, service.getSnsvVatpStaticWeightingValue("00100"))
  }

  @Test
  fun `getSnsvVatpDynamicWeightingValue should get SNSV VATP weighting value`() {
    whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
    whenever(redisTemplate.opsForValue().get("offence_code_mapping_00100")).thenReturn(sampleOffenceCodeMappingValues())
    assertEquals(0.204895023669854, service.getSnsvVatpDynamicWeightingValue("00100"))
  }

  private fun sampleOffenceCodeMappingValues(): OffenceCodeValues = OffenceCodeValues(
    ogrs3Weighting = OffenceCodeWeighting(0.0, OffenceCodeError.NEED_DETAILS_OF_EXACT_OFFENCE),
    snsvStaticWeighting = OffenceCodeWeighting(0.01927038224, null),
    snsvDynamicWeighting = OffenceCodeWeighting(-0.006538498404, null),
    snsvVatpStaticWeighting = OffenceCodeWeighting(0.238802610774108, null),
    snsvVatpDynamicWeighting = OffenceCodeWeighting(0.204895023669854, null),
    opdViolenceSexFlag = true,
  )
}
