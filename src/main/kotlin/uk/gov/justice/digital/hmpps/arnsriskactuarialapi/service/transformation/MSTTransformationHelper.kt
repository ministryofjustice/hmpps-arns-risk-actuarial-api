package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import java.time.LocalDate
import java.time.Period

fun roundedAge(birthDate: LocalDate, assessmentDate: LocalDate): Int {
  val dobTruncated = birthDate.withDayOfMonth(1)
  val todayTruncated = assessmentDate.withDayOfMonth(1)
  return Period.between(dobTruncated, todayTruncated).years
}

fun getMstApplicable(gender: Gender, age: Int): Boolean = isValidMstGender(gender) && isValidMstAge(age)

fun isValidMstAge(age: Int): Boolean = age in 18..25

fun isValidMstGender(gender: Gender): Boolean = gender == Gender.MALE

fun getMaturityFlag(maturityScore: Int): Boolean = maturityScore >= 10
