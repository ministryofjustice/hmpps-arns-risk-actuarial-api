package uk.gov.justice.digital.hmpps.arnsriskactuarialapi

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CustodyOrCommunity
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.PreviousConviction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
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

fun emptyOPD(): OPDObject = OPDObject(false, null, emptyList())

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
    currentAccommodation = true,
    employmentStatus = false,
    regularOffendingActivities = ProblemLevel.NO_PROBLEMS,
    currentDrugMisuse = ProblemLevel.NO_PROBLEMS,
    motivationDrug = ProblemLevel.NO_PROBLEMS,
    problemSolvingSkills = ProblemLevel.NO_PROBLEMS,
    awarenessOfConsequences = ProblemLevel.NO_PROBLEMS,
    understandsPeoplesViews = ProblemLevel.NO_PROBLEMS,
    proCriminalAttitudes = ProblemLevel.SOME_PROBLEMS,
  )
  val ALT_NULL_OGP_REQUEST = RiskScoreRequest(
    version = RiskScoreVersion.V1_0,
    currentAccommodation = null,
    employmentStatus = false,
    regularOffendingActivities = null,
    currentDrugMisuse = ProblemLevel.NO_PROBLEMS,
    motivationDrug = null,
    problemSolvingSkills = ProblemLevel.NO_PROBLEMS,
    awarenessOfConsequences = null,
    understandsPeoplesViews = ProblemLevel.NO_PROBLEMS,
    proCriminalAttitudes = null,
  )
  val OGP_REQUEST_39 = RiskScoreRequest(
    version = RiskScoreVersion.V1_0,
    currentAccommodation = true,
    employmentStatus = false,
    regularOffendingActivities = null,
    currentDrugMisuse = ProblemLevel.NO_PROBLEMS,
    motivationDrug = ProblemLevel.NO_PROBLEMS,
    problemSolvingSkills = ProblemLevel.NO_PROBLEMS,
    awarenessOfConsequences = ProblemLevel.NO_PROBLEMS,
    understandsPeoplesViews = ProblemLevel.NO_PROBLEMS,
    proCriminalAttitudes = null,
  )
  val OGP_REQUEST_0458 = RiskScoreRequest(
    version = RiskScoreVersion.V1_0,
    currentAccommodation = null,
    employmentStatus = false,
    regularOffendingActivities = ProblemLevel.NO_PROBLEMS,
    currentDrugMisuse = ProblemLevel.NO_PROBLEMS,
    motivationDrug = null,
    problemSolvingSkills = null,
    awarenessOfConsequences = ProblemLevel.NO_PROBLEMS,
    understandsPeoplesViews = ProblemLevel.NO_PROBLEMS,
    proCriminalAttitudes = null,
  )
  val FULL_LDS_REQUEST = RiskScoreRequest(
    version = RiskScoreVersion.V1_0,
    currentAccommodation = true,
    transferableSkills = ProblemLevel.SOME_PROBLEMS,
    educationDifficulties = ProblemLevel.SOME_PROBLEMS,
    readingDifficulties = true,
    numeracyDifficulties = false,
    learningDifficulties = ProblemLevel.SOME_PROBLEMS,
    professionalOrVocationalQualifications = HasQualifications.ANY_QUALIFICATION,
  )
  val INELIGIBLE_LDS_REQUEST = RiskScoreRequest(
    version = RiskScoreVersion.V1_0,
    currentAccommodation = true,
    transferableSkills = ProblemLevel.SOME_PROBLEMS,
    educationDifficulties = null,
    readingDifficulties = true,
    numeracyDifficulties = false,
    learningDifficulties = null,
    professionalOrVocationalQualifications = HasQualifications.ANY_QUALIFICATION,
  )
  val BAD_READING_DIFFICULTY_LDS_REQUEST = RiskScoreRequest(
    version = RiskScoreVersion.V1_0,
    currentAccommodation = true,
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
  totalNumberOfSanctions = 30 as Integer?,
  totalNumberOfViolentSanctions = 10 as Integer?,
  impactOfOffendingOnOthers = true,
  currentAccommodation = true,
  ageAtFirstSanction = null,
  currentOffence = null,
  employmentStatus = true,
  alcoholIsCurrentUseAProblem = ProblemLevel.NO_PROBLEMS,
  alcoholExcessive6Months = ProblemLevel.NO_PROBLEMS,
  currentPsychiatricTreatmentOrPending = false,
  temperControl = ProblemLevel.NO_PROBLEMS,
  proCriminalAttitudes = ProblemLevel.NO_PROBLEMS,
)

