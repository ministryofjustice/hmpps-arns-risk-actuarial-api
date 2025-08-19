package uk.gov.justice.digital.hmpps.arnsriskactuarialapi

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CustodyOrCommunity
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.MotivationLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.PreviousConviction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.YesSometimesNo
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.HasQualifications
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.LDSObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MSTObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp.OGPObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3.OGRS3Object
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd.OPDObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd.OPDRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.osp.OSPDCObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ospiic.OSPIICObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ovp.OVPObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.ProgrammeNeedIdentifier
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.rsr.RSRObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.SNSVObject
import java.time.LocalDate

fun emptyOVP(): OVPObject = OVPObject(null, null, null, null)

fun emptyOGRS3(): OGRS3Object = OGRS3Object(null, null, null, null)

fun emptyOGP(): OGPObject = OGPObject(null, null, null, null, null)

fun emptyMST(): MSTObject = MSTObject(null, null, null, null)

fun emptyOPD(): OPDObject = OPDObject(false, null, null, emptyList())

fun omittedPNI(): PNIObject = PNIObject(ProgrammeNeedIdentifier.OMISSION, null)

fun emptyLDS(): LDSObject = LDSObject(null, null)

fun emptyOSPDC(): OSPDCObject = OSPDCObject(null, null, null)

fun emptySNSV(): SNSVObject = SNSVObject(null, null, null)

fun emptyRSR(): RSRObject = RSRObject(null, null, null, null, null, null, null, null, null)

fun emptyOSPIIC(): OSPIICObject = OSPIICObject(null, null, emptyList())

fun emptyContext() = RiskScoreContext(version = RiskScoreVersion.V1_0)

fun lowOgrs2() = OGRS3Object(
  10,
  20,
  RiskBand.LOW,
  emptyList(),
)

fun highOgrs2() = OGRS3Object(
  88,
  90,
  RiskBand.VERY_HIGH,
  emptyList(),
)

fun highOvp() = OVPObject(
  77,
  88,
  RiskBand.VERY_HIGH,
  emptyList(),
)

val FIXED_TEST_DATE: LocalDate = LocalDate.of(2025, 1, 1)

