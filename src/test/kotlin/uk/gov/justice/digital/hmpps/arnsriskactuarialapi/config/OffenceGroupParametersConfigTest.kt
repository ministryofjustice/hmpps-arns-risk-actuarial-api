package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class OffenceGroupParametersConfigTest {

  object OffenceGroupParametersConfigTestConstants {
    const val FILE_NAME = "/offencegroupparameters/offence-code-mapping-test.csv"
    const val ERR_FILE_NAME = "/offencegroupparameters/offence-code-mapping-error-test.csv"
  }

  private lateinit var offenceGroupParameters: Map<String, OffenceGroupParameters>

  @BeforeEach
  fun setUp() {
    val config = OffenceGroupParametersConfig(OffenceGroupParametersConfigTestConstants.FILE_NAME)
    offenceGroupParameters = config.offenceGroupParameters()
  }

  @Test
  fun `Check Map Size`() {
    assertEquals(100, offenceGroupParameters.size)
  }

  @Test
  fun `Test OGRS3 Map Values`() {
    assertEquals(0.0, offenceGroupParameters["00100"]?.ogrs3Weighting)
    assertEquals(0.0, offenceGroupParameters["00101"]?.ogrs3Weighting)
    assertEquals(0.2622, offenceGroupParameters["00408"]?.ogrs3Weighting)
    assertEquals(-0.6534, offenceGroupParameters["01618"]?.ogrs3Weighting)
    assertNull(offenceGroupParameters["95006"]?.ogrs3Weighting)
    assertNull(offenceGroupParameters["99955"]?.ogrs3Weighting)
  }

  @Test
  fun `Error in OPD_VIOL_SEX field`() {
    val exception = assertFailsWith<IllegalArgumentException>(
      block = {
        OffenceGroupParametersConfig(OffenceGroupParametersConfigTestConstants.ERR_FILE_NAME)
          .offenceGroupParameters()
      },
    )
    assertEquals(
      exception.message,
      "Error in parsing value: 'X' on line 6 (Only 'Y' or 'N' Allowed)",
    )
  }
}
