package uk.gov.justice.digital.hmpps.arnsriskactuarialapi

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MSTObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp.OGPObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3.OGRS3Object
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ovp.OVPObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.ProgrammeNeedIdentifier
import java.time.LocalDate

fun emptyOVP(): OVPObject = OVPObject(null, null, null, null)

fun emptyOGRS3(): OGRS3Object = OGRS3Object(null, null, null, null)

fun emptyOGP(): OGPObject = OGPObject(null, null, null, null, null)

fun emptyMST(): MSTObject = MSTObject(null, null, null, null)

fun omittedPNI(): PNIObject = PNIObject(ProgrammeNeedIdentifier.OMISSION, null)

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
  dateOfBirth = LocalDate.now().minusYears(18),
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
  dateOfBirth = LocalDate.now().minusYears(18),
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
  custody = false,
  community = true,
  saraRiskToPartner = RiskBand.LOW,
  saraRiskToOthers = RiskBand.LOW,
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
) = PNIRequestValidated(
  gender = Gender.MALE,
  community = true,
  hasCommittedSexualOffence = null,
  riskSexualHarm = null,
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
  ovpRiskBand = null,
  ospDCCRiskBand = null,
  ospIICIRiskBand = null,
  ospRiskBand = null,
  rsrRiskBand = null,
  snsvRiskBand = null,
  saraRiskToPartner = null,
  saraRiskToOthers = null,
  ovp = null,
  rsr = null,
  custody = false,
)
