package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

enum class ProblemLevel(val score: Int) {
  NO_PROBLEMS(0),
  SOME_PROBLEMS(1),
  SIGNIFICANT_PROBLEMS(2),
}
