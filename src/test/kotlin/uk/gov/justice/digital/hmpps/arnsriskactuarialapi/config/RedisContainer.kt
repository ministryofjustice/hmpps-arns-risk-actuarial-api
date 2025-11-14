package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.config

import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

object RedisContainer {
  val instance: GenericContainer<Nothing>? by lazy { startRedisContainer() }

  private fun startRedisContainer(): GenericContainer<Nothing> {
    log.info("Creating a Redis container")
    return GenericContainer<Nothing>("postgres").apply {
      dockerImageName = "redis:8.2.2-alpine"
      withExposedPorts(6379)
      setWaitStrategy(Wait.forListeningPort())
      withReuse(true)
      start()
    }
  }

  private val log = LoggerFactory.getLogger(this::class.java)
}
