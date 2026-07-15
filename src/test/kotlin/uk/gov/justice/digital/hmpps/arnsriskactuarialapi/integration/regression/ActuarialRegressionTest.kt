package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.regression

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CurrentRelationshipStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.MotivationLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.PreviousConviction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.RiskScoreResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.asDoublePercentage
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.roundToNDecimals
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.sanitisePercentage
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.sigmoid
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.fail

private const val TEST_CSV_FILE = "/regression/v4_1_1_oasys_test_data.csv"
private const val WRITE_FAILED_OUTPUTS = false

class ActuarialRegressionTest : IntegrationTestBase() {
  
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
    id: String,
    dob: String,
    gender: String,
    offence_code: String,
    total_sanctions_count: String,
    total_violent_sanctions: String,
    first_sanction_date: String,
    last_sanction_date: String,
    community_date: String,
    two_point_two: String,
    three_point_four: String,
    four_point_two: String,
    six_point_four: String,
    six_point_seven: String,
    six_point_eight: String,
    seven_point_two: String,
    amphetamines: String,
    benzodiazipines: String,
    cannabis: String,
    crack_cocaine: String,
    ecstasy: String,
    hallucinogens: String,
    heroin: String,
    ketamine: String,
    methadone: String,
    misused_prescribed: String,
    other_drugs: String,
    other_opiate: String,
    powder_cocaine: String,
    solvents: String,
    spice: String,
    steroids: String,
    eight_point_eight: String,
    nine_point_one: String,
    nine_point_two: String,
    eleven_point_two: String,
    eleven_point_four: String,
    twelve_point_one: String,
    aggravated_burglary: String,
    arson: String,
    criminal_damage: String,
    firearms: String,
    gbh: String,
    homicide: String,
    kidnap: String,
    robbery: String,
    weapons_not_firearms: String,
    serious_violence_brief_predictions: String,
    serious_violence_extended_predictions: String,
    violence_brief_predictions: String,
    violence_extended_predictions: String,
    all_brief_predictions: String,
    all_extended_predictions: String,
    aai: String,
    ageatfirst: String,
    ageatsanction: String,
    yearssincefirst: String,
    ogrs4g_rate: String,
    ogrs4v_rate_general: String,
    ogrs4_targoff: String,
    female: String,
    firstsanction: String,
    secondsanction: String,
    onceviolent: String,
    ogrs4v_rate_violent: String,
    index_firearms: String,
    index_farmers_shotgun: String,
    index_weapons_not_firearm: String,
    index_abh_or_above: String,
    ofm: String,
    maleaai: String,
    maleaaiaai: String,
    maleaaiaaiaai: String,
    maleaaiaaiaaiaai: String,
    aaifemale: String,
    aaiaaifemale: String,
    aaiaaiaaifemale: String,
    aaiaaiaaiaaifemale: String,
    ogrs4_targoffAbsconding_bail: String,
    ogrs4_targoffBurglary__domestic_: String,
    ogrs4_targoffBurglary__other_: String,
    ogrs4_targoffCriminal_damage: String,
    ogrs4_targoffDrink_driving: String,
    ogrs4_targoffDrug_import_export_: String,
    ogrs4_targoffDrug_possession_sup: String,
    ogrs4_targoffDrunkenness: String,
    ogrs4_targoffFraud__forgery___mi: String,
    ogrs4_targoffHandling_stolen_goo: String,
    ogrs4_targoffMotoring__not_drink: String,
    ogrs4_targoffOther: String,
    ogrs4_targoffPublic_order__haras: String,
    ogrs4_targoffRobbery: String,
    ogrs4_targoffSexual__vs_children: String,
    ogrs4_targoffSexual__not_childre: String,
    ogrs4_targoffTheft: String,
    ogrs4_targoffVATP: String,
    ogrs4_targoffVehicle_related_the: String,
    ogrs4_targoffWelfare_fraud: String,
    ofmofm: String,
    ofmofmofm: String,
    ofmofmofmofm: String,
    malesecondsanctionyearssincefirs: String,
    femalesecondsanctionyearssincefi: String,
    malethreeplussanctionsogrs4v_rat: String,
    femalethreeplussanctionsogrs4v_r: String,
    malethreeplussanctionsogrs4g_rat: String,
    femalethreeplussanctionsogrs4g_r: String,
    malethreeplussanctionsogrs4g_rao: String,
    femalethreeplussanctionsogrs4g_o: String,
    maleneverviolent: String,
    femaleneverviolent: String,
    S3Q2_PARTNER: String,
    S3Q2_PARTNERS6Q4: String,
    heroin_feature: String,
    methadone_feature: String,
    otheropiate: String,
    crack: String,
    cokepowder: String,
    prescribed: String,
    benzo: String,
    cannabis_feature: String,
    steroid: String,
    otherdrug_code_iln: String,
    row_type: String,
  ) {

    if (four_point_two == "1") {
      print("Auto-pass due to unemployment being 1!")
      return
    } else if (six_point_eight == "0") {
      print("Auto-pass due to relationship status being 0!")
      return
    } else if (offence_code == "14100" || offence_code == "08800" || offence_code == "11100") {
      print("Auto-pass due to offence code being NEED_DETAILS_OF_EXACT_OFFENCE!")
      return
    }

    val request = RiskScoreRequest(
      version = RiskScoreVersion.V1_0,
      assessmentDate = LocalDate.parse("2025-10-03"),
      dateOfBirth = LocalDate.parse(dob),
      gender = gender.convertGender(),
      dateOfCurrentConviction = LocalDate.parse(last_sanction_date),
      currentOffenceCode = offence_code,
      totalNumberOfSanctionsForAllOffences = total_sanctions_count.toInt(),
      ageAtFirstSanction = ageatfirst.toInt(),
      dateAtStartOfFollowup = LocalDate.parse(community_date),
      totalNumberOfViolentSanctions = total_violent_sanctions.toInt(),
      didOffenceInvolveCarryingOrUsingWeapon = two_point_two.toOneZeroBoolean(),
      suitabilityOfAccommodation = three_point_four.toProblemScore(),
      isUnemployed = four_point_two.toEmploymentBoolean(),
      currentRelationshipWithPartner = six_point_four.toProblemScore(),
      evidenceOfDomesticAbuse = six_point_seven.toOneZeroBoolean(),
      currentRelationshipStatus = six_point_eight.toRelationshipScore(),
      motivationToTackleDrugMisuse = eight_point_eight.toDrugMotivation(),
      currentAlcoholUseProblems = nine_point_one.toProblemScore(),
      excessiveAlcoholUse = nine_point_two.toProblemScore(),
      impulsivityProblems = eleven_point_two.toProblemScore(),
      temperControl = eleven_point_four.toProblemScore(),
      proCriminalAttitudes = twelve_point_one.toProblemScore(),
      regularOffendingActivities = seven_point_two.toProblemScore(),
      previousConvictions = buildPreviousConvictionsList(aggravated_burglary, arson, criminal_damage, firearms, gbh, homicide, kidnap, robbery, weapons_not_firearms),
      hasHeroinUsage = heroin.toYesNoBoolean(),
      hasOtherOpiateUsage = other_opiate.toYesNoBoolean(),
      hasCrackCocaineUsage = crack_cocaine.toYesNoBoolean(),
      hasPowderCocaineUsage = powder_cocaine.toYesNoBoolean(),
      hasMisusedPrescriptionDrugUsage = misused_prescribed.toYesNoBoolean(),
      hasBenzodiazepinesUsage = benzodiazipines.toYesNoBoolean(),
      hasCannabisUsage =cannabis.toYesNoBoolean(),
      hasMethadoneUsage = methadone.toYesNoBoolean(),
      hasSteroidsUsage = steroids.toYesNoBoolean(),
      hasOtherDrugsUsage = other_drugs.toYesNoBoolean(),
      hasKetamineUsage = ketamine.toYesNoBoolean(),
      hasSpiceUsage = spice.toYesNoBoolean(),
      hasHallucinogensUsage = hallucinogens.toYesNoBoolean(),
      hasSolventsUsage = solvents.toYesNoBoolean(),
    )

    val responseBody = webTestClient.post()
      .uri("/risk-scores/v1")
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ARNS_RISK_ACTUARIAL")))
      .bodyValue(request)
      .exchange()
      .expectStatus().isOk
      .expectBody<RiskScoreResponse>()
      .returnResult()
      .responseBody

    println(responseBody)

    assertEquals(all_extended_predictions.toDouble().asDoublePercentage(), responseBody?.actuarialPredictors?.allPredictor?.output?.score)
  }
}

