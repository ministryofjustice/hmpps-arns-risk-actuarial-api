package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.regression

import oracle.jdbc.OracleConnection
import oracle.jdbc.datasource.impl.OracleDataSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.PreviousConviction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.SupervisionStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.RiskBandResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.RiskScoreResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.ScoreTypeResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.RiskScoreService
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.Properties
import kotlin.random.Random
import kotlin.reflect.full.memberProperties

data class InputField(
  val arnsFieldName: String,
  val oasysFieldName: String,
  val transformFunction: (Any?) -> Any? = convertDefault(),
)

data class InputFieldWithOption(
  val arnsFieldName: String,
  val oasysFieldName: String,
  val arnsOption: Any?,
  val oasysOption: Any?,
)

data class OASysRSROutput(
  val scoreType: ScoreTypeResponse?,
  val rsrBand: RiskBandResponse?,
  val rsrScore: Double?,
  val ospdcBand: RiskBandResponse?,
  val ospdcScore: Double?,
  val ospiicBand: RiskBandResponse?,
  val ospiicScore: Double?,
  val ospRiskReduction: Boolean?,
  val snsvScore: Double?,
  val errorMessage: String?,
)

data class InputMapping(
  val arnsData: Map<String, Any?>,
  val oasysData: Map<String, Any?>,
)

data class ARNSRequestAndResponse(
  val request: RiskScoreRequest,
  val response: RiskScoreResponse,
)

data class OASysRequestAndResponse(
  val request: Map<String, Any?>,
  val response: OASysRSROutput,
)

fun convertDefault() = { input: Any? ->
  when (input) {
    null -> "NULL"
    is Int -> "'$input'"
    is String -> if (isDateString(input)) "to_date('$input', 'YYYY-MM-DD')" else "'$input'"
    else -> throw IllegalArgumentException("Value must be a string or null")
  }
}

fun convertGender() = { input: Any? ->
  when (input) {
    "MALE" -> "'M'"
    "FEMALE" -> "'F'"
    null -> "NULL"
    else -> throw IllegalArgumentException("Gender must be MALE, FEMALE or null")
  }
}

fun convertSupervisionStatus() = { input: Any? ->
  when (input) {
    "CUSTODY" -> "'Y'"
    "REMAND", "COMMUNITY" -> "'N'"
    null -> "NULL"
    else -> throw IllegalArgumentException("Supervision status must be CUSTODY, REMAND, COMMUNITY or null")
  }
}

fun convertBoolean() = { input: Any? ->
  when (input) {
    true -> "'Y'"
    false -> "'N'"
    null -> "NULL"
    else -> throw IllegalArgumentException("Boolean must be true, false or null")
  }
}

fun convertBooleanWithOmission() = { input: Any? ->
  when (input) {
    true -> "'Y'"
    false -> "'N'"
    null -> "'O'"
    else -> throw IllegalArgumentException("Boolean must be true, false or null")
  }
}

fun convertBooleanToInt() = { input: Any? ->
  when (input) {
    true -> "1"
    false -> "0"
    null -> "NULL"
    else -> throw IllegalArgumentException("Boolean must be true, false or null")
  }
}

fun convertProblemLevelToInt() = { input: Any? ->
  when (input) {
    "NO_PROBLEMS" -> "0"
    "SOME_PROBLEMS" -> "1"
    "SIGNIFICANT_PROBLEMS" -> "2"
    null -> "NULL"
    else -> throw IllegalArgumentException("Problem level must be NO_PROBLEMS, SOME_PROBLEMS, SIGNIFICANT_PROBLEMS or null")
  }
}

fun convertDomesticAbuseBooleanListToInt() = { input: Any? ->
  when (input) {
    listOf(true, true) -> "1"
    listOf(true, false) -> "1"
    listOf(false, true) -> "0"
    listOf(false, false) -> "0"
    null -> "NULL"
    else -> throw IllegalArgumentException("Domestic abuse boolean list must be a list of two booleans or null")
  }
}

val defaultOASysInputFields: Map<String, Any?> = mapOf(
  "ALGORITHM_VERSION" to 5,
)

fun getOASysInputMappingString(input: Map<String, Any?>): String {
  val defaultInputs = defaultOASysInputFields.map {
    "${it.key} => ${it.value}"
  }
  val dynamicInputs = input.map {
    "${it.key} => i.${it.key}"
  }
  return (defaultInputs + dynamicInputs).joinToString(", ")
}

