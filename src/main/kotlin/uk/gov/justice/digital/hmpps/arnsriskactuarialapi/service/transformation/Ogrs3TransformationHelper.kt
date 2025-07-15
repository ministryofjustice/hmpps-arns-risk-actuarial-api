package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import java.time.LocalDate
import java.time.Period

private const val MIN_CONVICTION_AGE = 10

fun ageAtCurrentConviction(
  dateOfBirth: LocalDate,
  dateOfCurrentConviction: LocalDate?, // can this actually be null ?
  ageAtFirstSanction: Int,
): Result<Int> {
  return runCatching {
    if (dateOfCurrentConviction == null) {
      throw IllegalArgumentException("conviction date is null.")
    }

    if (dateOfCurrentConviction.isBefore(dateOfBirth)) {
      throw IllegalArgumentException("Conviction date cannot be before date of birth.")
    }

    val ageAtCurrentConviction = Period.between(dateOfBirth, dateOfCurrentConviction).years

    if (ageAtCurrentConviction < MIN_CONVICTION_AGE) {
      throw IllegalArgumentException("Age at current conviction must be at least $MIN_CONVICTION_AGE.")
    }

    if (ageAtFirstSanction > ageAtCurrentConviction) {
      throw IllegalArgumentException("Age at first sanction cannot be greater than age at current conviction.")
    }

    ageAtCurrentConviction
  }
}