object RiskScoreRequestTestConstants {
  val NULL_REQUEST = RiskScoreRequest(version = RiskScoreVersion.V1_0)
  val FULL_OGP_REQUEST = RiskScoreRequest(
    version = RiskScoreVersion.V1_0,
    isCurrentlyOfNoFixedAbodeOrTransientAccommodation = true,
    isUnemployed = false,
    regularOffendingActivities = ProblemLevel.NO_PROBLEMS,
    currentDrugMisuse = ProblemLevel.NO_PROBLEMS,
    motivationToTackleDrugMisuse = MotivationLevel.FULL_MOTIVATION,
    problemSolvingSkills = ProblemLevel.NO_PROBLEMS,
    awarenessOfConsequences = YesSometimesNo.YES,
    understandsOtherPeoplesViews = ProblemLevel.NO_PROBLEMS,
    proCriminalAttitudes = ProblemLevel.SOME_PROBLEMS,
  )
  val ALT_NULL_OGP_REQUEST = RiskScoreRequest(
    version = RiskScoreVersion.V1_0,
    isCurrentlyOfNoFixedAbodeOrTransientAccommodation = null,
    isUnemployed = false,
    regularOffendingActivities = null,
    currentDrugMisuse = ProblemLevel.NO_PROBLEMS,
    motivationToTackleDrugMisuse = null,
    problemSolvingSkills = ProblemLevel.NO_PROBLEMS,
    awarenessOfConsequences = null,
    understandsOtherPeoplesViews = ProblemLevel.NO_PROBLEMS,
    proCriminalAttitudes = null,
  )
  val OGP_REQUEST_39 = RiskScoreRequest(
    version = RiskScoreVersion.V1_0,
    isCurrentlyOfNoFixedAbodeOrTransientAccommodation = true,
    isUnemployed = false,
    regularOffendingActivities = null,
    currentDrugMisuse = ProblemLevel.NO_PROBLEMS,
    motivationToTackleDrugMisuse = MotivationLevel.FULL_MOTIVATION,
    problemSolvingSkills = ProblemLevel.NO_PROBLEMS,
    awarenessOfConsequences = YesSometimesNo.YES,
    understandsOtherPeoplesViews = ProblemLevel.NO_PROBLEMS,
    proCriminalAttitudes = null,
  )
  val OGP_REQUEST_0458 = RiskScoreRequest(
    version = RiskScoreVersion.V1_0,
    isCurrentlyOfNoFixedAbodeOrTransientAccommodation = null,
    isUnemployed = false,
    regularOffendingActivities = ProblemLevel.NO_PROBLEMS,
    currentDrugMisuse = ProblemLevel.NO_PROBLEMS,
    motivationToTackleDrugMisuse = null,
    problemSolvingSkills = null,
    awarenessOfConsequences = YesSometimesNo.YES,
    understandsOtherPeoplesViews = ProblemLevel.NO_PROBLEMS,
    proCriminalAttitudes = null,
  )
  val FULL_LDS_REQUEST = RiskScoreRequest(
    version = RiskScoreVersion.V1_0,
    isCurrentlyOfNoFixedAbodeOrTransientAccommodation = true,
    transferableSkills = ProblemLevel.SOME_PROBLEMS,
    educationDifficulties = ProblemLevel.SOME_PROBLEMS,
    readingDifficulties = true,
    numeracyDifficulties = false,
    learningDifficulties = ProblemLevel.SOME_PROBLEMS,
    professionalOrVocationalQualifications = HasQualifications.ANY_QUALIFICATION,
  )
  val INELIGIBLE_LDS_REQUEST = RiskScoreRequest(
    version = RiskScoreVersion.V1_0,
    isCurrentlyOfNoFixedAbodeOrTransientAccommodation = true,
    transferableSkills = ProblemLevel.SOME_PROBLEMS,
    educationDifficulties = null,
    readingDifficulties = true,
    numeracyDifficulties = false,
    learningDifficulties = null,
    professionalOrVocationalQualifications = HasQualifications.ANY_QUALIFICATION,
  )
  val BAD_READING_DIFFICULTY_LDS_REQUEST = RiskScoreRequest(
    version = RiskScoreVersion.V1_0,
    isCurrentlyOfNoFixedAbodeOrTransientAccommodation = true,
    transferableSkills = ProblemLevel.SOME_PROBLEMS,
    educationDifficulties = null,
    readingDifficulties = true,
    numeracyDifficulties = null,
    learningDifficulties = ProblemLevel.SOME_PROBLEMS,
    professionalOrVocationalQualifications = HasQualifications.ANY_QUALIFICATION,
  )
  val FULL_OSPIIC_REQUEST = RiskScoreRequest(
    version = RiskScoreVersion.V1_0,
    gender = Gender.MALE,
    totalContactAdultSexualSanctions = 5,
    totalContactChildSexualSanctions = 2,
    totalIndecentImageSanctions = 4,
    totalNonContactSexualOffences = 6,
  )
}

fun validOVPRiskScoreRequest(): RiskScoreRequest = RiskScoreRequest(
  version = RiskScoreVersion.V1_0,
  gender = Gender.MALE,
  dateOfBirth = LocalDate.of(1964, 10, 15),
  dateOfCurrentConviction = LocalDate.of(2014, 12, 13),
  dateAtStartOfFollowup = LocalDate.of(2027, 12, 12),
  totalNumberOfSanctionsForAllOffences = 30 as Integer?,
  totalNumberOfViolentSanctions = 10 as Integer?,
  doesRecogniseImpactOfOffendingOnOthers = true,
  isCurrentlyOfNoFixedAbodeOrTransientAccommodation = true,
  ageAtFirstSanction = null,
  currentOffenceCode = null,
  isUnemployed = true,
  currentAlcoholUseProblems = ProblemLevel.NO_PROBLEMS,
  excessiveAlcoholUse = ProblemLevel.NO_PROBLEMS,
  hasCurrentPsychiatricTreatment = false,
  temperControl = ProblemLevel.NO_PROBLEMS,
  proCriminalAttitudes = ProblemLevel.NO_PROBLEMS,
)

