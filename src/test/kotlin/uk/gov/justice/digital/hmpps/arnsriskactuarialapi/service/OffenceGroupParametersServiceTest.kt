package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.config.OffenceGroupParametersConfig
import kotlin.test.assertFailsWith

@SpringBootTest(classes = [OffenceGroupParametersConfig::class, OffenceGroupParametersService::class])
class OffenceGroupParametersServiceTest {

  @Autowired
  lateinit var service: OffenceGroupParametersService

  @Test
  fun `Test OGRS3 Map values that are present`() {
    assertEquals(0.0, service.getOGRS3Weighting("00000"))
    assertEquals(0.0, service.getOGRS3Weighting("00001"))
    assertEquals(0.2622, service.getOGRS3Weighting("00408"))
    assertEquals(-0.6534, service.getOGRS3Weighting("01618"))
  }

  @ParameterizedTest
  @ValueSource(strings = ["XX", "95006", "99955"])
  fun `Test OGRS3 values that are NOT present`(exceptionCode: String) {
    val exception = assertFailsWith<NoSuchElementException>(
      block = { service.getOGRS3Weighting(exceptionCode) },
    )
    assertEquals(exception.message, "No Match found on lookup: '$exceptionCode'")
  }

  @ParameterizedTest
  @ValueSource(strings = ["XX", "ABC", ""])
  fun `isViolentOrSexualType should throw when values not found`(exceptionCode: String) {
    val exception = assertFailsWith<NoSuchElementException>(
      block = { service.isViolentOrSexualType(exceptionCode) },
    )
    assertEquals(exception.message, "No Match found on lookup: '$exceptionCode'")
  }

  @ParameterizedTest
  @CsvSource(
    "00000, false",
    "00001, true",
    "99968, false",
    "99959, true",
  )
  fun `isViolentOrSexualType should return when values are found`(offenceCode: String, expected: Boolean) {
    assertEquals(expected, service.isViolentOrSexualType(offenceCode))
  }

  @Test
  fun `Test SNSVStatic Map values that are present`() {
    assertEquals(-0.215779995, service.getSNSVStaticWeighting("09999"))
    assertEquals(-0.215779995, service.getSNSVStaticWeighting("99968"))
  }

  @ParameterizedTest
  @ValueSource(strings = ["XX", "99969", "00000"])
  fun `Test SNSVStatic values that are NOT present`(exceptionCode: String) {
    val exception = assertFailsWith<NoSuchElementException>(
      block = { service.getSNSVStaticWeighting(exceptionCode) },
    )
    assertEquals(exception.message, "No Match found on lookup: '$exceptionCode'")
  }

  @Test
  fun `Test SNSVVATPStatic Map values that are present`() {
    assertEquals(0.238802611, service.getSNSVVATPStaticWeighting("00101"))
    assertEquals(0.503126183, service.getSNSVVATPStaticWeighting("99958"))
  }

  @ParameterizedTest
  @ValueSource(strings = ["XX", "99969", "00000"])
  fun `Test SNSVVATPStatic values that are NOT present`(exceptionCode: String) {
    val exception = assertFailsWith<NoSuchElementException>(
      block = { service.getSNSVVATPStaticWeighting(exceptionCode) },
    )
    assertEquals(exception.message, "No Match found on lookup: '$exceptionCode'")
  }

  @Test
  fun `Test SNSVDynamic Map values that are present`() {
    assertEquals(-0.006538498, service.getSNSVDynamicWeighting("00101"))
    assertEquals(-0.006538498, service.getSNSVDynamicWeighting("99958"))
  }

  @ParameterizedTest
  @ValueSource(strings = ["XX", "99969", "00000"])
  fun `Test SNSVDynamic values that are NOT present`(exceptionCode: String) {
    val exception = assertFailsWith<NoSuchElementException>(
      block = { service.getSNSVDynamicWeighting(exceptionCode) },
    )
    assertEquals(exception.message, "No Match found on lookup: '$exceptionCode'")
  }

  @Test
  fun `Test SNSVVATPDynamic Map values that are present`() {
    assertEquals(0.204895024, service.getSNSVVATPDynamicWeighting("00101"))
    assertEquals(0.413159451, service.getSNSVVATPDynamicWeighting("99958"))
  }

  @ParameterizedTest
  @ValueSource(strings = ["XX", "99969", "00000"])
  fun `Test SNSVVATPDynamic values that are NOT present`(exceptionCode: String) {
    val exception = assertFailsWith<NoSuchElementException>(
      block = { service.getSNSVVATPDynamicWeighting(exceptionCode) },
    )
    assertEquals(exception.message, "No Match found on lookup: '$exceptionCode'")
  }
}