fun isDateString(string: String): Boolean {
  try {
    LocalDate.parse(string)
  } catch (_: DateTimeParseException) {
    return false
  }
  return true
}

fun String.convertOASysBand() = when (this) {
  "Not Applicable" -> RiskBandResponse.NOT_APPLICABLE
  "Low" -> RiskBandResponse.LOW
  "Medium" -> RiskBandResponse.MEDIUM
  "High" -> RiskBandResponse.HIGH
  "Very High" -> RiskBandResponse.VERY_HIGH
  else -> throw IllegalArgumentException("Unexpected OASys band $this")
}

fun String.convertOASysBoolean() = when (this) {
  "Y" -> true
  "N" -> false
  else -> throw IllegalArgumentException("Unexpected OASys boolean $this")
}

fun String.convertOASysScoreType() = when (this) {
  "Static" -> ScoreTypeResponse.STATIC
  "Dynamic" -> ScoreTypeResponse.DYNAMIC
  else -> throw IllegalArgumentException("Unexpected OASys score type $this")
}

fun createDirectories(directory: String) {
  Files.createDirectories(Paths.get(directory))
}

fun getDatabaseConnection(username: String, password: String, connectionString: String): Connection {
  val connectionProperties = Properties()
  connectionProperties.setProperty(OracleConnection.CONNECTION_PROPERTY_USER_NAME, username)
  connectionProperties.setProperty(OracleConnection.CONNECTION_PROPERTY_PASSWORD, password)

  val oracleDataSource = OracleDataSource()
  oracleDataSource.connectionProperties = connectionProperties
  oracleDataSource.url = connectionString

  return oracleDataSource.connection
}

fun flattenInputOptions(input: Map<InputField, List<Any?>>) = input.toMutableMap().entries.map { i ->
  i.value.map { j ->
    InputFieldWithOption(
      arnsFieldName = i.key.arnsFieldName,
      oasysFieldName = i.key.oasysFieldName,
      arnsOption = j,
      oasysOption = i.key.transformFunction.invoke(j),
    )
  }
}

fun <T> findNumberOfCombinations(inputCombinations: List<List<T>>) = inputCombinations.fold(1L) { acc, list ->
  acc * list.size
}

fun <T> runTestCasesInBatches(
  inputs: List<List<T>>,
  totalCount: Int,
  batchSize: Int,
  seed: Long = System.nanoTime(),
  testExecutor: (List<List<T>>) -> Unit,
) {
  require(inputs.isNotEmpty() && inputs.all { it.isNotEmpty() }) {
    "The input list of lists must not be empty, and all inner lists must contain at least one element."
  }
  require(batchSize > 0) { "Batch size must be greater than zero." }
  require(totalCount >= 0) { "Total count must be non-negative." }

  val random = Random(seed)
  var currentBatch = mutableListOf<List<T>>()
  var casesRun = 0
  var batchCount = 0

  println("Starting test case generation and batch execution...")

  // Loop until the total desired count is reached
  while (casesRun < totalCount) {
    // Generate a single test case
    val testCase = generateSingleTestCase(inputs, random)
    currentBatch.add(testCase)
    casesRun++

    // If the batch is full, execute it
    if (currentBatch.size >= batchSize) {
      batchCount++
      println("Executing batch #$batchCount (${currentBatch.size} cases)...")
      testExecutor(currentBatch)
      // Start a new batch
      currentBatch = mutableListOf()
    }
  }

  // Run any remaining partial batch
  if (currentBatch.isNotEmpty()) {
    batchCount++
    println("Executing final partial batch #$batchCount (${currentBatch.size} cases)...")
    testExecutor(currentBatch)
  }

  println("Finished. Total test cases executed: $casesRun across $batchCount batches.")
}

private fun <T> generateSingleTestCase(inputs: List<List<T>>, random: Random): List<T> = inputs.map { inputList ->
  inputList[random.nextInt(inputList.size)]
}

fun mapInputs(inputBatch: List<List<InputFieldWithOption>>) = inputBatch.map { i ->
  InputMapping(
    arnsData = i.associate {
      it.arnsFieldName to it.arnsOption
    },
    oasysData = i.associate {
      it.oasysFieldName to it.oasysOption
    },
  )
}

