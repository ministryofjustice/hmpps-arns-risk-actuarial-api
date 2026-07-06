package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.ActuarialCategory
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeDetails

@Service
class OffenceCodeCacheService(private val redisTemplate: RedisTemplate<String, OffenceCodeDetails>) {

  val log: Logger = LoggerFactory.getLogger(this::class.java)

  val offenceCodeMappingPrefix = "offence_code_mapping_"
  val offenceCodeMappingPrefixPattern = "$offenceCodeMappingPrefix*"

  fun sync(offenceCodeMappings: Map<String, OffenceCodeDetails>) {
    val existingKeys = redisTemplate.keys(offenceCodeMappingPrefixPattern)
    log.info("Found ${existingKeys.size} existing offence code mappings within cache.")

    redisTemplate.opsForValue().multiSet(offenceCodeMappings.mapKeys { (key, _) -> offenceCodeMappingPrefix + key })
    log.info("Created/Updated ${offenceCodeMappings.size} offence code mappings within cache.")

    val keysToDelete = existingKeys - offenceCodeMappings.keys.map { offenceCodeMappingPrefix + it }.toSet()
    if (keysToDelete.isNotEmpty()) {
      redisTemplate.delete(keysToDelete)
      log.info("Deleted ${keysToDelete.size} offence code mappings from cache.")
    }
  }

  private fun get(offenceKey: String): OffenceCodeDetails? = redisTemplate.opsForValue().get(offenceCodeMappingPrefix + offenceKey)

  fun getActuarialCategory(offenceKey: String): ActuarialCategory? = get(offenceKey)?.actuarialCategory

  fun isViolentOrSexualType(offenceKey: String): Boolean? = get(offenceKey)?.flags?.opdViolenceSex
}
