package uk.gov.justice.digital.hmpps.arnsriskactuarialapi

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CurrentRelationshipStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.MotivationLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.PreviousConviction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.SupervisionStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.YesSometimesNo
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.allreoffendingpredictor.AllReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.imagesandIndirectcontactsexualreoffendingpredictor.ImagesAndIndirectContactSexualReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.HasQualifications
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.LDSObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MSTObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd.OPDObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd.OPDRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.osp.OSPDCObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.ProgrammeNeedIdentifier
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.rsr.RSRObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.seriousviolentreoffendingpredictor.SeriousViolentReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.violentreoffendingpredictor.ViolentReoffendingPredictorObject
import java.time.LocalDate
import kotlin.collections.emptyList

fun emptyViolentReoffendingPredictor(): ViolentReoffendingPredictorObject = ViolentReoffendingPredictorObject(null, null, null, null, null)

fun emptyAllReoffendingPredictor(): AllReoffendingPredictorObject = AllReoffendingPredictorObject(null, null, null, null, null)

fun emptyMST(): MSTObject = MSTObject(null, null, null, null)

fun emptyOPD(): OPDObject = OPDObject(false, null, null, emptyList())

fun omittedPNI(): PNIObject = PNIObject(ProgrammeNeedIdentifier.OMISSION, null)

fun emptyLDS(): LDSObject = LDSObject(null, null)

fun emptyOSPDC(): OSPDCObject = OSPDCObject(null, null, null, null, null, null, null, null)

fun emptySeriousViolentReoffendingPredictor(): SeriousViolentReoffendingPredictorObject = SeriousViolentReoffendingPredictorObject(null, null, null, null, null)

fun emptyRSR(): RSRObject = RSRObject(null, null, null, null, null, null, null, null, null, null, null)

fun emptyImagesAndIndirectContactSexualReoffendingPredictor(): ImagesAndIndirectContactSexualReoffendingPredictorObject = ImagesAndIndirectContactSexualReoffendingPredictorObject(null, null, null, null, null, null, null)

fun emptyContext() = RiskScoreContext(version = RiskScoreVersion.V1_0)

fun lowAllReoffendingPredictor(staticOrDynamic: StaticOrDynamic = StaticOrDynamic.STATIC) = AllReoffendingPredictorObject(
  10.0,
  RiskBand.LOW,
  staticOrDynamic,
  emptyList(),
  emptyMap(),
)

fun highAllReoffendingPredictor(staticOrDynamic: StaticOrDynamic = StaticOrDynamic.STATIC) = AllReoffendingPredictorObject(
  90.0,
  RiskBand.VERY_HIGH,
  staticOrDynamic,
  emptyList(),
  emptyMap(),
)

fun highViolentReoffendingPredictor(staticOrDynamic: StaticOrDynamic = StaticOrDynamic.STATIC) = ViolentReoffendingPredictorObject(
  77.0,
  RiskBand.VERY_HIGH,
  staticOrDynamic,
  emptyList(),
  emptyMap(),
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
  val FULL_LDS_REQUEST = RiskScoreRequest(
    version = RiskScoreVersion.V1_0,
    isCurrentlyOfNoFixedAbodeOrTransientAccommodation = true,
    workRelatedSkills = ProblemLevel.SOME_PROBLEMS,
    problemsWithReadingWritingNumeracy = ProblemLevel.SOME_PROBLEMS,
    hasProblemsWithReading = true,
    hasProblemsWithNumeracy = false,
    learningDifficulties = ProblemLevel.SOME_PROBLEMS,
    professionalOrVocationalQualifications = HasQualifications.ANY_QUALIFICATION,
  )
  val INELIGIBLE_LDS_REQUEST = RiskScoreRequest(
    version = RiskScoreVersion.V1_0,
    isCurrentlyOfNoFixedAbodeOrTransientAccommodation = true,
    workRelatedSkills = ProblemLevel.SOME_PROBLEMS,
    problemsWithReadingWritingNumeracy = null,
    hasProblemsWithReading = true,
    hasProblemsWithNumeracy = false,
    learningDifficulties = null,
    professionalOrVocationalQualifications = HasQualifications.ANY_QUALIFICATION,
  )
  val BAD_READING_DIFFICULTY_LDS_REQUEST = RiskScoreRequest(
    version = RiskScoreVersion.V1_0,
    isCurrentlyOfNoFixedAbodeOrTransientAccommodation = true,
    workRelatedSkills = ProblemLevel.SOME_PROBLEMS,
    problemsWithReadingWritingNumeracy = null,
    hasProblemsWithReading = true,
    hasProblemsWithNumeracy = null,
    learningDifficulties = ProblemLevel.SOME_PROBLEMS,
    professionalOrVocationalQualifications = HasQualifications.ANY_QUALIFICATION,
  )
}