fun runBatchIntoRiskScoreService(inputMappings: List<InputMapping>, riskScoreService: RiskScoreService) = inputMappings.map { inputMapping ->
  val inputs = inputMapping.arnsData
  val request = RiskScoreRequest(
    gender = inputs["gender"]?.let { Gender.valueOf(it as String) },
    assessmentDate = inputs["assessmentDate"].let { LocalDate.parse(it as String) },
    dateOfBirth = inputs["dateOfBirth"]?.let { LocalDate.parse(it as String) },
    dateOfCurrentConviction = inputs["dateOfCurrentConviction"]?.let { LocalDate.parse(it as String) },
    currentOffenceCode = inputs["currentOffenceCode"] as String?,
    totalNumberOfSanctionsForAllOffences = inputs["totalNumberOfSanctionsForAllOffences"] as Integer?,
    ageAtFirstSanction = inputs["ageAtFirstSanction"] as Integer?,
    supervisionStatus = inputs["supervisionStatus"]?.let { SupervisionStatus.valueOf(it as String) },
    dateAtStartOfFollowupUserInput = inputs["dateAtStartOfFollowupUserInput"]?.let { LocalDate.parse(it as String) },
    totalNumberOfViolentSanctions = inputs["totalNumberOfViolentSanctions"] as Integer?,
    hasEverCommittedSexualOffence = inputs["hasEverCommittedSexualOffence"] as Boolean?,
    totalContactAdultSexualSanctions = inputs["totalContactAdultSexualSanctions"] as Int?,
    totalContactChildSexualSanctions = inputs["totalContactChildSexualSanctions"] as Int?,
    totalIndecentImageSanctions = inputs["totalIndecentImageSanctions"] as Int?,
    totalNonContactSexualOffences = inputs["totalNonContactSexualOffences"] as Int?,
    dateOfMostRecentSexualOffence = inputs["dateOfMostRecentSexualOffence"]?.let { LocalDate.parse(it as String) },
    didOffenceInvolveCarryingOrUsingWeapon = inputs["didOffenceInvolveCarryingOrUsingWeapon"] as Boolean?,
    suitabilityOfAccommodation = inputs["suitabilityOfAccommodation"]?.let { ProblemLevel.valueOf(it as String) },
    isUnemployed = inputs["isUnemployed"] as Boolean?,
    currentRelationshipWithPartner = inputs["currentRelationshipWithPartner"]?.let { ProblemLevel.valueOf(it as String) },
    evidenceOfDomesticAbuse = (inputs["evidenceOfDomesticAbuse/domesticAbuseAgainstPartner"] as List<*>)[0] as Boolean?,
    domesticAbuseAgainstPartner = (inputs["evidenceOfDomesticAbuse/domesticAbuseAgainstPartner"] as List<*>)[1] as Boolean?,
    currentAlcoholUseProblems = inputs["currentAlcoholUseProblems"]?.let { ProblemLevel.valueOf(it as String) },
    excessiveAlcoholUse = inputs["excessiveAlcoholUse"]?.let { ProblemLevel.valueOf(it as String) },
    impulsivityProblems = inputs["impulsivityProblems"]?.let { ProblemLevel.valueOf(it as String) },
    temperControl = inputs["temperControl"]?.let { ProblemLevel.valueOf(it as String) },
    proCriminalAttitudes = inputs["proCriminalAttitudes"]?.let { ProblemLevel.valueOf(it as String) },
    previousConvictions = getPreviousConvictions(inputs),
    isCurrentOffenceSexuallyMotivated = inputs["isCurrentOffenceSexuallyMotivated"] as Boolean?,
    isCurrentOffenceAgainstVictimStranger = inputs["isCurrentOffenceAgainstVictimStranger"] as Boolean?,
    mostRecentOffenceDate = inputs["mostRecentOffenceDate"]?.let { LocalDate.parse(it as String) },
  )
  val response = riskScoreService.riskScoreProducer(request)
  ARNSRequestAndResponse(request, response)
}

fun getPreviousConvictions(inputs: Map<String, Any?>) = inputs.mapNotNull {
  if (it.key.contains("previousConvictions") && it.value == true) {
    PreviousConviction.valueOf(it.key.split("/")[1])
  } else {
    null
  }
}

