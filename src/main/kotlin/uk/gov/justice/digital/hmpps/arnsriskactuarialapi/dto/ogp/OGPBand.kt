package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp

enum class OGPBand(val bounds: IntRange) {

  LOW(1..33),
  MEDIUM(34..66),
  HIGH(67..84),
  VERY_HIGH(85..99),
  ;

  companion object {
    fun findBand(value: Int): OGPBand {
      require(value in 1..99) { "Percentage $value should be between 1 and 99" }
      return OGPBand.entries.find { band -> value in band.bounds }
        ?: throw IllegalArgumentException("Unexpected percentage value: $value")
    }
  }
}
