package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.regression

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CurrentRelationshipStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.MotivationLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.PreviousConviction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import java.time.LocalDate
import java.time.Period

fun loadActuarialRegressionTestCsv(resourcePath: String): List<ActuarialRegressionTestCase> {
  val inputStream = object {}.javaClass.getResourceAsStream(resourcePath) ?: throw IllegalArgumentException("Resource not found: $resourcePath")

  return inputStream.bufferedReader().useLines { lines ->
    lines.drop(1)
      .map { line ->
        val cols = line.split(",").map { col ->
          col.trim().takeIf { it.isNotEmpty() }
        }
        ActuarialRegressionTestCase(
          id = cols[0]?.toInt(),
          dateOfBirth = cols[1]?.let { LocalDate.parse(it) },
          gender = cols[2],
          offenceCode = cols[3],
          totalSanctionsCount = cols[4]?.toInt(),
          totalViolentSanctions = cols[5]?.toInt(),
          firstSanctionDate = cols[6]?.let { LocalDate.parse(it) },
          lastSanctionDate = cols[7]?.let { LocalDate.parse(it) },
          communityDate = cols[8]?.let { LocalDate.parse(it) },
          twoPointTwo = cols[9]?.toInt(),
          threePointFour = cols[10]?.toInt(),
          fourPointTwo = cols[11]?.toInt(),
          sixPointFour = cols[12]?.toInt(),
          sixPointSeven = cols[13]?.toInt(),
          sixPointEight = cols[14]?.toInt(),
          sevenPointTwo = cols[15]?.toInt(),
          amphetamines = cols[16],
          benzodiazipines = cols[17],
          cannabis = cols[18],
          crackCocaine = cols[19],
          ecstasy = cols[20],
          hallucinogens = cols[21],
          heroin = cols[22],
          ketamine = cols[23],
          methadone = cols[24],
          misusedPrescribed = cols[25],
          otherDrugs = cols[26],
          otherOpiate = cols[27],
          powderCocaine = cols[28],
          solvents = cols[29],
          spice = cols[30],
          steroids = cols[31],
          eightPointEight = cols[32]?.toInt(),
          ninePointOne = cols[33]?.toInt(),
          ninePointTwo = cols[34]?.toInt(),
          elevenPointTwo = cols[35]?.toInt(),
          elevenPointFour = cols[36]?.toInt(),
          twelvePointOne = cols[37]?.toInt(),
          aggravatedBurglary = cols[38]?.toInt(),
          arson = cols[39]?.toInt(),
          criminalDamage = cols[40]?.toInt(),
          firearms = cols[41]?.toInt(),
          gbh = cols[42]?.toInt(),
          homicide = cols[43]?.toInt(),
          kidnap = cols[44]?.toInt(),
          robbery = cols[45]?.toInt(),
          weaponsNotFirearms = cols[46]?.toInt(),
          seriousViolenceBriefPredictions = cols[47]?.toDouble(),
          seriousViolenceExtendedPredictions = cols[48]?.toDouble(),
          violenceBriefPredictions = cols[49]?.toDouble(),
          violenceExtendedPredictions = cols[50]?.toDouble(),
          allBriefPredictions = cols[51]?.toDouble(),
          allExtendedPredictions = cols[52]?.toDouble(),
        )
      }.toList()
  }
}

fun Int.toDrugMotivation(): MotivationLevel? = when (this) {
  0 -> MotivationLevel.FULL_MOTIVATION
  1 -> MotivationLevel.PARTIAL_MOTIVATION
  2 -> MotivationLevel.NO_MOTIVATION
  else -> throw IllegalArgumentException("Input must 0, 1 or 2")
}

fun Int.toRelationshipScore(): CurrentRelationshipStatus? = when (this) {
  1 -> CurrentRelationshipStatus.IN_RELATIONSHIP_LIVING_TOGETHER
  2 -> CurrentRelationshipStatus.IN_RELATIONSHIP_NOT_LIVING_TOGETHER
  3 -> CurrentRelationshipStatus.NOT_IN_RELATIONSHIP
  else -> throw IllegalArgumentException("Input must 1, 2 or 3")
}

fun String.toYesNoBoolean(): Boolean = when (this) {
  "Y" -> true
  "N" -> false
  else -> throw IllegalArgumentException("Input must Y or N")
}

fun Int.toOneZeroBoolean(): Boolean = when (this) {
  1 -> true
  0 -> false
  else -> throw IllegalArgumentException("Input must be 0 or 1")
}

fun Int.toUnemploymentBoolean(): Boolean = when (this) {
  0 -> false
  2 -> true
  else -> throw IllegalArgumentException("Unemployment must be 0 or 2")
}

fun String.convertGender(): Gender = when (this) {
  "M" -> Gender.MALE
  "F" -> Gender.FEMALE
  else -> throw IllegalArgumentException("Gender must be M or F")
}

