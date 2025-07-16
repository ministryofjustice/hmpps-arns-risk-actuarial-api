package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp

enum class OGPBand(val bounds: Pair<Double, Double>) {

  LOW(Pair(0.0, 34.0)),
  MEDIUM(Pair(34.0, 67.0)),
  HIGH(Pair(67.0, 85.0)),
  VERY_HIGH(Pair(85.0, 100.0)),
  ;

  companion object {
    fun findBand(value: Double): OGPBand {
      require(0 < value && value < 100) { "Percentage $value should be between 0 and 100" }
      for (band in OGPBand.entries) {
        val (a, b) = band.bounds
        if (a <= value && value < b) {
          return band
        }
      }
      throw IllegalArgumentException("Unexpected percentage value: $value")
    }
  }
}
