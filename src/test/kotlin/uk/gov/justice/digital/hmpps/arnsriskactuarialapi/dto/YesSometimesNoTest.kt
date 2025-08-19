package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class YesSometimesNoTest {

  @Test
  fun `enum constants are set correctly`() {
    Assertions.assertEquals(
      mapOf(
        YesSometimesNo.YES to 0,
        YesSometimesNo.SOMETIMES to 1,
        YesSometimesNo.NO to 2,
      ),
      YesSometimesNo.entries.associateWith { grading -> grading.ordinal },
    )
  }
}
