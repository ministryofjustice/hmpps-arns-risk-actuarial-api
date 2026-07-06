package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeDetails

@Configuration
class RedisConfig(private val objectMapper: ObjectMapper) {

  @Bean
  fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, OffenceCodeDetails> {
    val offenceCodeDetailsSerializer = JacksonJsonRedisSerializer(objectMapper, OffenceCodeDetails::class.java)

    val template = RedisTemplate<String, OffenceCodeDetails>()
    template.connectionFactory = connectionFactory
    template.keySerializer = StringRedisSerializer()
    template.hashKeySerializer = StringRedisSerializer()
    template.valueSerializer = offenceCodeDetailsSerializer
    template.hashValueSerializer = offenceCodeDetailsSerializer
    template.afterPropertiesSet()
    return template
  }
}
