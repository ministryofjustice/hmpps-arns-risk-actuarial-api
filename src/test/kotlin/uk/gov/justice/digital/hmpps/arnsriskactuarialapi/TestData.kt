package uk.gov.justice.digital.hmpps.arnsriskactuarialapi

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MSTObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp.OGPObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3.OGRS3Object
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ovp.OVPObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.ProgrammeNeedIdentifier
import java.time.LocalDate

fun emptyOVP(): OVPObject = OVPObject("1_0", null, null, null, null)

fun emptyOGRS3(): OGRS3Object = OGRS3Object("1_0", null, null, null, null)

fun emptyOGP(): OGPObject = OGPObject("1_0", null, null, null, null)

fun emptyMST(): MSTObject = MSTObject("1_0", null, null, null, null)

fun omittedPNI(): PNIObject = PNIObject("1_0", ProgrammeNeedIdentifier.OMISSION, null)

fun emptyContext() = RiskScoreContext()

object RiskScoreRequestTestConstants {
  val NULL_REQUEST = RiskScoreRequest(version = "1_0")
  val FULL_OGP_REQUEST = RiskScoreRequest(
    version = "1_0",
    ogrs3TwoYear = 22 as Integer?,
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
    version = "1_0",
    ogrs3TwoYear = 22 as Integer?,
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
    version = "1_0",
    ogrs3TwoYear = 22 as Integer?,
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
  val OGP_REQUEST_01569 = RiskScoreRequest(
    version = "1_0",
    ogrs3TwoYear = null,
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
  version = "1_0",
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
  version = "1_0",
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