fun validAllReoffendingPredictorStaticRiskScoreRequest(): RiskScoreRequest = RiskScoreRequest(
  assessmentDate = LocalDate.of(2025, 1, 1),
  dateOfBirth = LocalDate.of(1990, 1, 1),
  dateOfCurrentConviction = LocalDate.of(2024, 1, 1),
  ageAtFirstSanction = 18,
  gender = Gender.MALE,
  currentOffenceCode = "00001",
  totalNumberOfSanctionsForAllOffences = 2,
  dateAtStartOfFollowupCalculated = LocalDate.of(2026, 1, 1),
)

fun validMinimumAllReoffendingPredictorStaticRiskScoreRequest(): RiskScoreRequest = RiskScoreRequest(
  assessmentDate = LocalDate.of(2025, 1, 1),
  dateOfBirth = LocalDate.of(1990, 1, 1),
  dateOfCurrentConviction = LocalDate.of(2024, 1, 1),
  ageAtFirstSanction = 18,
  gender = Gender.MALE,
  currentOffenceCode = "00001",
  totalNumberOfSanctionsForAllOffences = 2,
)

fun validAllReoffendingPredictorDynamicRiskScoreRequest(): RiskScoreRequest = RiskScoreRequest(
  assessmentDate = LocalDate.of(2025, 1, 1),
  dateOfBirth = LocalDate.of(1990, 1, 1),
  dateOfCurrentConviction = LocalDate.of(2024, 1, 1),
  ageAtFirstSanction = 18,
  gender = Gender.MALE,
  currentOffenceCode = "00001",
  totalNumberOfSanctionsForAllOffences = 2,
  dateAtStartOfFollowupCalculated = LocalDate.of(2026, 1, 1),
  suitabilityOfAccommodation = ProblemLevel.SOME_PROBLEMS,
  isUnemployed = true,
  currentRelationshipWithPartner = ProblemLevel.SOME_PROBLEMS,
  evidenceOfDomesticAbuse = false,
  currentRelationshipStatus = CurrentRelationshipStatus.IN_RELATIONSHIP_NOT_LIVING_TOGETHER,
  regularOffendingActivities = ProblemLevel.SOME_PROBLEMS,
  motivationToTackleDrugMisuse = MotivationLevel.PARTIAL_MOTIVATION,
  hasHeroinUsage = false,
  hasOtherOpiateUsage = false,
  hasCrackCocaineUsage = false,
  hasPowderCocaineUsage = false,
  hasMisusedPrescriptionDrugUsage = false,
  hasBenzodiazepinesUsage = false,
  hasCannabisUsage = true,
  hasSteroidsUsage = true,
  hasOtherDrugsUsage = false,
  hasKetamineUsage = false,
  hasSpiceUsage = false,
  hasHallucinogensUsage = false,
  hasSolventsUsage = false,
  currentAlcoholUseProblems = ProblemLevel.SOME_PROBLEMS,
  excessiveAlcoholUse = ProblemLevel.SOME_PROBLEMS,
  impulsivityProblems = ProblemLevel.SOME_PROBLEMS,
  proCriminalAttitudes = ProblemLevel.SOME_PROBLEMS,
)

fun validSeriousViolentReoffendingPredictorStaticRiskScoreRequest(): RiskScoreRequest = RiskScoreRequest(
  assessmentDate = LocalDate.of(2025, 1, 1),
  dateOfBirth = LocalDate.of(1990, 1, 1),
  dateOfCurrentConviction = LocalDate.of(2024, 1, 1),
  ageAtFirstSanction = 18,
  gender = Gender.MALE,
  currentOffenceCode = "00001",
  totalNumberOfSanctionsForAllOffences = 2,
  totalNumberOfViolentSanctions = 2,
  dateAtStartOfFollowupCalculated = LocalDate.of(2026, 1, 1),
)

fun validSeriousViolentReoffendingPredictorDynamicRiskScoreRequest(): RiskScoreRequest = RiskScoreRequest(
  assessmentDate = LocalDate.of(2025, 1, 1),
  dateOfBirth = LocalDate.of(1990, 1, 1),
  dateOfCurrentConviction = LocalDate.of(2024, 1, 1),
  ageAtFirstSanction = 18,
  gender = Gender.MALE,
  currentOffenceCode = "00001",
  totalNumberOfSanctionsForAllOffences = 2,
  totalNumberOfViolentSanctions = 2,
  dateAtStartOfFollowupCalculated = LocalDate.of(2026, 1, 1),
  didOffenceInvolveCarryingOrUsingWeapon = true,
  suitabilityOfAccommodation = ProblemLevel.SOME_PROBLEMS,
  isUnemployed = true,
  currentAlcoholUseProblems = ProblemLevel.SOME_PROBLEMS,
  temperControl = ProblemLevel.SOME_PROBLEMS,
  proCriminalAttitudes = ProblemLevel.SOME_PROBLEMS,
  previousConvictions = listOf(PreviousConviction.FIREARMS),
)

