package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.regression

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.RiskScoreService
import java.io.File
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch

@ExperimentalAtomicApi
@SpringBootTest
@ActiveProfiles("test")
class RSRRegressionTest(
  @param:Autowired private val riskScoreService: RiskScoreService,
  @param:Autowired private val objectMapper: ObjectMapper,
  @param:Autowired @param:Value("\${regression-test.output-path}") private val outputPath: String,
  @param:Autowired @param:Value("\${regression-test.number-of-test-cases}") private val numberOfTestCases: Int,
  @param:Autowired @param:Value("\${regression-test.test-seed}") private val testSeed: Long?,
  @param:Autowired @param:Value("\${regression-test.batch-size}") private val batchSize: Int,
  @param:Autowired @param:Value("\${regression-test.save-only-failures}") private val saveOnlyFailures: Boolean,
  @param:Autowired @param:Value("\${regression-test.oracle.username}") private val oracleUsername: String,
  @param:Autowired @param:Value("\${regression-test.oracle.password}") private val oraclePassword: String,
  @param:Autowired @param:Value("\${regression-test.oracle.connection-string}") private val oracleConnectionString: String,
) {

  @Test
  @Disabled("Cannot currently run on the build server")
  fun `run RSR static test cases`() {
    // Set up directory structure
    val testOutputPath = "$outputPath/${System.currentTimeMillis()}"
    createDirectories(testOutputPath)

    // Set up DB connection
    val connection = getDatabaseConnection(oracleUsername, oraclePassword, oracleConnectionString)

    // Flatten input options into lists of lists
    val inputCombinations = flattenInputOptions(rsrInputFields)

    // Use the provided seed or generate a new one if not provided
    val seed = testSeed ?: System.nanoTime()

    // Iterate through batches of input combinations
    val currentBatchNumber = AtomicLong(0)
    runTestCasesInBatches(inputCombinations, numberOfTestCases, batchSize, seed) { inputBatch ->

      // Map inputs by arns and oasys field names
      val inputMappings = mapInputs(inputBatch)

      // Run batch into ARNS calculator
      val arnsResponses = runBatchIntoRiskScoreService(inputMappings, riskScoreService)

      // Run batch into OASys oracle function
      val oasysResponses = runBatchIntoOASys(inputMappings, connection)

      // Check results from ARNS and OASys are the same size
      Assertions.assertEquals(arnsResponses.size, oasysResponses.size)

      // Compare each result and record in txt file
      arnsResponses.forEachIndexed { i, arnsResponse ->
        val testNum = ((currentBatchNumber.load()) * batchSize) + (i + 1)
        println("## Running test case $testNum of $numberOfTestCases")
        val oasysResponse = oasysResponses[i]

        // Check OASys and ARNS get the same result
        var testFailed = false

        // Check if we are in an error case
        val errorCase = oasysResponse.response.errorMessage != null || arnsResponse.response.actuarialPredictors.seriousPredictor.validationErrors.isNotEmpty()

        if (!errorCase) {
          if (arnsAndOasysResultsNotEqual(arnsResponse.response, oasysResponse.response)) {
            testFailed = true
          }
        } else {
          // Check that errors are reported by both ARNS and OASys
          if (oasysResponse.response.errorMessage == null && arnsResponse.response.actuarialPredictors.seriousPredictor.validationErrors.isNotEmpty()) {
            // Validation isn't as tight on OASys so filter out any known issues
            if (!knownValidationIssue(inputMappings[i])) {
              testFailed = true
            }
          } else if (oasysResponse.response.errorMessage != null && arnsResponse.response.actuarialPredictors.seriousPredictor.validationErrors.isEmpty()) {
            testFailed = true
          }
        }

        // Record results in text file
        if (testFailed || !saveOnlyFailures) {
          val passFailPostfix = if (!testFailed) "pass" else "fail"
          val testType = if (!errorCase) "happy" else "error"
          val output = File("$testOutputPath/$testNum-$testType-$passFailPostfix.txt")
          output.appendText(
            getTestOutputText(
              objectMapper,
              testNum,
              arnsResponse,
              oasysResponse,
              testFailed,
              seed,
            ),
          )
        }
      }

      // Increment batch number for next loop
      currentBatchNumber.incrementAndFetch()
    }
    connection.close()
  }
}
