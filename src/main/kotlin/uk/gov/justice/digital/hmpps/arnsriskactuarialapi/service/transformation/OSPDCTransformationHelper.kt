package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.sigmoid
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun getTotalContactAdultSexualSanctionsWeight(sanctions: Int) = when (sanctions) {
  0 -> 0
  1 -> 5
  2 -> 10
  in 3..Int.MAX_VALUE -> 15
  else -> throw IllegalArgumentException("Invalid contact adult sexual sanctions value: $sanctions")
}

fun getTotalContactChildSexualSanctionsWeight(sanctions: Int) = when (sanctions) {
  0 -> 0
  1 -> 3
  2 -> 6
  in 3..Int.MAX_VALUE -> 9
  else -> throw IllegalArgumentException("Invalid contact child sexual sanctions value: $sanctions")
}

fun getTotalNonContactSexualOffencesExcludingIndecentImagesWeight(totalNonContactSexualOffences: Int, totalIndecentImageSanctions: Int): Int {
  val totalNonContactSexualOffencesExcludingIndecentImages = totalNonContactSexualOffences - totalIndecentImageSanctions

  return when (totalNonContactSexualOffencesExcludingIndecentImages) {
    0 -> 0
    1 -> 2
    2 -> 4
    in 3..Int.MAX_VALUE -> 6
    else -> throw IllegalArgumentException("Invalid total non-contact sexual offences excluding indecent images value: $totalNonContactSexualOffencesExcludingIndecentImages")
  }
}

fun getAgeAtStartOfFollowupWeight(dob: LocalDate, dateAtStartOfFollowup: LocalDate): Int {
  val ageAtStartOfFollowup = getAgeAtStartOfFollowup(dob, dateAtStartOfFollowup)

  if (ageAtStartOfFollowup >= 18) {
    return if (ageAtStartOfFollowup >= 63) {
      0
    } else {
      20 - (ageAtStartOfFollowup / 3)
    }
  } else {
    throw IllegalArgumentException("Invalid age at start of follow up value: $ageAtStartOfFollowup")
  }
}

fun getAgeAtLastSanctionForSexualOffenceWeight(dateOfBirth: LocalDate, dateOfMostRecentSexualOffence: LocalDate): Int {
  val ageAtLastSanctionForSexualOffence = ChronoUnit.YEARS.between(dateOfBirth, dateOfMostRecentSexualOffence).toInt()

  return when (ageAtLastSanctionForSexualOffence) {
    in 10..15 -> 0
    in 16..17 -> 5
    in 18..Int.MAX_VALUE -> 10
    else -> throw IllegalArgumentException("Invalid age at last sanction for sexual offence value: $ageAtLastSanctionForSexualOffence")
  }
}

fun getTotalNumberOfSanctionsForAllOffencesWeight(totalNumberOfSanctionsForAllOffences: Int): Int = when (totalNumberOfSanctionsForAllOffences) {
  1 -> 0
  in 2..Int.MAX_VALUE -> 6
  else -> throw IllegalArgumentException("Invalid total number of sanctions value: $totalNumberOfSanctionsForAllOffences")
}

fun getStrangerVictimWeight(strangerVictim: Boolean?): Int = when (strangerVictim) {
  true -> 4
  false -> 0
  null -> 2
}

fun getOSPDCBand(ospdc64PointScore: Int): RiskBand = when (ospdc64PointScore) {
  0 -> RiskBand.NOT_APPLICABLE
  in 1..21 -> RiskBand.LOW
  in 22..29 -> RiskBand.MEDIUM
  in 30..35 -> RiskBand.HIGH
  in 36..64 -> RiskBand.VERY_HIGH
  else -> throw IllegalArgumentException("Invalid OSP/DC 64 point score value: $ospdc64PointScore")
}

fun getOSPDCScore(ospdc64PointScore: Int): Double {
  if (ospdc64PointScore == 0) {
    return 0.0
  }
  val z = -8.6333 + (0.1598 * ospdc64PointScore)
  return z.sigmoid()
}
