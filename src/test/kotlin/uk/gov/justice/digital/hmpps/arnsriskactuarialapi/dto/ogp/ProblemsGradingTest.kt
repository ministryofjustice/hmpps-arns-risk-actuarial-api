package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProblemsGradingTest {

  @Test
  fun `enum constants are set correctly`() {
    assertEquals(
      mapOf(
        ProblemsGrading.NO_PROBLEMS to 0,
        ProblemsGrading.SOME_PROBLEMS to 1,
        ProblemsGrading.SIGNIFICANT_PROBLEMS to 2,
      ),
      ProblemsGrading.entries.associateWith { grading -> grading.value },
    )
  }
}
