package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel

data class LDSInputValidated(
  val isCurrentlyOfNoFixedAbodeOrTransientAccommodation: Boolean?,
  val transferableSkills: ProblemLevel?,
  val educationDifficulties: ProblemLevel?,
  val readingDifficulties: Boolean?,
  val numeracyDifficulties: Boolean?,
  val learningDifficulties: ProblemLevel?,
  val professionalOrVocationalQualifications: HasQualifications?,
)
