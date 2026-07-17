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
