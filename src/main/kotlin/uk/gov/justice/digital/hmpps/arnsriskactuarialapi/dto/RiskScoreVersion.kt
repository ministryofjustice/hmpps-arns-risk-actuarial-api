package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

enum class RiskScoreVersion {
  V1_0,
  V2_0,
  ;

  companion object {
    fun getLatestVersion() = entries.last()
  }
}
