package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel

data class LDSInputValidated(
  val isCurrentlyOfNoFixedAbodeOrTransientAccommodation: Boolean?,
  val workRelatedSkills: ProblemLevel?,
  val problemsWithReadingWritingNumeracy: ProblemLevel?,
  val hasProblemsWithReading: Boolean?,
  val hasProblemsWithNumeracy: Boolean?,
  val learningDifficulties: ProblemLevel?,
  val professionalOrVocationalQualifications: HasQualifications?,
)
