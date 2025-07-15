package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.util

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.util.getOffenderCopasScore

class ProbabilityUtilsTest {

  @Test
  fun `getOffenderCopasScore should produce the copas score (Double)`() {
    val result = getOffenderCopasScore(1, 20, 18)
    println(result)
    assertTrue(result is Double, "Expected result to be of type Double but was ${result::class}")

  }
}