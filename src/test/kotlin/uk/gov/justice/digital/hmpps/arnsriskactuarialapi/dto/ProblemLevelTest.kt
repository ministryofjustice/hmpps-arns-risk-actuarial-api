package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProblemLevelTest {

  @Test
  fun `enum constants are set correctly`() {
    assertEquals(
      mapOf(
        ProblemLevel.NO_PROBLEMS to 0,
        ProblemLevel.SOME_PROBLEMS to 1,
        ProblemLevel.SIGNIFICANT_PROBLEMS to 2,
      ),
      ProblemLevel.entries.associateWith { grading -> grading.ordinal },
    )
  }
}
