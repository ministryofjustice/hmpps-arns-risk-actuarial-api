package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds

enum class HasQualifications(val score: Int) {
  ANY_QUALIFICATION(0),
  NO_QUALIFICATIONS(2),
}
