package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OffenceGroupParametersConfigTest {

  object OffenseGroupParametersConfigTestConstants {
    const val FILE_NAME = "/offensegroupparameters/CT_Offence_min_test.csv"
  }

  private lateinit var offenseGroupParameters: Map<String, OffenceGroupParameters>

  @BeforeEach
  fun setUp() {
    val config = OffenseGroupParametersConfig(OffenseGroupParametersConfigTestConstants.FILE_NAME)
    offenseGroupParameters = config.offenseGroupParameters()
  }

  @Test
  fun `Check Map Size`() {
    assertEquals(100, offenseGroupParameters.size)
  }

  @Test
  fun `Test OGRS3 Map Values`() {
    assertEquals(0.0, offenseGroupParameters["00000"]?.ogrs3Weighting)
    assertEquals(0.0, offenseGroupParameters["00001"]?.ogrs3Weighting)
    assertEquals(0.2622, offenseGroupParameters["00408"]?.ogrs3Weighting)
    assertEquals(-0.6534, offenseGroupParameters["01618"]?.ogrs3Weighting)
    assertNull(offenseGroupParameters["95006"]?.ogrs3Weighting)
    assertNull(offenseGroupParameters["99955"]?.ogrs3Weighting)
  }
}
