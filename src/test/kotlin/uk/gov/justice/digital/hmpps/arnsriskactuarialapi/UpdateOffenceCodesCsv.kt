package uk.gov.justice.digital.hmpps.arnsriskactuarialapi

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.time.LocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// Path to CSV output of CT_OFFENCE
const val CT_OFFENCES_PATH = "ADD ME"

// Path to CSV output of RSR_COEFFICIENTS
const val RSR_COEFFICIENTS_PATH = "ADD ME"

// Path to CSV output of ogrs_description_weight_map
const val OGRS_DESCRIPTION_PATH = "ADD ME"

// Path to CSV output of RSR_COEFFICIENTS_extended
const val RSR_COEFFICIENTS_UPDATE_PATH = "ADD ME"

// Do not check in this file. Once happy with output copy contents to replace offence-code-mapping.csv.
@OptIn(ExperimentalTime::class)
val OUTPUT_FILE =
  "src/main/resources/offencegroupparameters/offence-code-mapping-${Clock.System.now().epochSeconds}.csv"
val FLYWAY_OUTPUT_FILE = File("src/main/resources/offencegroupparameters/V66__seed_risk_actuarial.sql")
val FLYWAY_OUTPUT_UPDATE_FILE = File("src/main/resources/offencegroupparameters/V67__risk_actuarial_weighting_update.sql")

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

  val ogrs3DescriptionInputStream = File(OGRS_DESCRIPTION_PATH).inputStream()
  val ogrs3Descriptions = ogrs3DescriptionInputStream.bufferedReader().use { reader ->
    val cleanedReader = reader.readText().replace("\uFEFF", "").reader()
    val csvParser = CSVParser(cleanedReader, csvFormat)
    csvParser.map { csvRecord ->
      OgrsDescriptionWeightMap(
        ogrs3description = csvRecord.get("Offence"),
        ogrs3Weighting = csvRecord.get("Parameter").toDoubleOrNull(),
      )
    }
  }

  val rsrCoefficientsUpdateInputStream = File(RSR_COEFFICIENTS_UPDATE_PATH).inputStream()
  val rsrUpdateCoefficients = rsrCoefficientsUpdateInputStream.bufferedReader().use { reader ->
    val csvParser = CSVParser(reader, csvFormat)
    csvParser.map { csvRecord ->
      RSRCoefficient(
        coefficientName = csvRecord.get("COEFFICIENTS_NAME"),
        snsvStatic = csvRecord.get("SNSV_STATIC").toDoubleOrNull(),
        snsvDynamic = csvRecord.get("SNSV_DYNAMIC").toDoubleOrNull(),
      )
    }
  }

  val offenceCodeMapping = ctOffences.filterNot {
    filterCategory000(it.offenceGroupCode, it.subCode)
  }.map { ctOffence ->
    val ogrs3Description = ogrs3Descriptions.firstOrNull {
      it.ogrs3Weighting == ctOffence.ogrs3Weighting
    }?.ogrs3description ?: "Missing description"
    val ogrs4CategoryDescription = if (ctOffence.ogrs4CategoryDesc != "") ctOffence.ogrs4CategoryDesc else "Missing description"
    val rsrDescription = if (ctOffence.rsrCategoryDesc != "") ctOffence.rsrCategoryDesc else "Missing description"

    OffenceCodeMapping(
      offenceGroupCode = StringUtils.leftPad(ctOffence.offenceGroupCode, 3, '0'),
      subCode = StringUtils.leftPad(ctOffence.subCode, 2, '0'),
      ogrs3Weighting = ctOffence.ogrs3Weighting,
      ogrs3Description = ogrs3Description,
      snsvStaticWeighting = rsrCoefficients.firstOrNull { it.coefficientName == ctOffence.ogrs4CategoryDesc }?.snsvStatic,
      snsvStaticDescription = ogrs4CategoryDescription,
      snsvDynamicWeighting = rsrCoefficients.firstOrNull { it.coefficientName == ctOffence.ogrs4CategoryDesc }?.snsvDynamic,
      snsvDynamicDescription = ogrs4CategoryDescription,
      snsvVatpStaticWeighting = lookupVatpWeighting(ctOffence, rsrCoefficients, true),
      snsvVatpStaticDescription = rsrDescription,
      snsvVatpDynamicWeighting = lookupVatpWeighting(ctOffence, rsrCoefficients, false),
      snsvVatpDynamicDescription = rsrDescription,
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

  val sqlSeedFileContent = offenceCodeMapping.map { mapping ->
    val createdDate = LocalDateTime.now()
    val flagValue = when (mapping.opdViolSex) {
      "Y" -> "TRUE"
      "N" -> "FALSE"
      else -> "NULL"
    }

    val weightings = listOf(
      "ogrs3Weighting" to (mapping.ogrs3Weighting to mapping.ogrs3Description),
      "snsvStaticWeighting" to (mapping.snsvStaticWeighting to mapping.snsvStaticDescription),
      "snsvDynamicWeighting" to (mapping.snsvDynamicWeighting to mapping.snsvDynamicDescription),
      "snsvVatpStaticWeighting" to (mapping.snsvVatpStaticWeighting to mapping.snsvVatpStaticDescription),
      "snsvVatpDynamicWeighting" to (mapping.snsvVatpDynamicWeighting to mapping.snsvVatpDynamicDescription),
    )

    val weightingsUnionAll = weightings.joinToString("\nUNION ALL\n") { (name, pair) ->
      val (value, desc) = pair
      val descSql = desc?.replace("'", "''")?.let { "'$it'" } ?: "NULL"
      val is999 = value == 999.0
      val errorCode = if (name == "ogrs3Weighting" && is999) "'NEED_DETAILS_OF_EXACT_OFFENCE'" else "NULL"
      val valueSql = handle999Value(value)
      "SELECT id, '$name', $valueSql, $descSql, $errorCode FROM inserted"
    }

    """
    WITH inserted AS (
        INSERT INTO risk_actuarial_ho_code (category, sub_category, created_date)
        VALUES ('${mapping.offenceGroupCode}', '${mapping.subCode}', '$createdDate')
        RETURNING id
    )
    INSERT INTO risk_actuarial_ho_code_weightings
    (risk_actuarial_ho_code_id, weighting_name, weighting_value, weighting_desc, error_code)
    $weightingsUnionAll;

    WITH inserted AS (
        SELECT id FROM risk_actuarial_ho_code
        WHERE category = '${mapping.offenceGroupCode}' AND sub_category = '${mapping.subCode}'
    )
    INSERT INTO risk_actuarial_ho_code_flags
    (risk_actuarial_ho_code_id, flag_name, flag_value, created_date)
    SELECT id, 'opdViolSex', $flagValue, '$createdDate' FROM inserted;
    """.trimIndent()
  }

  FLYWAY_OUTPUT_FILE.writeText(sqlSeedFileContent.joinToString("\n\n"))

  val offenceCodeMappingUpdate = ctOffences.filterNot {
    filterCategory000(it.offenceGroupCode, it.subCode)
  }.map { ctOffence ->
    WeightingMapping(
      offenceGroupCode = StringUtils.leftPad(ctOffence.offenceGroupCode, 3, '0'),
      subCode = StringUtils.leftPad(ctOffence.subCode, 2, '0'),
      ogrs3Weighting = ctOffence.ogrs3Weighting,
      snsvStaticWeighting = rsrUpdateCoefficients.firstOrNull { it.coefficientName == ctOffence.ogrs4CategoryDesc }?.snsvStatic,
      snsvDynamicWeighting = rsrUpdateCoefficients.firstOrNull { it.coefficientName == ctOffence.ogrs4CategoryDesc }?.snsvDynamic,
      snsvVatpStaticWeighting = lookupVatpWeighting(ctOffence, rsrUpdateCoefficients, true),
      snsvVatpDynamicWeighting = lookupVatpWeighting(ctOffence, rsrUpdateCoefficients, false),
    )
  }

  val weightingUpdateSql = StringBuilder()
  weightingUpdateSql.append("BEGIN;\n\n")

  offenceCodeMappingUpdate.forEach { mapping ->
    val valueSql = handle999Value(mapping.ogrs3Weighting)
    val weightings = listOf(
      WeightingUpdate(mapping.offenceGroupCode, mapping.subCode, "ogrs3Weighting", valueSql),
      WeightingUpdate(mapping.offenceGroupCode, mapping.subCode, "snsvStaticWeighting", mapping.snsvStaticWeighting),
      WeightingUpdate(mapping.offenceGroupCode, mapping.subCode, "snsvDynamicWeighting", mapping.snsvDynamicWeighting),
      WeightingUpdate(mapping.offenceGroupCode, mapping.subCode, "snsvVatpStaticWeighting", mapping.snsvVatpStaticWeighting),
      WeightingUpdate(mapping.offenceGroupCode, mapping.subCode, "snsvVatpDynamicWeighting", mapping.snsvVatpDynamicWeighting),
    )

    weightings.forEach { weighting ->
      weightingUpdateSql.append(
        """
      UPDATE risk_actuarial_ho_code_weightings AS w
      SET
          weighting_value = ${weighting.value}
      FROM risk_actuarial_ho_code AS r
      WHERE w.risk_actuarial_ho_code_id = r.id
        AND r.category = '${escape(weighting.category)}'
        AND r.sub_category = '${escape(weighting.subCategory)}'
        AND w.weighting_name = '${escape(weighting.name)}';

        """.trimIndent(),
      ).append("\n")
    }
  }

  weightingUpdateSql.append("COMMIT;\n")

  FLYWAY_OUTPUT_UPDATE_FILE.writeText(weightingUpdateSql.toString())
}

private fun escape(value: String): String = value.replace("'", "''")

private fun handle999Value(weighting: Double?): Double? {
  val is999 = weighting == 999.0
  return when {
    weighting == null -> null
    is999 -> null
    else -> weighting
  }
}

private fun filterCategory000(offenceGroupCode: String?, subCode: String?): Boolean {
  val groupCode = StringUtils.leftPad(offenceGroupCode, 3, '0')
  val sub = StringUtils.leftPad(subCode, 2, '0')
  return groupCode == "000" && (sub == "00" || sub == "01")
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

data class WeightingMapping(
  val offenceGroupCode: String,
  val subCode: String,
  val ogrs3Weighting: Double?,
  val snsvStaticWeighting: Double?,
  val snsvDynamicWeighting: Double?,
  val snsvVatpStaticWeighting: Double?,
  val snsvVatpDynamicWeighting: Double?,
)

data class WeightingUpdate(
  val category: String,
  val subCategory: String,
  val name: String,
  val value: Double?,
)

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

data class OgrsDescriptionWeightMap(
  val ogrs3description: String?,
  val ogrs3Weighting: Double?,
)

data class OffenceCodeMapping(
  val offenceGroupCode: String,
  val subCode: String,
  val ogrs3Weighting: Double?,
  val ogrs3Description: String?,
  val snsvStaticWeighting: Double?,
  val snsvStaticDescription: String?,
  val snsvDynamicWeighting: Double?,
  val snsvDynamicDescription: String?,
  val snsvVatpStaticWeighting: Double?,
  val snsvVatpStaticDescription: String?,
  val snsvVatpDynamicWeighting: Double?,
  val snsvVatpDynamicDescription: String?,
  val opdViolSex: String,
)
