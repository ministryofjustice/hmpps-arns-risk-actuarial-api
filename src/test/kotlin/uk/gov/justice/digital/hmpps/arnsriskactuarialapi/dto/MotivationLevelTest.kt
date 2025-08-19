package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class MotivationLevelTest {

  @Test
  fun `enum constants are set correctly`() {
    Assertions.assertEquals(
      mapOf(
        MotivationLevel.FULL_MOTIVATION to 0,
        MotivationLevel.PARTIAL_MOTIVATION to 1,
        MotivationLevel.NO_MOTIVATION to 2,
      ),
      MotivationLevel.entries.associateWith { grading -> grading.ordinal },
    )
  }
}