fun String.toDrugMotivation(): MotivationLevel? = when (this) {
  "0" -> MotivationLevel.FULL_MOTIVATION
  "1" -> MotivationLevel.PARTIAL_MOTIVATION
  "2" -> MotivationLevel.NO_MOTIVATION
  else -> throw IllegalArgumentException("Input must 0, 1 or 2")
}

fun String.toRelationshipScore(): CurrentRelationshipStatus? = when (this) {
  "1" -> CurrentRelationshipStatus.IN_RELATIONSHIP_LIVING_TOGETHER
  "2" -> CurrentRelationshipStatus.IN_RELATIONSHIP_NOT_LIVING_TOGETHER
  "3" -> CurrentRelationshipStatus.NOT_IN_RELATIONSHIP
  else -> throw IllegalArgumentException("Input must 1, 2 or 3")
}

private fun String.toYesNoBoolean(): Boolean = when (this) {
  "Y" -> true
  "N" -> false
  else -> throw IllegalArgumentException("Input must Y or N")
}

private fun String.toOneZeroBoolean(): Boolean = when (this) {
  "1" -> true
  "0" -> false
  else -> throw IllegalArgumentException("Input must be 0 or 1")
}

private fun String.toEmploymentBoolean(): Boolean = when (this) {
  "0" -> false
  "1" -> throw IllegalArgumentException("Employment cannot be 1")
  "2" -> true
  else -> throw IllegalArgumentException("Input must be 0, 1 or 2")
}

