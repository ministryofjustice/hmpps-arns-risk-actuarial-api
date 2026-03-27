package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.cfg.CoercionAction
import tools.jackson.databind.cfg.CoercionInputShape
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.cfg.EnumFeature
import tools.jackson.module.kotlin.kotlinModule
import java.text.SimpleDateFormat

@Configuration
class JacksonConfig {

  @Bean
  fun jacksonCustomiser(@Value("\${spring.jackson.date-format}") dateFormat: String): JsonMapperBuilderCustomizer = JsonMapperBuilderCustomizer { builder ->
    builder
      .addModule(kotlinModule())
      .defaultDateFormat(SimpleDateFormat(dateFormat))
      .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
      .configure(EnumFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, false)
      .configure(EnumFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
      .configure(EnumFeature.READ_ENUMS_USING_TO_STRING, true)
      .withCoercionConfig(Boolean::class.javaObjectType) { config ->
        config.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
          .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
          .setCoercion(CoercionInputShape.String, CoercionAction.Fail)
      }
      .withCoercionConfig(Boolean::class.javaPrimitiveType) { config ->
        config.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
          .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
          .setCoercion(CoercionInputShape.String, CoercionAction.Fail)
      }
  }
}
