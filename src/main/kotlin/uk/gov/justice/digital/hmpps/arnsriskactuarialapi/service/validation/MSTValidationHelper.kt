package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isValidMstAge
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isValidMstGender

const val MIN_MST_ANSWERS_SIZE = 9

fun mstInitialValidation(request: RiskScoreRequest): MutableList<ValidationErrorResponse> {
  val errors = mutableListOf<ValidationErrorResponse>()

  val missingFields = mutableListOf<String>()

  if (request.gender == null) missingFields.add("Gender")
  if (request.dateOfBirth == null) missingFields.add("Date of birth")
  val answersCount = listOfNotNull(
    request.peerGroupInfluences,
    request.attitudesPeerPressure,
    request.attitudesStableBehaviour,
    request.difficultiesCoping,
    request.attitudesTowardsSelf,
    request.impulsivityBehaviour,
    request.temperControl,
    request.problemSolvingSkills,
    request.awarenessOfConsequences,
    request.understandsPeoplesViews,
  ).size

  if (answersCount < MIN_MST_ANSWERS_SIZE) {
    if (request.peerGroupInfluences == null) missingFields.add("Peer group influences")
    if (request.attitudesPeerPressure == null) missingFields.add("Attitudes peer pressure")
    if (request.attitudesStableBehaviour == null) missingFields.add("Attitudes stable behaviour")
    if (request.difficultiesCoping == null) missingFields.add("Difficulties coping")
    if (request.attitudesTowardsSelf == null) missingFields.add("Attitudes towards self")
    if (request.impulsivityBehaviour == null) missingFields.add("Impulsivity behaviour")
    if (request.temperControl == null) missingFields.add("Temper control")
    if (request.problemSolvingSkills == null) missingFields.add("Problem solving skills")
    if (request.awarenessOfConsequences == null) missingFields.add("Awareness of consequences")
    if (request.understandsPeoplesViews == null) missingFields.add("Understands peoples views")
  }
  return addMissingFields(missingFields, errors)
}

fun genderAndAgeValidation(
  gender: Gender,
  age: Int,
  errors: MutableList<ValidationErrorResponse>,
): MutableList<ValidationErrorResponse> {
  val criteriaFields = mutableListOf<String>()

  if (!isValidMstGender(gender)) criteriaFields.add("Gender")
  if (!isValidMstAge(age)) criteriaFields.add("Date of birth")

  return addMissingCriteriaValidation(criteriaFields, errors)
}
