import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "9.5.0"
  id("org.jetbrains.kotlinx.kover") version "0.9.1"
  kotlin("plugin.spring") version "2.1.21"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

kover {
  reports {
    verify {
      rule {
        bound {
          minValue = 85
          aggregationForGroup = AggregationType.COVERED_PERCENTAGE
          coverageUnits = CoverageUnit.BRANCH
        }
      }
    }
  }
}

dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.8.2")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-redis")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.16")
  implementation("org.apache.commons:commons-csv:1.14.1")
  implementation(kotlin("reflect"))

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:1.8.2")
  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.39") {
    exclude(group = "io.swagger.core.v3")
  }
  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testImplementation(kotlin("test"))
  testImplementation("com.oracle.database.jdbc:ojdbc11:23.9.0.25.07")
  testImplementation("com.oracle.database.xml:xdb:23.9.0.25.07")
  testImplementation(platform("org.testcontainers:testcontainers-bom:2.0.4"))
  testImplementation("org.testcontainers:testcontainers")
}

kotlin {
  jvmToolchain(21)
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
  }
}
