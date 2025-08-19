package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

enum class MotivationLevel(val score: Int) {
  FULL_MOTIVATION(0),
  PARTIAL_MOTIVATION(1),
  NO_MOTIVATION(2),
}