fun runBatchIntoOASys(inputMappings: List<InputMapping>, connection: Connection): List<OASysRequestAndResponse> {
  val oasysInputSQLStatements = inputMappings.map { i ->
    val inputs = i.oasysData.map {
      "${it.value} as ${it.key}"
    }
    "select ${inputs.joinToString(", ")} from DUAL"
  }

  val oasysSqlStatement = """
        with inputs as (
          ${oasysInputSQLStatements.joinToString(" UNION ALL ")}
        ),
        outputs(results_object) as (
        select
          EOR.RSR_PKG.GET_RSR(${getOASysInputMappingString(inputMappings[0].oasysData)})
        from inputs i
        )
        select
          o.results_object.RSR_Score as RSR_Score,
          o.results_object.RSR_Band as RSR_Band,
          o.results_object.Score_Type as Score_Type,
          o.results_object.OSPC_Score as OSPC_Score,
          o.results_object.OSPC_Band as OSPC_Band,
          o.results_object.OSPI_Score as OSPI_Score,
          o.results_object.OSPI_Band as OSPI_Band,
          o.results_object.SNSV_SCORE as SNSV_SCORE,
          o.results_object.Error_Message as Error_Message,
          o.results_object.OSP_RISK_REDUCTION as OSP_RISK_REDUCTION
        from outputs o
  """.trimIndent()

  val statement = connection.createStatement()
  val oasysResponse = statement.executeQuery(oasysSqlStatement)

  val oasysResponseObjects: MutableList<OASysRequestAndResponse> = mutableListOf()
  while (oasysResponse.next()) {
    oasysResponseObjects.add(
      OASysRequestAndResponse(
        request = inputMappings[oasysResponse.row - 1].oasysData,
        response = OASysRSROutput(
          scoreType = oasysResponse.getString("Score_Type")?.convertOASysScoreType(),
          rsrBand = oasysResponse.getString("RSR_Band")?.convertOASysBand(),
          rsrScore = oasysResponse.getString("RSR_Score")?.toDoubleOrNull(),
          ospdcBand = oasysResponse.getString("OSPC_Band")?.convertOASysBand(),
          ospdcScore = oasysResponse.getString("OSPC_Score")?.toDoubleOrNull(),
          ospiicBand = oasysResponse.getString("OSPI_Band")?.convertOASysBand(),
          ospiicScore = oasysResponse.getString("OSPI_Score")?.toDoubleOrNull(),
          ospRiskReduction = oasysResponse.getString("OSP_RISK_REDUCTION")?.convertOASysBoolean(),
          snsvScore = oasysResponse.getString("SNSV_SCORE")?.toDoubleOrNull(),
          errorMessage = oasysResponse.getString("Error_Message"),
        ),
      ),
    )
  }
  statement.close()

  return oasysResponseObjects
}

fun arnsAndOasysResultsNotEqual(arnsResponse: RiskScoreResponse, oasysResponse: OASysRSROutput) = // Check RSR score and band
  arnsResponse.actuarialPredictors.seriousPredictor.output.band != oasysResponse.rsrBand ||
    arnsResponse.actuarialPredictors.seriousPredictor.output.overallScore != oasysResponse.rsrScore ||
    // Check OSP/DC score and band
    arnsResponse.actuarialPredictors.directContactSexualPredictor.output.band != oasysResponse.ospdcBand ||
    arnsResponse.actuarialPredictors.directContactSexualPredictor.output.score != oasysResponse.ospdcScore ||
    arnsResponse.actuarialPredictors.directContactSexualPredictor.output.riskBandReductionApplied != oasysResponse.ospRiskReduction ||
    // Check OSP/IIC score and band
    arnsResponse.actuarialPredictors.indirectContactSexualPredictor.output.band != oasysResponse.ospiicBand ||
    arnsResponse.actuarialPredictors.indirectContactSexualPredictor.output.score != oasysResponse.ospiicScore ||
    // Check SNSV score
    arnsResponse.actuarialPredictors.seriousViolencePredictor.output.score != oasysResponse.snsvScore