fun validMSTRiskScoreRequest(): RiskScoreRequest = RiskScoreRequest(
  version = RiskScoreVersion.V1_0,
  gender = Gender.MALE,
  dateOfBirth = FIXED_TEST_DATE.minusYears(18),
  assessmentDate = FIXED_TEST_DATE,
  hasPeerGroupInfluences = true,
  influenceFromCriminalAssociates = ProblemLevel.SOME_PROBLEMS,
  recklessnessAndRiskTakingBehaviour = ProblemLevel.SOME_PROBLEMS,
  difficultiesCoping = ProblemLevel.SOME_PROBLEMS,
  attitudesTowardsSelf = ProblemLevel.SOME_PROBLEMS,
  impulsivityProblems = ProblemLevel.SOME_PROBLEMS,
  temperControl = ProblemLevel.SOME_PROBLEMS,
  problemSolvingSkills = ProblemLevel.SOME_PROBLEMS,
  awarenessOfConsequences = YesSometimesNo.SOMETIMES,
  understandsOtherPeoplesViews = ProblemLevel.SOME_PROBLEMS,
)

fun validPNIRiskScoreRequest(): RiskScoreRequest = RiskScoreRequest(
  version = RiskScoreVersion.V1_0,
  gender = Gender.MALE,
  assessmentDate = FIXED_TEST_DATE,
  dateOfBirth = FIXED_TEST_DATE.minusYears(18),
  hasPeerGroupInfluences = true,
  influenceFromCriminalAssociates = ProblemLevel.SOME_PROBLEMS,
  recklessnessAndRiskTakingBehaviour = ProblemLevel.SOME_PROBLEMS,
  difficultiesCoping = ProblemLevel.SOME_PROBLEMS,
  attitudesTowardsSelf = ProblemLevel.SOME_PROBLEMS,
  impulsivityProblems = ProblemLevel.SOME_PROBLEMS,
  temperControl = ProblemLevel.SOME_PROBLEMS,
  problemSolvingSkills = ProblemLevel.SOME_PROBLEMS,
  awarenessOfConsequences = YesSometimesNo.SOMETIMES,
  understandsOtherPeoplesViews = ProblemLevel.SOME_PROBLEMS,
  sexualPreoccupation = ProblemLevel.SOME_PROBLEMS,
  offenceRelatedSexualInterests = ProblemLevel.SOME_PROBLEMS,
  emotionalCongruenceWithChildren = ProblemLevel.SOME_PROBLEMS,
  proCriminalAttitudes = ProblemLevel.SOME_PROBLEMS,
  hostileOrientation = ProblemLevel.SOME_PROBLEMS,
  currentRelationshipWithFamilyMembers = ProblemLevel.SOME_PROBLEMS,
  previousCloseRelationships = ProblemLevel.SOME_PROBLEMS,
  easilyInfluencedByCriminalAssociates = ProblemLevel.SOME_PROBLEMS,
  controllingOrAggressiveBehaviour = ProblemLevel.SOME_PROBLEMS,
  supervisionStatus = CustodyOrCommunity.COMMUNITY,
  saraRiskToPartner = RiskBand.LOW,
  saraRiskToOthers = RiskBand.LOW,
)

fun validOPDRiskScoreRequest() = RiskScoreRequest(
  version = RiskScoreVersion.V1_0,
  overallRiskForAssessment = RiskBand.HIGH,
  highestRiskLevel = RiskBand.HIGH,
  currentOffenceCode = "02700",
  applyOPDOverride = false,
  isEligibleForMappa = false,
  didOffenceInvolveCarryingOrUsingWeapon = false,
  didOffenceInvolveViolenceOrThreatOfViolence = false,
  didOffenceInvolveExcessiveUseOfViolence = false,
  didOffenceInvolveArson = false,
  offenceLinkedRiskOfSeriousHarm = false,
  offenceMotivationEmotionalState = false,
  gender = Gender.MALE,
  accommodationLinkedRiskOfSeriousHarm = false,
  experienceOfChildhood = ProblemLevel.NO_PROBLEMS,
  difficultiesCoping = ProblemLevel.NO_PROBLEMS,
  domesticAbuse = false,
  domesticAbusePartner = null,
  domesticAbuseFamily = null,
  relationshipLinkedSeriousHarm = false,
  currentPsychologicalProblems = ProblemLevel.NO_PROBLEMS,
  wellbeingEmotionalLinkedRiskOfSeriousHarm = false,
  thinkingAndBehaviourLinedToRiskOfSeriousHarm = false,
  custodialSentence = false,
  financialRelianceOnOthers = ProblemLevel.NO_PROBLEMS,
  manipulativePredatoryBehaviour = ProblemLevel.NO_PROBLEMS,
  childhoodBehaviour = false,
  currentPsychiatricProblems = ProblemLevel.NO_PROBLEMS,
  historyOfPsychiatricTreatment = false,
  medicationMentalHealth = false,
  patientSecureUnitOrHospital = false,
  obsessiveBehaviour = false,
  selfHarmSuicideAttempt = false,
  attitudeTowardsSupervision = ProblemLevel.NO_PROBLEMS,
  assaultedOrThreatenedStaff = false,
  escapeOrAbsconded = false,
  controlIssuesOrBreachOfTrust = false,
  doesRecogniseImpactOfOffendingOnOthers = false,
  recklessnessAndRiskTakingBehaviour = ProblemLevel.NO_PROBLEMS,
  impulsivityProblems = ProblemLevel.NO_PROBLEMS,
)

