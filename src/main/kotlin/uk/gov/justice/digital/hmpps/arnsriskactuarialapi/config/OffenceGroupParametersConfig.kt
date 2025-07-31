package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.config

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OffenseGroupParametersConfig(
  @param:Value($$"${hmpps.arnsriskactuarial.offensegroupparameters.csv}") val resource: String,
) {

  companion object {
    fun String.toStrictlyBooleanAt(recordNumber: Long): Boolean = when (this) {
      "Y" -> true
      "N" -> false
      else -> {
        throw IllegalArgumentException(
          "Error in parsing value: '$this' on line $recordNumber (Only 'Y' or 'N' Allowed)",
        )
      }
    }
  }

  @Bean
  fun offenseGroupParameters(): Map<String, OffenceGroupParameters> {
    val inputStream = this::class.java.getResourceAsStream(resource)
      ?: throw IllegalArgumentException("CSV file not found at path: $resource")
    inputStream.bufferedReader().use { reader ->
      val csvFormat = CSVFormat.DEFAULT.builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .setTrim(true)
        .build()
      val csvParser = CSVParser(reader, csvFormat)
      return csvParser.associate { csvRecord ->
        "${csvRecord.get("OFFENCE_GROUP_CODE")}${csvRecord.get("SUB_CODE")}" to
          OffenceGroupParameters(
            ogrs3Weighting = csvRecord.get("OGRS3_WEIGHTING").toDoubleOrNull(),
            opdViolSex = csvRecord.get("OPD_VIOL_SEX").toStrictlyBooleanAt(csvRecord.recordNumber),
          )
      }
    }
  }
}

data class OffenceGroupParameters(
  val ogrs3Weighting: Double?,
  val opdViolSex: Boolean,
)
