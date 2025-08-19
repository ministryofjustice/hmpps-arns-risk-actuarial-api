package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.HasQualifications
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.LDSInputValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.LDSObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.LDSRiskProducerService.Companion.getLDSOutput

class LDSRiskProducerServiceCompanionTest {

  @Test
  fun `testing single test case`() {
    val input = LDSInputValidated(
      isCurrentlyOfNoFixedAbodeOrTransientAccommodation = true,
      workRelatedSkills = ProblemLevel.SOME_PROBLEMS,
      problemsWithReadingWritingNumeracy = ProblemLevel.SOME_PROBLEMS,
      hasProblemsWithReading = true,
      hasProblemsWithNumeracy = null,
      learningDifficulties = ProblemLevel.SOME_PROBLEMS,
      professionalOrVocationalQualifications = HasQualifications.NO_QUALIFICATIONS,
    )
    val output = getLDSOutput(input)
    val expected = LDSObject(
      ldsScore = 2,
      validationError = emptyList(),
    )
    assertEquals(expected, output)
  }
}
