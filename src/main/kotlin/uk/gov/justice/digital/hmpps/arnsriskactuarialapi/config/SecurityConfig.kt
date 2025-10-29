package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import uk.gov.justice.hmpps.kotlin.auth.AuthAwareTokenConverter

@Configuration
class SecurityConfig {

  @Bean
  fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
    http
      .csrf { it.disable() }
      .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
      .authorizeHttpRequests { auth ->
        auth
          .requestMatchers(
            "/health",
            "/health/**",
            "/info",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs*",
            "/v3/api-docs/**",
            "/admin/update-offence-mapping",
          ).permitAll()
          .requestMatchers("/risk-scores/v1").hasAnyAuthority(ROLE_ARNS_RISK_ACTUARIAL)
          .anyRequest().authenticated()
      }
      .oauth2ResourceServer { it.jwt { jwt -> jwt.jwtAuthenticationConverter(AuthAwareTokenConverter()) } }

    return http.build()
  }
}
