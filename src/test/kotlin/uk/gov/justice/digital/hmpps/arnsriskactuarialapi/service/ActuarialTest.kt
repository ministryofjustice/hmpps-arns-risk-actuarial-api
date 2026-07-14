package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ActuarialRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CurrentRelationshipStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.MotivationLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.ProgrammeNeedIdentifier
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyAllReoffendingPredictor
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyRSR
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyViolentReoffendingPredictor
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.CommonValidator
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.PNIValidator
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate

private const val TEST_CSV_FILE = "/data/PNI/v4_1_1_oasys_test_data.csv"
private const val WRITE_FAILED_OUTPUTS = false

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActuarialTest {

  private val service = PNIRiskProducerService(PNIValidator(CommonValidator()))
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
    dob: String?,
    gender: String?,
    offence_code: String?,
    total_sanctions_count: String?,
    total_violent_sanctions: String?,
    @Suppress("UNUSEDUPARAMETER")
    first_sanction_date: String?,
    last_sanction_date: String?,
    community_date: String?,
    two_point_two: String?,
    three_point_four: String?,
    four_point_two: String?,
    six_point_four: String?,
    six_point_seven: String?,
    six_point_eight: String?,
    @Suppress("UNUSEDUPARAMETER")
    seven_point_two: String?,
    @Suppress("UNUSEDUPARAMETER")
    amphetamines: String?,
    benzodiazipines: String?,
    cannabis: String?,
    crack_cocaine: String?,
    @Suppress("UNUSEDUPARAMETER")
    ecstasy: String?,
    hallucinogens: String?,
    heroin: String?,
    ketamine: String?,
    methadone: String?,
    misused_prescribed: String?,
    other_drugs: String?,
    other_opiate: String?,
    powder_cocaine: String?,
    solvents: String?,
    spice: String?,
    steroids: String?,
    eight_point_eight: String?,
    nine_point_one: String?,
    nine_point_two: String?,
    eleven_point_two: String?,
    eleven_point_four: String?,
    twelve_point_one: String?,
    aggravated_burglary: String?,
    arson: String?,
    criminal_damage: String?,
    firearms: String?,
    gbh: String?,
    homicide: String?,
    kidnap: String?,
    robbery: String?,
    weapons_not_firearms: String?,
    serious_violence_brief_predictions: String?,
    serious_violence_extended_predictions: String?,
    violence_brief_predictions: String?,
    violence_extended_predictions: String?,
    all_brief_predictions: String?,
    all_extended_predictions: String?,
    @Suppress("UNUSEDUPARAMETER")
    aai: String?,
    ageatfirst: String?,
    ageatsanction: String?,
    @Suppress("UNUSEDUPARAMETER")
    yearssincefirst: String?,
    ogrs4g_rate: String?,
    @Suppress("UNUSEDUPARAMETER")
    ogrs4v_rate_general: String?,
    @Suppress("UNUSEDUPARAMETER")
    ogrs4_targoff: String?,
    female: String?,
    firstsanction: String?,
    secondsanction: String?,
    onceviolent: String?,
    ogrs4v_rate_violent: String?,
    @Suppress("UNUSEDUPARAMETER")
    index_firearms: String?,
    @Suppress("UNUSEDUPARAMETER")
    index_farmers_shotgun: String?,
    @Suppress("UNUSEDUPARAMETER")
    index_weapons_not_firearm: String?,
    @Suppress("UNUSEDUPARAMETER")
    index_abh_or_above: String?,
    @Suppress("UNUSEDUPARAMETER")
    ofm: String?,
    @Suppress("UNUSEDUPARAMETER")
    maleaai: String?,
    @Suppress("UNUSEDUPARAMETER")
    maleaaiaai: String?,
    @Suppress("UNUSEDUPARAMETER")
    maleaaiaaiaai: String?,
    @Suppress("UNUSEDUPARAMETER")
    maleaaiaaiaaiaai: String?,
    @Suppress("UNUSEDUPARAMETER")
    aaifemale: String?,
    @Suppress("UNUSEDUPARAMETER")
    aaiaaifemale: String?,
    @Suppress("UNUSEDUPARAMETER")
    aaiaaiaaifemale: String?,
    @Suppress("UNUSEDUPARAMETER")
    aaiaaiaaiaaifemale: String?,
    @Suppress("UNUSEDUPARAMETER")
    ogrs4_targoffAbsconding_bail: String?,
    @Suppress("UNUSEDUPARAMETER")
    ogrs4_targoffBurglary__domestic_: String?,
    @Suppress("UNUSEDUPARAMETER")
    ogrs4_targoffBurglary__other_: String?,
    @Suppress("UNUSEDUPARAMETER")
    ogrs4_targoffCriminal_damage: String?,
    @Suppress("UNUSEDUPARAMETER")
    ogrs4_targoffDrink_driving: String?,
    @Suppress("UNUSEDUPARAMETER")
    ogrs4_targoffDrug_import_export_: String?,
    @Suppress("UNUSEDUPARAMETER")
    ogrs4_targoffDrug_possession_sup: String?,
    @Suppress("UNUSEDUPARAMETER")
    ogrs4_targoffDrunkenness: String?,
    @Suppress("UNUSEDUPARAMETER")
    ogrs4_targoffFraud__forgery___mi: String?,
    @Suppress("UNUSEDUPARAMETER")
    ogrs4_targoffHandling_stolen_goo: String?,
    @Suppress("UNUSEDUPARAMETER")
    ogrs4_targoffMotoring__not_drink: String?,
    @Suppress("UNUSEDUPARAMETER")
    ogrs4_targoffOther: String?,
    @Suppress("UNUSEDUPARAMETER")
    ogrs4_targoffPublic_order__haras: String?,
    @Suppress("UNUSEDUPARAMETER")
    ogrs4_targoffRobbery: String?,
    @Suppress("UNUSEDUPARAMETER")
    ogrs4_targoffSexual__vs_children: String?,
    @Suppress("UNUSEDUPARAMETER")
    ogrs4_targoffSexual__not_childre: String?,
    @Suppress("UNUSEDUPARAMETER")
    ogrs4_targoffTheft: String?,
    @Suppress("UNUSEDUPARAMETER")
    ogrs4_targoffVATP: String?,
    @Suppress("UNUSEDUPARAMETER")
    ogrs4_targoffVehicle_related_the: String?,
    @Suppress("UNUSEDUPARAMETER")
    ogrs4_targoffWelfare_fraud: String?,
    @Suppress("UNUSEDUPARAMETER")
    ofmofm: String?,
    @Suppress("UNUSEDUPARAMETER")
    ofmofmofm: String?,
    @Suppress("UNUSEDUPARAMETER")
    ofmofmofmofm: String?,
    @Suppress("UNUSEDUPARAMETER")
    malesecondsanctionyearssincefirs: String?,
    @Suppress("UNUSEDUPARAMETER")
    femalesecondsanctionyearssincefi: String?,
    @Suppress("UNUSEDUPARAMETER")
    malethreeplussanctionsogrs4v_rat: String?,
    @Suppress("UNUSEDUPARAMETER")
    femalethreeplussanctionsogrs4v_r: String?,
    @Suppress("UNUSEDUPARAMETER")
    malethreeplussanctionsogrs4g_rat: String?,
    @Suppress("UNUSEDUPARAMETER")
    femalethreeplussanctionsogrs4g_r: String?,
    @Suppress("UNUSEDUPARAMETER")
    malethreeplussanctionsogrs4g_rao: String?,
    @Suppress("UNUSEDUPARAMETER")
    femalethreeplussanctionsogrs4g_o: String?,
    @Suppress("UNUSEDUPARAMETER")
    maleneverviolent: String?,
    @Suppress("UNUSEDUPARAMETER")
    femaleneverviolent: String?,
    @Suppress("UNUSEDUPARAMETER")
    S3Q2_PARTNER: String?,
    @Suppress("UNUSEDUPARAMETER")
    S3Q2_PARTNERS6Q4: String?,
    @Suppress("UNUSEDUPARAMETER")
    heroin_feature: String?,
    @Suppress("UNUSEDUPARAMETER")
    methadone_feature: String?,
    @Suppress("UNUSEDUPARAMETER")
    otheropiate: String?,
    @Suppress("UNUSEDUPARAMETER")
    crack: String?,
    @Suppress("UNUSEDUPARAMETER")
    cokepowder: String?,
    @Suppress("UNUSEDUPARAMETER")
    prescribed: String?,
    @Suppress("UNUSEDUPARAMETER")
    benzo: String?,
    @Suppress("UNUSEDUPARAMETER")
    cannabis_feature: String?,
    @Suppress("UNUSEDUPARAMETER")
    steroid: String?,
    @Suppress("UNUSEDUPARAMETER")
    otherdrug_code_iln: String?,
    @Suppress("UNUSEDUPARAMETER")
    row_type: String?,
  ) {
    val request = ActuarialRequest().copy(
      dateOfBirth = dob as LocalDate?,
      gender = gender.convertGender(),
      dateOfCurrentConviction = last_sanction_date as LocalDate?,
      currentOffenceCode = offence_code?.toIntOrNull() as String?,
      totalNumberOfSanctionsForAllOffences = total_sanctions_count?.toIntOrNull(),
      ageAtFirstSanction = ageatfirst?.toIntOrNull(),
      dateAtStartOfFollowupUserInput = last_sanction_date as LocalDate?,
      dateAtStartOfFollowupCalculated = community_date as LocalDate?,
      totalNumberOfViolentSanctions = total_violent_sanctions?.toIntOrNull(),
      didOffenceInvolveCarryingOrUsingWeapon = two_point_two.toOneZeroBoolean(),
      suitabilityOfAccommodation = three_point_four.toProblemScore(),
      isUnemployed = four_point_two.toOneZeroBoolean(),
      currentRelationshipWithPartner = six_point_four.toProblemScore(),
      hasEvidenceOfDomesticAbuse = six_point_seven.toOneZeroBoolean(),
      currentRelationshipStatus = six_point_eight.toRelationshipScore(),
      motivationToTackleDrugMisuse = eight_point_eight.toDrugMotivation(),
      currentAlcoholUseProblems = nine_point_one.toProblemScore(),
      excessiveAlcoholUse = nine_point_two.toProblemScore(),
      impulsivityProblems = eleven_point_two.toProblemScore(),
      temperControl = eleven_point_four.toProblemScore(),
      proCriminalAttitudes = twelve_point_one.toProblemScore(),
      previousConvictions = previousConvictionsList(),
      hasHeroinUsage = heroin.toYesNoBoolean(),
      hasKetamineUsage = ketamine.toYesNoBoolean(),
      hasMethadoneUsage = methadone.toYesNoBoolean(),
      hasMisusedPrescriptionDrugUsage = misused_prescribed.toYesNoBoolean(),
      hasOtherDrugsUsage = other_drugs.toYesNoBoolean(),
      hasOtherOpiateUsage = other_opiate.toYesNoBoolean(),
      hasPowderCocaineUsage = powder_cocaine.toYesNoBoolean(),
      hasSolventsUsage = solvents.toYesNoBoolean(),
      hasSpiceUsage = spice.toYesNoBoolean(),
      hasSteroidsUsage = steroids.toYesNoBoolean(),
    )
  }
}

fun String?.toDrugMotivation(): MotivationLevel? = when (this?.toIntOrNull()) {
  0 -> MotivationLevel.FULL_MOTIVATION
  1 -> MotivationLevel.PARTIAL_MOTIVATION
  2 -> MotivationLevel.NO_MOTIVATION
  else -> null
}

fun String?.toRelationshipScore(): CurrentRelationshipStatus? = when (this?.toIntOrNull()) {
  1 -> CurrentRelationshipStatus.IN_RELATIONSHIP_NOT_LIVING_TOGETHER
  2 -> CurrentRelationshipStatus.IN_RELATIONSHIP_LIVING_TOGETHER
  3 -> CurrentRelationshipStatus.NOT_IN_RELATIONSHIP
  else -> null
}

private fun String?.toYesNoBoolean(): Boolean? = when {
  this?.contains("YES") == true -> true
  this?.contains("NO") == true -> false
  else -> null
}

private fun String?.toOneZeroBoolean(): Boolean? = when {
  this?.contains("1") == true -> true
  this?.contains("0") == true -> false
  else -> null
}

private fun String?.convertGender(): String = when {
    this?.contains("M") == true -> "MALE"
    this?.contains("F") == true -> "FEMALE"
    else -> "UNKNOWN"
}
