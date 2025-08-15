package uk.gov.justice.digital.hmpps.arnsriskactuarialapi

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// Path to CSV output of CT_OFFENCE
const val CT_OFFENCES_PATH = "ADD ME"

// Path to CSV output of RSR_COEFFICIENTS
const val RSR_COEFFICIENTS_PATH = "ADD ME"

// Do not check in this file. Once happy with output copy contents to replace offence-code-mapping.csv.
@OptIn(ExperimentalTime::class)
val OUTPUT_FILE =
  "src/main/resources/offencegroupparameters/offence-code-mapping-${Clock.System.now().epochSeconds}.csv"

fun main(args: Array<String>) {
  val ctOffenceInputStream = File(CT_OFFENCES_PATH).inputStream()

  val csvFormat = CSVFormat.DEFAULT.builder()
    .setHeader()
    .setSkipHeaderRecord(true)
    .setTrim(true)
    .build()

  val ctOffences = ctOffenceInputStream.bufferedReader().use { reader ->
    val csvParser = CSVParser(reader, csvFormat)
    csvParser.map { csvRecord ->
      CTOffence(
        offenceGroupCode = csvRecord.get("OFFENCE_GROUP_CODE"),
        subCode = csvRecord.get("SUB_CODE"),
        ogrs3Weighting = csvRecord.get("OGRS3_WEIGHTING").toDoubleOrNull(),
        ogrs4CategoryDesc = csvRecord.get("OGRS4_CATEGORY_DESC"),
        rsrCategoryDesc = csvRecord.get("RSR_CATEGORY_DESC"),
        opdViolSex = csvRecord.get("OPD_VIOL_SEX"),
      )
    }
  }

  val rsrCoefficientsInputStream = File(RSR_COEFFICIENTS_PATH).inputStream()
  val rsrCoefficients = rsrCoefficientsInputStream.bufferedReader().use { reader ->
    val csvParser = CSVParser(reader, csvFormat)
    csvParser.map { csvRecord ->
      RSRCoefficient(
        coefficientName = csvRecord.get("COEFFICIENTS_NAME"),
        snsvStatic = csvRecord.get("SNSV_STATIC").toDoubleOrNull(),
        snsvDynamic = csvRecord.get("SNSV_DYNAMIC").toDoubleOrNull(),
      )
    }
  }

  val offenceCodeMapping = ctOffences.map { ctOffence ->
    OffenceCodeMapping(
      offenceGroupCode = StringUtils.leftPad(ctOffence.offenceGroupCode, 3, '0'),
      subCode = StringUtils.leftPad(ctOffence.subCode, 2, '0'),
      ogrs3Weighting = ctOffence.ogrs3Weighting,
      snsvStaticWeighting = rsrCoefficients.firstOrNull { it.coefficientName == ctOffence.ogrs4CategoryDesc }?.snsvStatic,
      snsvDynamicWeighting = rsrCoefficients.firstOrNull { it.coefficientName == ctOffence.ogrs4CategoryDesc }?.snsvDynamic,
      snsvVatpStaticWeighting = lookupVatpWeighting(ctOffence, rsrCoefficients, true),
      snsvVatpDynamicWeighting = lookupVatpWeighting(ctOffence, rsrCoefficients, false),
      opdViolSex = ctOffence.opdViolSex,
    )
  }

  FileOutputStream(OUTPUT_FILE).apply {
    val writer = bufferedWriter()
    val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)
    csvPrinter.printRecord(
      "OFFENCE_GROUP_CODE",
      "SUB_CODE",
      "OGRS3_WEIGHTING",
      "SNSV_STATIC_WEIGHTING",
      "SNSV_DYNAMIC_WEIGHTING",
      "SNSV_VATP_STATIC_WEIGHTING",
      "SNSV_VATP_DYNAMIC_WEIGHTING",
      "OPD_VIOL_SEX",
    )
    offenceCodeMapping.forEach {
      csvPrinter.printRecord(
        it.offenceGroupCode,
        it.subCode,
        formatDouble(it.ogrs3Weighting),
        formatDouble(it.snsvStaticWeighting),
        formatDouble(it.snsvDynamicWeighting),
        formatDouble(it.snsvVatpStaticWeighting),
        formatDouble(it.snsvVatpDynamicWeighting),
        it.opdViolSex,
      )
    }
    writer.flush()
  }
}

internal fun formatDouble(value: Double?) = if (value?.compareTo(value.toInt()) == 0) DecimalFormat("#").format(value) else value?.toString() ?: ""

internal fun lookupVatpWeighting(ctOffence: CTOffence, rsrCoefficients: List<RSRCoefficient>, static: Boolean) = if (ctOffence.ogrs4CategoryDesc.trim() == "") {
  null
} else if (ctOffence.ogrs4CategoryDesc == "Violence against the person") {
  val rsrCategory = rsrCoefficients.firstOrNull { it.coefficientName == ctOffence.rsrCategoryDesc }
  if (static) {
    rsrCategory?.snsvStatic
  } else {
    rsrCategory?.snsvDynamic
  }
} else {
  0.0
}

data class CTOffence(
  val offenceGroupCode: String,
  val subCode: String,
  val ogrs3Weighting: Double?,
  val ogrs4CategoryDesc: String,
  val rsrCategoryDesc: String,
  val opdViolSex: String,
)

data class RSRCoefficient(
  val coefficientName: String,
  val snsvStatic: Double?,
  val snsvDynamic: Double?,
)

data class OffenceCodeMapping(
  val offenceGroupCode: String,
  val subCode: String,
  val ogrs3Weighting: Double?,
  val snsvStaticWeighting: Double?,
  val snsvDynamicWeighting: Double?,
  val snsvVatpStaticWeighting: Double?,
  val snsvVatpDynamicWeighting: Double?,
  val opdViolSex: String,
)