fun getTestOutputText(testNum: Long, arnsResponse: ARNSRequestAndResponse, oasysResponse: OASysRequestAndResponse, testFailed: Boolean) =
  """
    # Test $testNum
    
    # Input
    # ARNS input
    ${arnsResponse.request.printNonNullProperties()}
    
    # OASys input
    ${oasysResponse.request}
    
    # Output
    ARNS score type  = ${arnsResponse.response.actuarialPredictors.seriousViolencePredictor.type}
    OASys score type = ${oasysResponse.response.scoreType}
    
    ARNS RSR Band  = ${arnsResponse.response.actuarialPredictors.seriousPredictor.output.band}
    OASys RSR Band = ${oasysResponse.response.rsrBand}
    
    ARNS RSR Score  = ${arnsResponse.response.actuarialPredictors.seriousPredictor.output.overallScore}
    OASys RSR Score = ${oasysResponse.response.rsrScore}
    
    ARNS OSP/DC Band  = ${arnsResponse.response.actuarialPredictors.directContactSexualPredictor.output.band}
    OASys OSP/DC Band = ${oasysResponse.response.ospdcBand}
    
    ARNS OSP/DC Score  = ${arnsResponse.response.actuarialPredictors.directContactSexualPredictor.output.score}
    OASys OSP/DC Score = ${oasysResponse.response.ospdcScore}
    
    ARNS OSP risk reduction  = ${arnsResponse.response.actuarialPredictors.directContactSexualPredictor.output.riskBandReductionApplied}
    OASys OSP risk reduction = ${oasysResponse.response.ospRiskReduction}
    
    ARNS OSP/IIC Band  = ${arnsResponse.response.actuarialPredictors.indirectContactSexualPredictor.output.band}
    OASys OSP/IIC Band = ${oasysResponse.response.ospiicBand}
    
    ARNS OSP/IIC Score  = ${arnsResponse.response.actuarialPredictors.indirectContactSexualPredictor.output.score}
    OASys OSP/IIC Score = ${oasysResponse.response.ospiicScore}
    
    ARNS SNSV Score  = ${arnsResponse.response.actuarialPredictors.seriousViolencePredictor.output.score}
    OASys SNSV Score = ${oasysResponse.response.snsvScore}
    
    ARNS RSR errors     = ${arnsResponse.response.actuarialPredictors.seriousPredictor.validationErrors}
    ARNS OSP/DC errors  = ${arnsResponse.response.actuarialPredictors.directContactSexualPredictor.validationErrors}
    ARNS OSP/IIC errors = ${arnsResponse.response.actuarialPredictors.indirectContactSexualPredictor.validationErrors}
    ARNS SNSV errors    = ${arnsResponse.response.actuarialPredictors.seriousViolencePredictor.validationErrors}
    OASys errors        = ${oasysResponse.response.errorMessage}
    
    ${if (!testFailed) "# TEST PASSED" else "# TEST FAILED"}
  """.trimIndent()

fun knownValidationIssue(inputMapping: InputMapping): Boolean {
  // Issue where hasEverCommittedSexualOffence=false and sanctions count/dates are set to non-null values
  // OASys allows this and ignores the counts/date even though the user has specified they have a sexual offence history
  val hasEverCommitedSexualOffence = inputMapping.arnsData["hasEverCommittedSexualOffence"] as Boolean?
  val totalContactAdultSexualSanctions = inputMapping.arnsData["totalContactAdultSexualSanctions"] as Integer?
  val totalContactChildSexualSanctions = inputMapping.arnsData["totalContactChildSexualSanctions"] as Integer?
  val totalIndecentImageSanctions = inputMapping.arnsData["totalIndecentImageSanctions"] as Integer?
  val totalNonContactSexualOffences = inputMapping.arnsData["totalNonContactSexualOffences"] as Integer?
  val dateOfMostRecentSexualOffence = inputMapping.arnsData["dateOfMostRecentSexualOffence"] as String?

  return hasEverCommitedSexualOffence == false && (totalContactAdultSexualSanctions != null || totalContactChildSexualSanctions != null || totalIndecentImageSanctions != null || totalNonContactSexualOffences != null || dateOfMostRecentSexualOffence != null)
}

fun Any.printNonNullProperties(): String {
  val properties = this::class.memberProperties
  val nonNullFields = properties.mapNotNull { prop ->
    val value = try {
      prop.getter.call(this)
    } catch (_: Exception) {
      null
    }

    if (value != null) {
      "${prop.name}=$value"
    } else {
      null
    }
  }

  return "${this::class.simpleName}(${nonNullFields.joinToString(", ")})"
}
