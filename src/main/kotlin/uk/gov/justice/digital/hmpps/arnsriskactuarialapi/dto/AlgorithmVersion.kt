package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

enum class AlgorithmVersion(val version: String) {
  V3("3_0"), ;

  override fun toString(): String = version
}