fun validOSPDCRiskScoreRequest() = RiskScoreRequest(
  version = RiskScoreVersion.V1_0,
  gender = Gender.MALE,
  dateOfBirth = LocalDate.of(1980, 1, 1),
  hasEverCommittedSexualOffence = true,
  totalContactAdultSexualSanctions = 1,
  totalContactChildSexualSanctions = 1,
  totalNonContactSexualOffences = 1,
  totalIndecentImageSanctions = 1,
  dateAtStartOfFollowup = LocalDate.of(2025, 1, 1),
  dateOfMostRecentSexualOffence = LocalDate.of(2000, 1, 1),
  totalNumberOfSanctionsForAllOffences = 4 as Integer,
)

fun validSNSVStaticRiskScoreRequest() = RiskScoreRequest(
  version = RiskScoreVersion.V1_0,
  gender = Gender.MALE,
  dateOfBirth = LocalDate.of(1980, 1, 1),
  assessmentDate = LocalDate.of(2025, 1, 1),
  dateOfCurrentConviction = LocalDate.of(2020, 1, 1),
  currentOffenceCode = "02700",
  totalNumberOfSanctionsForAllOffences = 1 as Integer,
  ageAtFirstSanction = 40 as Integer,
  supervisionStatus = CustodyOrCommunity.COMMUNITY,
  dateAtStartOfFollowup = LocalDate.of(2024, 1, 1),
  totalNumberOfViolentSanctions = 1 as Integer,
)

fun validSNSVDynamicRiskScoreRequest() = RiskScoreRequest(
  version = RiskScoreVersion.V1_0,
  gender = Gender.MALE,
  dateOfBirth = LocalDate.of(1980, 1, 1),
  assessmentDate = LocalDate.of(2025, 1, 1),
  dateOfCurrentConviction = LocalDate.of(2020, 1, 1),
  currentOffenceCode = "02700",
  totalNumberOfSanctionsForAllOffences = 1 as Integer,
  ageAtFirstSanction = 40 as Integer,
  supervisionStatus = CustodyOrCommunity.COMMUNITY,
  dateAtStartOfFollowup = LocalDate.of(2027, 1, 1),
  totalNumberOfViolentSanctions = 1 as Integer,
  didOffenceInvolveCarryingOrUsingWeapon = false,
  suitabilityOfAccommodation = ProblemLevel.NO_PROBLEMS,
  isUnemployed = false,
  currentRelationshipWithPartner = ProblemLevel.NO_PROBLEMS,
  currentAlcoholUseProblems = ProblemLevel.NO_PROBLEMS,
  excessiveAlcoholUse = ProblemLevel.NO_PROBLEMS,
  impulsivityProblems = ProblemLevel.NO_PROBLEMS,
  temperControl = ProblemLevel.NO_PROBLEMS,
  proCriminalAttitudes = ProblemLevel.NO_PROBLEMS,
  domesticAbuse = false,
  previousConvictions = listOf(PreviousConviction.WOUNDING_GBH),
)

