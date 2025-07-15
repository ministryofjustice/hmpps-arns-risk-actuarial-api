package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.config

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OffenseGroupParametersConfig(@Value("\${hmpps.arnsriskactuarial.offensegroupparameters.csv}") val resource: String) {

  @Bean
  fun offenseGroupParameters(): Map<String, OffenceGroupParameters> {
    val bufferedReader = this::class.java.getResourceAsStream(resource)!!.bufferedReader()
    val csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT)
      .setHeader()
      .setSkipHeaderRecord(true)
      .setTrim(true)
      .build()
    val csvParser = CSVParser(bufferedReader, csvFormat)
    return csvParser.associate { csvRecord ->
      "${csvRecord.get(0)}${csvRecord.get(1)}" to
        OffenceGroupParameters(
          ogrs3Weighting = csvRecord.get(2).toDoubleOrNull(),
          rsrOffenceCategory = csvRecord.get(3).toIntOrNull(),
          rsrCategoryDesc = csvRecord.get(4),
          opdViolSex = "Y" == csvRecord.get(5),
        )
    }
  }
}

data class OffenceGroupParameters(
  val ogrs3Weighting: Double?,
  val rsrOffenceCategory: Int?,
  val rsrCategoryDesc: String?,
  val opdViolSex: Boolean,
)
