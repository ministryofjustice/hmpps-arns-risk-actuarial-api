package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import java.time.LocalDate
import java.time.Period

fun calculateAge(birthDate: LocalDate, currentDate: LocalDate = LocalDate.now()): Int = Period.between(birthDate, currentDate).years

fun getMstApplicable(gender: Gender, age: Int): Boolean = gender == Gender.MALE && age in 18..24

fun getMaturityFlag(maturityScore: Int): Boolean = maturityScore >= 10