fun validMSTRiskScoreRequest(): RiskScoreRequest = RiskScoreRequest(
  version = RiskScoreVersion.V1_0,
  gender = Gender.MALE,
  dateOfBirth = FIXED_TEST_DATE.minusYears(18),
  assessmentDate = FIXED_TEST_DATE,
  peerGroupInfluences = true,
  attitudesPeerPressure = ProblemLevel.SOME_PROBLEMS,
  attitudesStableBehaviour = ProblemLevel.SOME_PROBLEMS,
  difficultiesCoping = ProblemLevel.SOME_PROBLEMS,
  attitudesTowardsSelf = ProblemLevel.SOME_PROBLEMS,
  impulsivityBehaviour = ProblemLevel.SOME_PROBLEMS,
  temperControl = ProblemLevel.SOME_PROBLEMS,
  problemSolvingSkills = ProblemLevel.SOME_PROBLEMS,
  awarenessOfConsequences = ProblemLevel.SOME_PROBLEMS,
  understandsPeoplesViews = ProblemLevel.SOME_PROBLEMS,
)

fun validPNIRiskScoreRequest(): RiskScoreRequest = RiskScoreRequest(
  version = RiskScoreVersion.V1_0,
  gender = Gender.MALE,
  assessmentDate = FIXED_TEST_DATE,
  dateOfBirth = FIXED_TEST_DATE.minusYears(18),
  peerGroupInfluences = true,
  attitudesPeerPressure = ProblemLevel.SOME_PROBLEMS,
  attitudesStableBehaviour = ProblemLevel.SOME_PROBLEMS,
  difficultiesCoping = ProblemLevel.SOME_PROBLEMS,
  attitudesTowardsSelf = ProblemLevel.SOME_PROBLEMS,
  impulsivityBehaviour = ProblemLevel.SOME_PROBLEMS,
  temperControl = ProblemLevel.SOME_PROBLEMS,
  problemSolvingSkills = ProblemLevel.SOME_PROBLEMS,
  awarenessOfConsequences = ProblemLevel.SOME_PROBLEMS,
  understandsPeoplesViews = ProblemLevel.SOME_PROBLEMS,
  sexualPreoccupation = ProblemLevel.SOME_PROBLEMS,
  sexualInterestsOffenceRelated = ProblemLevel.SOME_PROBLEMS,
  emotionalCongruence = ProblemLevel.SOME_PROBLEMS,
  proCriminalAttitudes = ProblemLevel.SOME_PROBLEMS,
  hostileOrientation = ProblemLevel.SOME_PROBLEMS,
  currentRelationshipFamilyMembers = ProblemLevel.SOME_PROBLEMS,
  previousCloseRelationships = ProblemLevel.SOME_PROBLEMS,
  easilyInfluencedByCriminals = ProblemLevel.SOME_PROBLEMS,
  controllingBehaviour = ProblemLevel.SOME_PROBLEMS,
  inCustodyOrCommunity = CustodyOrCommunity.COMMUNITY,
  saraRiskToPartner = RiskBand.LOW,
  saraRiskToOthers = RiskBand.LOW,
)

fun validOPDRiskScoreRequest() = RiskScoreRequest(
  version = RiskScoreVersion.V1_0,
  overallRiskForAssessment = RiskBand.HIGH,
  highestRiskLevel = RiskBand.HIGH,
  currentOffence = "02700",
  opdOverride = false,
  eligibleForMappa = false,
  carryingOrUsingWeapon = false,
  violenceOrThreatOfViolence = false,
  excessiveOrSadisticViolence = false,
  offenceArson = false,
  offenceLinkedRiskOfSeriousHarm = false,
  offenderMotivations = false,
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
  impactOfOffendingOnOthers = false,
  attitudesStableBehaviour = ProblemLevel.NO_PROBLEMS,
  impulsivityBehaviour = ProblemLevel.NO_PROBLEMS,
)

fun validOSPDCRiskScoreRequest() = RiskScoreRequest(
  version = RiskScoreVersion.V1_0,
  gender = Gender.MALE,
  dateOfBirth = LocalDate.of(1980, 1, 1),
  hasCommittedSexualOffence = true,
  totalContactAdultSexualSanctions = 1,
  totalContactChildSexualSanctions = 1,
  totalNonContactSexualOffences = 1,
  totalIndecentImageSanctions = 1,
  dateAtStartOfFollowup = LocalDate.of(2025, 1, 1),
  dateOfMostRecentSexualOffence = LocalDate.of(2000, 1, 1),
  totalNumberOfSanctions = 4 as Integer,
)