fun opdRequestValidated() = OPDRequestValidated(
  overallRiskForAssessment = RiskBand.HIGH,
  highestRiskLevel = RiskBand.HIGH,
  currentOffenceCode = "02700",
  applyOPDOverride = false,
  isEligibleForMappa = false,
  didOffenceInvolveCarryingOrUsingWeapon = false,
  didOffenceInvolveViolenceOrThreatOfViolence = false,
  didOffenceInvolveExcessiveUseOfViolence = false,
  didOffenceInvolveArson = false,
  offenceLinkedRiskOfSeriousHarm = false,
  offenceMotivationEmotionalState = false,
  gender = Gender.MALE,
  accommodationLinkedRiskOfSeriousHarm = false,
  experienceOfChildhood = ProblemLevel.NO_PROBLEMS,
  difficultiesCoping = ProblemLevel.NO_PROBLEMS,
  domesticAbuse = false,
  domesticAbusePartner = null,
  domesticAbuseFamily = null,
  relationshipLinkedSeriousHarm = false,
  currentPsychologicalProblems = ProblemLevel.NO_PROBLEMS,
  wellbeingEmotionalLinkedRiskOfSeriousHarm = false,
  thinkingAndBehaviourLinedToRiskOfSeriousHarm = false,
  custodialSentence = false,
  financialRelianceOnOthers = ProblemLevel.NO_PROBLEMS,
  manipulativePredatoryBehaviour = ProblemLevel.NO_PROBLEMS,
  childhoodBehaviour = false,
  currentPsychiatricProblems = ProblemLevel.NO_PROBLEMS,
  historyOfPsychiatricTreatment = false,
  medicationMentalHealth = false,
  patientSecureUnitOrHospital = false,
  obsessiveBehaviour = false,
  selfHarmSuicideAttempt = false,
  attitudeTowardsSupervision = ProblemLevel.NO_PROBLEMS,
  assaultedOrThreatenedStaff = false,
  escapeOrAbsconded = false,
  controlIssuesOrBreachOfTrust = false,
  doesRecogniseImpactOfOffendingOnOthers = false,
  recklessnessAndRiskTakingBehaviour = ProblemLevel.NO_PROBLEMS,
  impulsivityProblems = ProblemLevel.NO_PROBLEMS,
  ageAtFirstSanction = null,
  hasCurrentPsychiatricTreatment = null,
  controllingOrAggressiveBehaviour = null,
)

fun pniRequest(
  sexualPreoccupation: ProblemLevel? = null,
  offenceRelatedSexualInterests: ProblemLevel? = null,
  emotionalCongruenceWithChildren: ProblemLevel? = null,
  proCriminalAttitudes: ProblemLevel? = null,
  hostileOrientation: ProblemLevel? = null,
  currentRelationshipWithFamilyMembers: ProblemLevel? = null,
  previousCloseRelationships: ProblemLevel? = null,
  easilyInfluencedByCriminalAssociates: ProblemLevel? = null,
  controllingOrAggressiveBehaviour: ProblemLevel? = null,
  impulsivityProblems: ProblemLevel? = null,
  temperControl: ProblemLevel? = null,
  problemSolvingSkills: ProblemLevel? = null,
  difficultiesCoping: ProblemLevel? = null,
  hasEverCommittedSexualOffence: Boolean? = null,
  isARiskOfSexualHarm: Boolean? = null,
) = PNIRequestValidated(
  supervisionStatus = CustodyOrCommunity.COMMUNITY,
  hasEverCommittedSexualOffence = hasEverCommittedSexualOffence,
  isARiskOfSexualHarm = isARiskOfSexualHarm,
  sexualPreoccupation = sexualPreoccupation,
  offenceRelatedSexualInterests = offenceRelatedSexualInterests,
  emotionalCongruenceWithChildren = emotionalCongruenceWithChildren,
  problemSolvingSkills = problemSolvingSkills,
  difficultiesCoping = difficultiesCoping,
  proCriminalAttitudes = proCriminalAttitudes,
  hostileOrientation = hostileOrientation,
  currentRelationshipWithFamilyMembers = currentRelationshipWithFamilyMembers,
  previousCloseRelationships = previousCloseRelationships,
  easilyInfluencedByCriminalAssociates = easilyInfluencedByCriminalAssociates,
  controllingOrAggressiveBehaviour = controllingOrAggressiveBehaviour,
  impulsivityProblems = impulsivityProblems,
  temperControl = temperControl,
  ogrs3TwoYear = null,
  ovpBand = null,
  ospDCBand = null,
  ospIICBand = null,
  saraRiskToPartner = null,
  saraRiskToOthers = null,
  ovp = null,
  rsr = null,
)
