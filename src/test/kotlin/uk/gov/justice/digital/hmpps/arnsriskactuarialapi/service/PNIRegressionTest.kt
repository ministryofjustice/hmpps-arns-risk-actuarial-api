package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CustodyOrCommunity
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.ProgrammeNeedIdentifier
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyOGRS3
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyOVP
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyRSR
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

private const val TEST_CSV_FILE = "/data/PNI/PNI_test_data.csv"
private const val WRITE_FAILED_OUTPUTS = true

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PNIRegressionTest {

  private val service = PNIRiskProducerService()
  private val failedRows = ArrayList<String>()

  val csvHeader: String by lazy {
    // Read the header from the CSV resource
    val path = Paths.get(javaClass.getResource(TEST_CSV_FILE)!!.toURI())
    Files.newBufferedReader(path).readLine()
  }

  @AfterAll
  fun writeFailedRowsToFile() {
    if (failedRows.isEmpty() || WRITE_FAILED_OUTPUTS == false) return

    val outputFile = File("./failed_rows_output.csv")
    outputFile.parentFile.mkdirs()

    outputFile.bufferedWriter().use { writer ->
      writer.write(csvHeader)
      writer.newLine()
      for (row in failedRows) {
        writer.write(row)
        writer.newLine()
      }
    }
  }

  @ParameterizedTest(name = "[{index}] {arguments}")
  @CsvFileSource(
    resources = [TEST_CSV_FILE],
    useHeadersInDisplayName = true,
    ignoreLeadingAndTrailingWhitespace = true,
    encoding = "UTF8",
  )
  fun `requests from CSV file`(
    @Suppress("UNUSEDUPARAMETER")
    id: String?,
    @Suppress("UNUSEDUPARAMETER")
    scenario: String?,
    expectedOutcome: String?,
    community: String?,
    s1U30: String?,
    s6U11: String?,
    s11U11: String?,
    s11U12: String?,
    s6U12: String?,
    s12U1: String?,
    s12U9: String?,
    s6U1: String?,
    s6U6: String?,
    s7U3: String?,
    s11U3: String?,
    s11U2: String?,
    s11U4: String?,
    s11U6: String?,
    s10U1: String?,
    ogrsY2: String?,
    ovpRiskReconElm: String?,
    ospCdc: String?,
    ospIiic: String?,
    rsrPercentageScore: String?,
    saraRiskLevelToPartner: String?,
    saraRiskLevelToOther: String?,
    @Suppress("UNUSEDUPARAMETER")
    sexDomain: String?,
    @Suppress("UNUSEDUPARAMETER")
    thinkingDomain: String?,
    @Suppress("UNUSEDUPARAMETER")
    relaDomain: String?,
    @Suppress("UNUSEDUPARAMETER")
    smDomain: String?,
    @Suppress("UNUSEDUPARAMETER")
    overallNeedsCheck: String?,
    @Suppress("UNUSEDUPARAMETER")
    overallRiskScore: String?,
    @Suppress("UNUSEDUPARAMETER")
    expectedPni: String?,
    @Suppress("UNUSEDUPARAMETER")
    sdScore: String?,
    @Suppress("UNUSEDUPARAMETER")
    tdScore: String?,
    @Suppress("UNUSEDUPARAMETER")
    rdScore: String?,
    @Suppress("UNUSEDUPARAMETER")
    smScore: String?,
    @Suppress("UNUSEDUPARAMETER")
    needsScoreMissingIgnored: String?,
    @Suppress("UNUSEDUPARAMETER")
    convertedRsrLevel: String?,
    @Suppress("UNUSEDUPARAMETER")
    removeLeading: String?,
    @Suppress("UNUSEDUPARAMETER")
    sdCountMissing: String?,
    @Suppress("UNUSEDUPARAMETER")
    tdCountMissing: String?,
    @Suppress("UNUSEDUPARAMETER")
    rdCountMissing: String?,
    @Suppress("UNUSEDUPARAMETER")
    smCountMissing: String?,
    @Suppress("UNUSEDUPARAMETER")
    sdPossibleRawScore: String?,
    @Suppress("UNUSEDUPARAMETER")
    tdPossibleRawScore: String?,
    @Suppress("UNUSEDUPARAMETER")
    rdPossibleRawScore: String?,
    @Suppress("UNUSEDUPARAMETER")
    smPossibleRawScore: String?,
    @Suppress("UNUSEDUPARAMETER")
    sdPossibleBand: String?,
    @Suppress("UNUSEDUPARAMETER")
    tdPossibleBand: String?,
    @Suppress("UNUSEDUPARAMETER")
    rdPossibleBand: String?,
    @Suppress("UNUSEDUPARAMETER")
    smPossibleBand: String?,
    @Suppress("UNUSEDUPARAMETER")
    projectedNeedsScore: String?,
    @Suppress("UNUSEDUPARAMETER")
    couldNeedsScoreChange: String?,
    @Suppress("UNUSEDUPARAMETER")
    usedNeedScore: String?,
    @Suppress("UNUSEDUPARAMETER")
    transform6U11: String?,
  ) {
    val request = RiskScoreRequest().copy(
      temperControl = s11U4.toProblemScore(),
      proCriminalAttitudes = s12U1.toProblemScore(),
      sexualPreoccupation = s11U11.toProblemScore(),
      sexualInterestsOffenceRelated = s11U12.toProblemScore(),
      emotionalCongruence = s6U12.toProblemScore(),
      saraRiskToPartner = saraRiskLevelToPartner.toSaraRiskBand(),
      saraRiskToOthers = saraRiskLevelToOther.toSaraRiskBand(),
      hostileOrientation = s12U9.toProblemScore(),
      currentRelationshipFamilyMembers = s6U1.toProblemScore(),
      previousCloseRelationships = s6U6.toProblemScore(),
      easilyInfluencedByCriminals = s7U3.toProblemScore(),
      controllingBehaviour = s11U3.toProblemScore(),
      impulsivityBehaviour = s11U2.toProblemScore(),
      problemSolvingSkills = s11U6.toProblemScore(),
      difficultiesCoping = s10U1.toProblemScore(),
      inCustodyOrCommunity = community.toCustodyOrCommunity(),
      hasCommittedSexualOffence = s1U30.toYesNoBoolean(),
      riskSexualHarm = s6U11.toYesNoBoolean(),
    )

    val rsr = emptyRSR().copy(
      ospdcBand = ospCdc.toRiskBand(),
      ospiicBand = ospIiic.toRiskBand(),
      rsrScore = rsrPercentageScore?.toIntOrNull(),
    )
    val ovp = emptyOVP().copy(
      provenViolentTypeReoffendingTwoYear = ovpRiskReconElm.bandToOVPScore(),
      band = ovpRiskReconElm.toRiskBand(),
    )
    val ogrs3 = emptyOGRS3().copy(
      ogrs3TwoYear = ogrsY2?.bandToOGRSScore(),
    )
    val result = service.getRiskScore(
      request,
      emptyContext().copy(
        RSR = rsr,
        OVP = ovp,
        OGRS3 = ogrs3,
      ),
    )
    val expectedPathway =
      expectedOutcome?.let {
        when (it) {
          "'H'" -> ProgrammeNeedIdentifier.HIGH
          "'M'" -> ProgrammeNeedIdentifier.MODERATE
          "'A'" -> ProgrammeNeedIdentifier.ALTERNATIVE
          "'O'" -> ProgrammeNeedIdentifier.OMISSION
          else -> null
        }
      } ?: throw IllegalArgumentException("Cannot parse PNI outcome from CSV")

    if (expectedPathway != result.PNI?.pniPathway) {
      // write file
      failedRows.add(
        arrayListOf(
          id, scenario, expectedOutcome, community, s1U30, s6U11, s11U11, s11U12, s6U12, s12U1,
          s12U9, s6U1, s6U6, s7U3, s11U3, s11U2, s11U4, s11U6, s10U1, ogrsY2,
          ovpRiskReconElm, ospCdc, ospIiic, rsrPercentageScore, saraRiskLevelToPartner,
          saraRiskLevelToOther, sexDomain, thinkingDomain, relaDomain, smDomain,
          overallNeedsCheck, overallRiskScore, expectedPni, sdScore, tdScore, rdScore,
          smScore, needsScoreMissingIgnored, convertedRsrLevel, removeLeading,
          sdCountMissing, tdCountMissing, rdCountMissing, smCountMissing,
          sdPossibleRawScore, tdPossibleRawScore, rdPossibleRawScore, smPossibleRawScore,
          sdPossibleBand, tdPossibleBand, rdPossibleBand, smPossibleBand,
          projectedNeedsScore, couldNeedsScoreChange, usedNeedScore, transform6U11,
        ).joinToString(","),
      )
    }
    assertEquals(expectedPathway, result.PNI?.pniPathway)
  }
}

