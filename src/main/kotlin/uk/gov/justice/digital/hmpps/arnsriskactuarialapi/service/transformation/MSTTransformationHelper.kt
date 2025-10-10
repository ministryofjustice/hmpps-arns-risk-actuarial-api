package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import java.time.LocalDate
import java.time.Period

fun calculateAge(birthDate: LocalDate, assessmentDate: LocalDate): Int = Period.between(birthDate, assessmentDate).years

fun getMaturityFlag(maturityScore: Int): Boolean = maturityScore >= 10
