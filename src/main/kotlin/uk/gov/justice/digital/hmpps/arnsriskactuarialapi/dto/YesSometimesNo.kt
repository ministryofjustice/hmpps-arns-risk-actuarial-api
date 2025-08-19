package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

enum class YesSometimesNo(val score: Int) {
  YES(0),
  SOMETIMES(1),
  NO(2),
}
