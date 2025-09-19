package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.config.OffenceGroupParametersConfig

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
    assertNull(service.getOGRS3Weighting(exceptionCode))
  }

  @ParameterizedTest
  @ValueSource(strings = ["XX", "ABC", ""])
  fun `isViolentOrSexualType should throw when values not found`(exceptionCode: String) {
    assertNull(service.isViolentOrSexualType(exceptionCode))
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
    assertEquals(-0.215779995107354, service.getSNSVStaticWeighting("09999"))
    assertEquals(-0.215779995107354, service.getSNSVStaticWeighting("99968"))
  }

  @ParameterizedTest
  @ValueSource(strings = ["XX", "99969", "00000"])
  fun `Test SNSVStatic values that are NOT present`(exceptionCode: String) {
    assertNull(service.getSNSVStaticWeighting(exceptionCode))
  }

  @Test
  fun `Test SNSVVATPStatic Map values that are present`() {
    assertEquals(0.238802610774108, service.getSNSVVATPStaticWeighting("00101"))
    assertEquals(0.503126183131338, service.getSNSVVATPStaticWeighting("99958"))
  }

  @ParameterizedTest
  @ValueSource(strings = ["XX", "99969", "00000"])
  fun `Test SNSVVATPStatic values that are NOT present`(exceptionCode: String) {
    assertNull(service.getSNSVVATPStaticWeighting(exceptionCode))
  }

  @Test
  fun `Test SNSVDynamic Map values that are present`() {
    assertEquals(-0.006538498404, service.getSNSVDynamicWeighting("00101"))
    assertEquals(-0.006538498404, service.getSNSVDynamicWeighting("99958"))
  }

  @ParameterizedTest
  @ValueSource(strings = ["XX", "99969", "00000"])
  fun `Test SNSVDynamic values that are NOT present`(exceptionCode: String) {
    assertNull(service.getSNSVDynamicWeighting(exceptionCode))
  }

  @Test
  fun `Test SNSVVATPDynamic Map values that are present`() {
    assertEquals(0.204895023669854, service.getSNSVVATPDynamicWeighting("00101"))
    assertEquals(0.41315945136753, service.getSNSVVATPDynamicWeighting("99958"))
  }

  @ParameterizedTest
  @ValueSource(strings = ["XX", "99969", "00000"])
  fun `Test SNSVVATPDynamic values that are NOT present`(exceptionCode: String) {
    assertNull(service.getSNSVVATPDynamicWeighting(exceptionCode))
  }
}
