package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration(buildProperties: BuildProperties) {
  private val version: String = buildProperties.version

  @Bean
  fun customOpenAPI(): OpenAPI = OpenAPI()
    .servers(
      listOf(
        Server().url("https://arns-risk-actuarial-api-dev.hmpps.service.justice.gov.uk").description("Development"),
        Server().url("https://arns-risk-actuarial-api-preprod.hmpps.service.justice.gov.uk").description("Pre-Production"),
        Server().url("https://arns-risk-actuarial-api.hmpps.service.justice.gov.uk").description("Production"),
        Server().url("http://localhost:8080").description("Local"),
      ),
    )
    .tags(
      listOf(),
    )
    .info(
      Info().title("HMPPS ARNS Risk Actuarial Api").version(version)
        .description(
          """
          |API for obtaining risk scores based on data captured in an ARNS assessment.
          |
          |For full details on using this API see this [Confluence](https://dsdmoj.atlassian.net/wiki/spaces/APP/pages/5781389446/Risk+actuarial+API+spec) page.
          """.trimMargin(),
        )
        .contact(Contact().name("HMPPS Digital Studio").email("feedback@digital.justice.gov.uk")),
    )
    .components(
      Components().addSecuritySchemes(
        "arns-risk-actuarial-api-role",
        SecurityScheme().addBearerJwtRequirement(ROLE_ARNS_RISK_ACTUARIAL),
      ),
    )
    .addSecurityItem(SecurityRequirement().addList("arns-risk-actuarial-api-role", listOf("read")))
}

private fun SecurityScheme.addBearerJwtRequirement(role: String): SecurityScheme = type(SecurityScheme.Type.HTTP)
  .scheme("bearer")
  .bearerFormat("JWT")
  .`in`(SecurityScheme.In.HEADER)
  .name("Authorization")
  .description("A HMPPS Auth access token with the `$role` role.")