fun Int.toProblemScore(): ProblemLevel = when (this) {
  0 -> ProblemLevel.NO_PROBLEMS
  1 -> ProblemLevel.SOME_PROBLEMS
  2 -> ProblemLevel.SIGNIFICANT_PROBLEMS
  else -> throw IllegalArgumentException("Input must 0, 1 or 2")
}

fun buildPreviousConvictionsList(testCase: ActuarialRegressionTestCase): List<PreviousConviction> = buildList {
  if (testCase.aggravatedBurglary == 1) {
    add(PreviousConviction.AGGRAVATED_BURGLARY)
  }
  if (testCase.arson == 1) {
    add(PreviousConviction.ARSON)
  }
  if (testCase.criminalDamage == 1) {
    add(PreviousConviction.CRIMINAL_DAMAGE)
  }
  if (testCase.firearms == 1) {
    add(PreviousConviction.FIREARMS)
  }
  if (testCase.gbh == 1) {
    add(PreviousConviction.WOUNDING_GBH)
  }
  if (testCase.homicide == 1) {
    add(PreviousConviction.HOMICIDE)
  }
  if (testCase.kidnap == 1) {
    add(PreviousConviction.KIDNAPPING)
  }
  if (testCase.robbery == 1) {
    add(PreviousConviction.ROBBERY)
  }
  if (testCase.weaponsNotFirearms == 1) {
    add(PreviousConviction.WEAPON)
  }
}

fun buildRiskScoreRequest(testCase: ActuarialRegressionTestCase, staticOrDynamic: StaticOrDynamic): RiskScoreRequest {
  val staticRequest = RiskScoreRequest(
    version = RiskScoreVersion.V1_0,
    assessmentDate = LocalDate.parse("2025-10-03"),
    dateOfBirth = testCase.dateOfBirth,
    gender = testCase.gender?.convertGender(),
    dateOfCurrentConviction = testCase.lastSanctionDate,
    currentOffenceCode = testCase.offenceCode,
    totalNumberOfSanctionsForAllOffences = testCase.totalSanctionsCount,
    ageAtFirstSanction = Period.between(testCase.dateOfBirth, testCase.firstSanctionDate).years,
    dateAtStartOfFollowup = testCase.communityDate,
    totalNumberOfViolentSanctions = testCase.totalViolentSanctions,
  )

  if (staticOrDynamic == StaticOrDynamic.DYNAMIC) {
    return staticRequest.copy(
      didOffenceInvolveCarryingOrUsingWeapon = testCase.twoPointTwo?.toOneZeroBoolean(),
      suitabilityOfAccommodation = testCase.threePointFour?.toProblemScore(),
      isUnemployed = testCase.fourPointTwo?.toUnemploymentBoolean(),
      currentRelationshipWithPartner = testCase.sixPointFour?.toProblemScore(),
      evidenceOfDomesticAbuse = testCase.sixPointSeven?.toOneZeroBoolean(),
      currentRelationshipStatus = testCase.sixPointEight?.toRelationshipScore(),
      motivationToTackleDrugMisuse = testCase.eightPointEight?.toDrugMotivation(),
      currentAlcoholUseProblems = testCase.ninePointOne?.toProblemScore(),
      excessiveAlcoholUse = testCase.ninePointTwo?.toProblemScore(),
      impulsivityProblems = testCase.elevenPointTwo?.toProblemScore(),
      temperControl = testCase.elevenPointFour?.toProblemScore(),
      proCriminalAttitudes = testCase.twelvePointOne?.toProblemScore(),
      regularOffendingActivities = testCase.sevenPointTwo?.toProblemScore(),
      previousConvictions = buildPreviousConvictionsList(testCase),
      hasHeroinUsage = testCase.heroin?.toYesNoBoolean(),
      hasOtherOpiateUsage = testCase.otherOpiate?.toYesNoBoolean(),
      hasCrackCocaineUsage = testCase.crackCocaine?.toYesNoBoolean(),
      hasPowderCocaineUsage = testCase.powderCocaine?.toYesNoBoolean(),
      hasMisusedPrescriptionDrugUsage = testCase.misusedPrescribed?.toYesNoBoolean(),
      hasBenzodiazepinesUsage = testCase.benzodiazipines?.toYesNoBoolean(),
      hasCannabisUsage = testCase.cannabis?.toYesNoBoolean(),
      hasMethadoneUsage = testCase.methadone?.toYesNoBoolean(),
      hasSteroidsUsage = testCase.steroids?.toYesNoBoolean(),
      hasOtherDrugsUsage = testCase.otherDrugs?.toYesNoBoolean(),
      hasKetamineUsage = testCase.ketamine?.toYesNoBoolean(),
      hasSpiceUsage = testCase.spice?.toYesNoBoolean(),
      hasHallucinogensUsage = testCase.hallucinogens?.toYesNoBoolean(),
      hasSolventsUsage = testCase.solvents?.toYesNoBoolean(),
    )
  }
  return staticRequest
}