fun validViolentReoffendingPredictorStaticRiskScoreRequest(): RiskScoreRequest = RiskScoreRequest(
  assessmentDate = LocalDate.of(2025, 1, 1),
  dateOfBirth = LocalDate.of(1990, 1, 1),
  dateOfCurrentConviction = LocalDate.of(2024, 1, 1),
  ageAtFirstSanction = 18,
  gender = Gender.MALE,
  currentOffenceCode = "00001",
  totalNumberOfSanctionsForAllOffences = 2,
  totalNumberOfViolentSanctions = 2,
  dateAtStartOfFollowupCalculated = LocalDate.of(2026, 1, 1),
)

fun validViolentReoffendingPredictorDynamicRiskScoreRequest(): RiskScoreRequest = RiskScoreRequest(
  assessmentDate = LocalDate.of(2025, 1, 1),
  dateOfBirth = LocalDate.of(1990, 1, 1),
  dateOfCurrentConviction = LocalDate.of(2024, 1, 1),
  ageAtFirstSanction = 18,
  gender = Gender.MALE,
  currentOffenceCode = "00001",
  totalNumberOfSanctionsForAllOffences = 2,
  totalNumberOfViolentSanctions = 2,
  dateAtStartOfFollowupCalculated = LocalDate.of(2026, 1, 1),
  suitabilityOfAccommodation = ProblemLevel.SOME_PROBLEMS,
  isUnemployed = true,
  currentRelationshipWithPartner = ProblemLevel.SOME_PROBLEMS,
  evidenceOfDomesticAbuse = false,
  currentRelationshipStatus = CurrentRelationshipStatus.IN_RELATIONSHIP_NOT_LIVING_TOGETHER,
  regularOffendingActivities = ProblemLevel.SOME_PROBLEMS,
  motivationToTackleDrugMisuse = MotivationLevel.PARTIAL_MOTIVATION,
  hasHeroinUsage = false,
  hasOtherOpiateUsage = false,
  hasCrackCocaineUsage = false,
  hasPowderCocaineUsage = false,
  hasMisusedPrescriptionDrugUsage = false,
  hasBenzodiazepinesUsage = false,
  hasCannabisUsage = true,
  hasSteroidsUsage = true,
  hasOtherDrugsUsage = false,
  hasKetamineUsage = false,
  hasSpiceUsage = false,
  hasHallucinogensUsage = false,
  hasSolventsUsage = false,
  hasMethadoneUsage = true,
  currentAlcoholUseProblems = ProblemLevel.SOME_PROBLEMS,
  excessiveAlcoholUse = ProblemLevel.SOME_PROBLEMS,
  impulsivityProblems = ProblemLevel.SOME_PROBLEMS,
  temperControl = ProblemLevel.SOME_PROBLEMS,
)

fun validImageAndIndirectContactPredictorStaticRiskScoreRequest(): RiskScoreRequest = RiskScoreRequest(
  gender = Gender.MALE,
  hasEverCommittedSexualOffence = true,
  totalContactAdultSexualSanctions = 1,
  totalContactChildSexualSanctions = 1,
  totalNonContactSexualOffences = 1,
  totalIndecentImageSanctions = 1,
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
  supervisionStatus = SupervisionStatus.COMMUNITY,
  saraRiskToPartner = RiskBand.LOW,
  saraRiskToOthers = RiskBand.LOW,
)

