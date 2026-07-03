package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.ActuarialCategory
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeDetails
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeFlags
import kotlin.test.assertEquals

class OffenceCodeCacheServiceTest {

  private val redisTemplate: RedisTemplate<String, OffenceCodeDetails> = mock()
  private val valueOperations: ValueOperations<String, OffenceCodeDetails> = mock()
  private val service: OffenceCodeCacheService = OffenceCodeCacheService(redisTemplate)

  @Test
  fun `sync should add new keys, update existing keys, and delete obsolete keys`() {
    val existingKeys = setOf("offence_code_mapping_10000", "offence_code_mapping_88888", "offence_code_mapping_99999")
    whenever(redisTemplate.keys(service.offenceCodeMappingPrefixPattern)).thenReturn(existingKeys)
    whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)

    val offenceCodeMappings = mapOf(
      "10000" to OffenceCodeDetails(
        parentGroupDescription = "parent group description 10000",
        categoryDescription = "category description 10000",
        subCategoryDescription = "sub category description 10000",
        actuarialCategory = ActuarialCategory.SEXUAL_NOT_AGAINST_CHILD,
        flags = OffenceCodeFlags(
          opdViolenceSex = true,
          isViolentSanction = true,
        ),
      ),
      "20000" to OffenceCodeDetails(
        parentGroupDescription = "parent group description 20000",
        categoryDescription = "category description 20000",
        subCategoryDescription = "sub category description 20000",
        actuarialCategory = ActuarialCategory.DRUNKENNESS,
        flags = OffenceCodeFlags(
          opdViolenceSex = false,
          isViolentSanction = true,
        ),
      ),
      "30000" to OffenceCodeDetails(
        parentGroupDescription = "parent group description 30000",
        categoryDescription = "category description 30000",
        subCategoryDescription = "sub category description 30000",
        actuarialCategory = ActuarialCategory.FRAUD_AND_FORGERY,
        flags = OffenceCodeFlags(
          opdViolenceSex = false,
          isViolentSanction = false,
        ),
      ),
    )

    service.sync(offenceCodeMappings)

    val expectedOffenceCodeMappingsMap = mapOf(
      "offence_code_mapping_10000" to OffenceCodeDetails(
        parentGroupDescription = "parent group description 10000",
        categoryDescription = "category description 10000",
        subCategoryDescription = "sub category description 10000",
        actuarialCategory = ActuarialCategory.SEXUAL_NOT_AGAINST_CHILD,
        flags = OffenceCodeFlags(
          opdViolenceSex = true,
          isViolentSanction = true,
        ),
      ),
      "offence_code_mapping_20000" to OffenceCodeDetails(
        parentGroupDescription = "parent group description 20000",
        categoryDescription = "category description 20000",
        subCategoryDescription = "sub category description 20000",
        actuarialCategory = ActuarialCategory.DRUNKENNESS,
        flags = OffenceCodeFlags(
          opdViolenceSex = false,
          isViolentSanction = true,
        ),
      ),
      "offence_code_mapping_30000" to OffenceCodeDetails(
        parentGroupDescription = "parent group description 30000",
        categoryDescription = "category description 30000",
        subCategoryDescription = "sub category description 30000",
        actuarialCategory = ActuarialCategory.FRAUD_AND_FORGERY,
        flags = OffenceCodeFlags(
          opdViolenceSex = false,
          isViolentSanction = false,
        ),
      ),
    )

    verify(valueOperations).multiSet(expectedOffenceCodeMappingsMap)

    val keysToDelete = setOf("offence_code_mapping_88888", "offence_code_mapping_99999")
    verify(redisTemplate).delete(keysToDelete)
  }

  @Test
  fun `getActuarialCategory should get the actuarial category for the given offence code`() {
    whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
    whenever(redisTemplate.opsForValue().get("offence_code_mapping_00100")).thenReturn(sampleOffenceCodeMappingValues())
    val actuarialCategory = service.getActuarialCategory("00100")
    assertEquals(ActuarialCategory.SEXUAL_NOT_AGAINST_CHILD, actuarialCategory)
  }

  @Test
  fun `isViolentOrSexualType should get OPD Sexual Violence flag`() {
    whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
    whenever(redisTemplate.opsForValue().get("offence_code_mapping_00100")).thenReturn(sampleOffenceCodeMappingValues())
    assertTrue(service.isViolentOrSexualType("00100")!!)
  }

  private fun sampleOffenceCodeMappingValues(): OffenceCodeDetails = OffenceCodeDetails(
    parentGroupDescription = "parent group description 00100",
    categoryDescription = "category description 00100",
    subCategoryDescription = "sub category description 00100",
    actuarialCategory = ActuarialCategory.SEXUAL_NOT_AGAINST_CHILD,
    flags = OffenceCodeFlags(
      opdViolenceSex = true,
      isViolentSanction = true,
    ),
  )
}