private fun String.convertGender(): Gender = when (this) {
  "M" -> Gender.MALE
  "F" -> Gender.FEMALE
  else -> throw IllegalArgumentException("Gender must be M or F")
}

private fun String.toProblemScore(): ProblemLevel = when (this) {
  "0" -> ProblemLevel.NO_PROBLEMS
  "1" -> ProblemLevel.SOME_PROBLEMS
  "2" -> ProblemLevel.SIGNIFICANT_PROBLEMS
  else -> throw IllegalArgumentException("Input must 0, 1 or 2")
}

private fun buildPreviousConvictionsList(aggravatedBurglary: String, arson: String, criminalDamage: String, firearms: String, gbh: String, homicide: String, kidnap: String, robbery: String, weaponsNotFirearms: String): List<PreviousConviction> {
  return buildList {
    if (aggravatedBurglary == "1") {
      add(PreviousConviction.AGGRAVATED_BURGLARY)
    }
    if (arson == "1") {
      add(PreviousConviction.ARSON)
    }
    if (criminalDamage == "1") {
      add(PreviousConviction.CRIMINAL_DAMAGE)
    }
    if (firearms == "1") {
      add(PreviousConviction.FIREARMS)
    }
    if (gbh == "1") {
      add(PreviousConviction.WOUNDING_GBH)
    }
    if (homicide == "1") {
      add(PreviousConviction.HOMICIDE)
    }
    if (kidnap == "1") {
      add(PreviousConviction.KIDNAPPING)
    }
    if (robbery == "1") {
      add(PreviousConviction.ROBBERY)
    }
    if (weaponsNotFirearms == "1") {
      add(PreviousConviction.WEAPON)
    }
  }
}