fun validOPDRiskScoreRequest() = RiskScoreRequest(
  version = RiskScoreVersion.V1_0,
  overallRiskForAssessment = RiskBand.HIGH,
  highestRiskLevelOverAllAssessments = RiskBand.HIGH,
  currentOffenceCode = "02700",
  applyOPDOverride = false,
  isEligibleForMappa = false,
  didOffenceInvolveCarryingOrUsingWeapon = false,
  didOffenceInvolveViolenceOrThreatOfViolence = false,
  didOffenceInvolveExcessiveUseOfViolence = false,
  didOffenceInvolveArson = false,
  isAnalysisOfOffenceIssuesLinkedToRisk = false,
  offenceMotivationEmotionalState = false,
  gender = Gender.MALE,
  hasAccommodationIssuesLinkedToRisk = false,
  experienceOfChildhood = ProblemLevel.NO_PROBLEMS,
  difficultiesCoping = ProblemLevel.NO_PROBLEMS,
  evidenceOfDomesticAbuse = false,
  domesticAbuseAgainstPartner = null,
  domesticAbuseAgainstFamily = null,
  relationshipIssuesLinkedToRisk = false,
  currentPsychologicalProblems = ProblemLevel.NO_PROBLEMS,
  areEmotionalIssuesLinkedToRisk = false,
  areThinkingAndBehaviourIssuesLinkedToRisk = false,
  hasCustodialSentence = false,
  overRelianceOnOthersForFinancialSupport = ProblemLevel.NO_PROBLEMS,
  manipulativeOrPredatoryBehaviour = ProblemLevel.NO_PROBLEMS,
  isEvidenceOfChildhoodBehaviouralProblems = false,
  currentPsychiatricProblems = ProblemLevel.NO_PROBLEMS,
  hasHistoryOfPsychiatricTreatment = false,
  hasBeenOnMedicationForMentalHealthProblems = false,
  hasEverBeenInSpecialHospitalOrRegionalSecureUnit = false,
  hasDisplayedObsessiveBehaviourLinkedToOffending = false,
  hasSelfHarmOrAttemptedSuicide = false,
  attitudeTowardsSupervisionOrLicence = ProblemLevel.NO_PROBLEMS,
  hasAssaultedOrThreatenedStaff = false,
  hasEscapedOrAbsconded = false,
  hasControlIssues = false,
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
  dateAtStartOfFollowupUserInput = LocalDate.of(2025, 1, 1),
  dateOfMostRecentSexualOffence = LocalDate.of(2000, 1, 1),
  totalNumberOfSanctionsForAllOffences = 4,
  isCurrentOffenceSexuallyMotivated = false,
  supervisionStatus = SupervisionStatus.CUSTODY,
)

fun opdRequestValidated() = OPDRequestValidated(
  overallRiskForAssessment = RiskBand.HIGH,
  highestRiskLevelOverAllAssessments = RiskBand.HIGH,
  currentOffenceCode = "02700",
  applyOPDOverride = false,
  isEligibleForMappa = false,
  didOffenceInvolveCarryingOrUsingWeapon = false,
  didOffenceInvolveViolenceOrThreatOfViolence = false,
  didOffenceInvolveExcessiveUseOfViolence = false,
  didOffenceInvolveArson = false,
  isAnalysisOfOffenceIssuesLinkedToRisk = false,
  offenceMotivationEmotionalState = false,
  gender = Gender.MALE,
  hasAccommodationIssuesLinkedToRisk = false,
  experienceOfChildhood = ProblemLevel.NO_PROBLEMS,
  difficultiesCoping = ProblemLevel.NO_PROBLEMS,
  evidenceOfDomesticAbuse = false,
  domesticAbuseAgainstPartner = null,
  domesticAbuseAgainstFamily = null,
  relationshipIssuesLinkedToRisk = false,
  currentPsychologicalProblems = ProblemLevel.NO_PROBLEMS,
  areEmotionalIssuesLinkedToRisk = false,
  areThinkingAndBehaviourIssuesLinkedToRisk = false,
  hasCustodialSentence = false,
  overRelianceOnOthersForFinancialSupport = ProblemLevel.NO_PROBLEMS,
  manipulativeOrPredatoryBehaviour = ProblemLevel.NO_PROBLEMS,
  isEvidenceOfChildhoodBehaviouralProblems = false,
  currentPsychiatricProblems = ProblemLevel.NO_PROBLEMS,
  hasHistoryOfPsychiatricTreatment = false,
  hasBeenOnMedicationForMentalHealthProblems = false,
  hasEverBeenInSpecialHospitalOrRegionalSecureUnit = false,
  hasDisplayedObsessiveBehaviourLinkedToOffending = false,
  hasSelfHarmOrAttemptedSuicide = false,
  attitudeTowardsSupervisionOrLicence = ProblemLevel.NO_PROBLEMS,
  hasAssaultedOrThreatenedStaff = false,
  hasEscapedOrAbsconded = false,
  hasControlIssues = false,
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
  supervisionStatus = SupervisionStatus.COMMUNITY,
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
  allReoffendingPredictorStaticScore = null,
  violentReoffendingPredictorStaticScore = null,
  violentReoffendingPredictorBand = null,
  ospDCBand = null,
  imagesAndIndirectContactSexualReoffendingPredictorBand = null,
  saraRiskToPartner = null,
  saraRiskToOthers = null,
  rsr = null,
)
