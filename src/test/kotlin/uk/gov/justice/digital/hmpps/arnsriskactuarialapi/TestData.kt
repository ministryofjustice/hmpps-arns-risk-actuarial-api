package uk.gov.justice.digital.hmpps.arnsriskactuarialapi

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3.OGRS3Object
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ovp.OVPObject
import java.time.LocalDate

fun emptyOVP(): OVPObject = OVPObject("1_0", null, null, null, null)

fun emptyOGRS3(): OGRS3Object = OGRS3Object("1_0", null, null, null, null)

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