fun validSNSVStaticRiskScoreRequest() = RiskScoreRequest(
  version = RiskScoreVersion.V1_0,
  gender = Gender.MALE,
  dateOfBirth = LocalDate.of(1980, 1, 1),
  assessmentDate = LocalDate.of(2025, 1, 1),
  dateOfCurrentConviction = LocalDate.of(2020, 1, 1),
  currentOffence = "02700",
  totalNumberOfSanctions = 1 as Integer,
  ageAtFirstSanction = 40 as Integer,
  inCustodyOrCommunity = CustodyOrCommunity.COMMUNITY,
  dateAtStartOfFollowup = LocalDate.of(2024, 1, 1),
  totalNumberOfViolentSanctions = 1 as Integer,
)

fun validSNSVDynamicRiskScoreRequest() = RiskScoreRequest(
  version = RiskScoreVersion.V1_0,
  gender = Gender.MALE,
  dateOfBirth = LocalDate.of(1980, 1, 1),
  assessmentDate = LocalDate.of(2025, 1, 1),
  dateOfCurrentConviction = LocalDate.of(2020, 1, 1),
  currentOffence = "02700",
  totalNumberOfSanctions = 1 as Integer,
  ageAtFirstSanction = 40 as Integer,
  inCustodyOrCommunity = CustodyOrCommunity.COMMUNITY,
  dateAtStartOfFollowup = LocalDate.of(2027, 1, 1),
  totalNumberOfViolentSanctions = 1 as Integer,
  carryingOrUsingWeapon = false,
  suitabilityOfAccommodation = ProblemLevel.NO_PROBLEMS,
  employmentStatus = false,
  currentRelationshipWithPartner = ProblemLevel.NO_PROBLEMS,
  alcoholIsCurrentUseAProblem = ProblemLevel.NO_PROBLEMS,
  alcoholExcessive6Months = ProblemLevel.NO_PROBLEMS,
  impulsivityBehaviour = ProblemLevel.NO_PROBLEMS,
  temperControl = ProblemLevel.NO_PROBLEMS,
  proCriminalAttitudes = ProblemLevel.NO_PROBLEMS,
  domesticAbuse = false,
  previousConvictions = listOf(PreviousConviction.WOUNDING_GBH),
)

fun opdRequestValidated() = OPDRequestValidated(
  overallRiskForAssessment = RiskBand.HIGH,
  highestRiskLevel = RiskBand.HIGH,
  currentOffence = "02700",
  opdOverride = false,
  eligibleForMappa = false,
  carryingOrUsingWeapon = false,
  violenceOrThreatOfViolence = false,
  excessiveOrSadisticViolence = false,
  offenceArson = false,
  offenceLinkedRiskOfSeriousHarm = false,
  offenderMotivations = false,
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
  impactOfOffendingOnOthers = false,
  attitudesStableBehaviour = ProblemLevel.NO_PROBLEMS,
  impulsivityBehaviour = ProblemLevel.NO_PROBLEMS,
  ageAtFirstSanction = null,
  currentPsychiatricTreatmentOrPending = null,
  controllingBehaviour = null,
)

fun pniRequest(
  sexualPreoccupation: ProblemLevel? = null,
  sexualInterestsOffenceRelated: ProblemLevel? = null,
  emotionalCongruence: ProblemLevel? = null,
  proCriminalAttitudes: ProblemLevel? = null,
  hostileOrientation: ProblemLevel? = null,
  currentRelationshipFamilyMembers: ProblemLevel? = null,
  previousCloseRelationships: ProblemLevel? = null,
  easilyInfluencedByCriminals: ProblemLevel? = null,
  controllingBehaviour: ProblemLevel? = null,
  impulsivityBehaviour: ProblemLevel? = null,
  temperControl: ProblemLevel? = null,
  problemSolvingSkills: ProblemLevel? = null,
  difficultiesCoping: ProblemLevel? = null,
  hasCommittedSexualOffence: Boolean? = null,
  riskSexualHarm: Boolean? = null,
) = PNIRequestValidated(
  inCustodyOrCommunity = CustodyOrCommunity.COMMUNITY,
  hasCommittedSexualOffence = hasCommittedSexualOffence,
  riskSexualHarm = riskSexualHarm,
  sexualPreoccupation = sexualPreoccupation,
  sexualInterestsOffenceRelated = sexualInterestsOffenceRelated,
  emotionalCongruence = emotionalCongruence,
  problemSolvingSkills = problemSolvingSkills,
  difficultiesCoping = difficultiesCoping,
  proCriminalAttitudes = proCriminalAttitudes,
  hostileOrientation = hostileOrientation,
  currentRelationshipFamilyMembers = currentRelationshipFamilyMembers,
  previousCloseRelationships = previousCloseRelationships,
  easilyInfluencedByCriminals = easilyInfluencedByCriminals,
  controllingBehaviour = controllingBehaviour,
  impulsivityBehaviour = impulsivityBehaviour,
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