private fun String?.toProblemScore(): ProblemLevel? = when (this?.toIntOrNull()) {
  0 -> ProblemLevel.NO_PROBLEMS
  1 -> ProblemLevel.SOME_PROBLEMS
  2 -> ProblemLevel.SIGNIFICANT_PROBLEMS
  else -> null
}

private fun String?.toSaraRiskBand(): RiskBand? = when (this?.toIntOrNull()) {
  1 -> RiskBand.LOW
  2 -> RiskBand.MEDIUM
  3 -> RiskBand.HIGH
  else -> null
}

private fun String?.toRiskBand(): RiskBand? = when (this) {
  "'H'" -> RiskBand.HIGH
  "'V'" -> RiskBand.VERY_HIGH
  "'M'" -> RiskBand.MEDIUM
  "'L'" -> RiskBand.LOW
  "'NA'" -> RiskBand.NOT_APPLICABLE
  "'NULL'" -> null
  else -> null
}

private fun String?.bandToOVPScore(): Int? = when (this.toRiskBand()) {
  RiskBand.LOW -> 10 // between 1 and 29
  RiskBand.MEDIUM -> 40 // between 30 and 59
  RiskBand.HIGH -> 70 // between 60 and 79
  RiskBand.VERY_HIGH -> 85 // between 80 and 99
  RiskBand.NOT_APPLICABLE -> null
  null -> null
}

private fun String?.bandToOGRSScore(): Int? = when (this.toRiskBand()) {
  RiskBand.LOW -> 30 // between 0 and 49
  RiskBand.MEDIUM -> 60 // between 50 and 74
  RiskBand.HIGH -> 80 // between 75 and 89
  RiskBand.VERY_HIGH -> 95 // 90 plus
  RiskBand.NOT_APPLICABLE -> null
  null -> null
}

private fun String?.toCustodyOrCommunity(): CustodyOrCommunity = when {
  this?.contains("YES") == true -> CustodyOrCommunity.COMMUNITY
  else -> CustodyOrCommunity.CUSTODY
}

private fun String?.toYesNoBoolean(): Boolean? = when {
  this?.contains("YES") == true -> true
  this?.contains("NO") == true -> false
  else -> null
}
