package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

enum class RiskScoreVersion(val ogrs3Version: OGRS3Version, val ogpVersion: OGPVersion, val ovpVersion: OVPVersion, val mstVersion: MSTVersion, val pniVersion: PNIVersion) {
  V1_0(
    ogrs3Version = OGRS3Version.V3_0,
    ogpVersion = OGPVersion.V1_0,
    ovpVersion = OVPVersion.V1_0,
    mstVersion = MSTVersion.V1_0,
    pniVersion = PNIVersion.V1_0,
  ),
  ;

  companion object {
    fun getLatestVersion() = entries.last()
  }
}

enum class OGRS3Version {
  V3_0,
}

enum class OGPVersion {
  V1_0,
}

enum class OVPVersion {
  V1_0,
}

enum class MSTVersion {
  V1_0,
}

enum class PNIVersion {
  V1_0,
}
