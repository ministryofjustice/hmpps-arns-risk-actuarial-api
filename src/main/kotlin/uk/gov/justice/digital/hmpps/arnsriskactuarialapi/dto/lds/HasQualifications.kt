package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds

enum class HasQualifications(val score: Int) {
  NO_QUALIFICATIONS(2),
  ANY_QUALIFICATION(0),
}
