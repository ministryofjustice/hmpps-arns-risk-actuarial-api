package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

  @Bean
  fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
    http.securityMatcher("/admin/update-offence-mapping")
      .authorizeHttpRequests { it.anyRequest().permitAll() }
    return http.build()
  }
}