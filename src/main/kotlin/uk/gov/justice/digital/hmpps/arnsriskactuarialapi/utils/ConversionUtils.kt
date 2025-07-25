package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils

class ConversionUtils {
  companion object {
    // Boolean to Int Score transformers
    fun Boolean.booleanToScore(): Int = if (this) 2 else 0
  }
}
