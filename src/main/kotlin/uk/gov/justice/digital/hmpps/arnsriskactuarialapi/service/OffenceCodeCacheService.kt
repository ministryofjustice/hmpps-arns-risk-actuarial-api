package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeValues

@Service
class OffenceCodeCacheService(private val redisTemplate: RedisTemplate<String, Any>) {

  val log: Logger = LoggerFactory.getLogger(this::class.java)

  val offenceCodeMappingPrefix = "offence_code_mapping_"
  val offenceCodeMappingPrefixPattern = "$offenceCodeMappingPrefix*"

  fun deleteAll() {
    val keys = redisTemplate.keys(offenceCodeMappingPrefixPattern)
    if (!keys.isNullOrEmpty()) {
      log.info("Deleting ${keys.size} offence code mappings from cache")
      redisTemplate.delete(keys)
    } else {
      log.warn("No offence code mappings found in cache for deletion")
    }
  }

  fun saveAll(offenceCodeMappings: Map<String, OffenceCodeValues>) {
    redisTemplate.opsForValue().multiSet(offenceCodeMappings.mapKeys { (key, _) -> "$offenceCodeMappingPrefix$key" })
    log.info("Saved ${offenceCodeMappings.size} offence code mappings to cache.")
  }
}