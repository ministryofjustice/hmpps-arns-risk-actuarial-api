package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.config

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OffenceGroupParametersConfig(
  @param:Value($$"${hmpps.arnsriskactuarial.offencegroupparameters.csv}") val resource: String,
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
  fun offenceGroupParameters(): Map<String, OffenceGroupParameters> {
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
            snsvStaticWeighting = csvRecord.get("SNSV_STATIC_WEIGHTING").toDoubleOrNull(),
            snsvDynamicWeighting = csvRecord.get("SNSV_DYNAMIC_WEIGHTING").toDoubleOrNull(),
            snsvVATPStaticWeighting = csvRecord.get("SNSV_VATP_STATIC_WEIGHTING").toDoubleOrNull(),
            snsvVATPDynamicWeighting = csvRecord.get("SNSV_VATP_DYNAMIC_WEIGHTING").toDoubleOrNull(),

          )
      }
    }
  }
}

data class OffenceGroupParameters(
  val ogrs3Weighting: Double?,
  val opdViolSex: Boolean,
  val snsvStaticWeighting: Double?,
  val snsvDynamicWeighting: Double?,
  val snsvVATPStaticWeighting: Double?,
  val snsvVATPDynamicWeighting: Double?,
)
