package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeValues

@Configuration
class RedisConfig(private val objectMapper: ObjectMapper) {

  @Bean
  fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, OffenceCodeValues> {
    objectMapper.registerKotlinModule()

    val offenceCodeValuesSerializer = Jackson2JsonRedisSerializer(objectMapper, OffenceCodeValues::class.java)

    val template = RedisTemplate<String, OffenceCodeValues>()
    template.connectionFactory = connectionFactory
    template.keySerializer = StringRedisSerializer()
    template.hashKeySerializer = StringRedisSerializer()
    template.valueSerializer = offenceCodeValuesSerializer
    template.hashValueSerializer = offenceCodeValuesSerializer
    template.afterPropertiesSet()
    return template
  }
}
