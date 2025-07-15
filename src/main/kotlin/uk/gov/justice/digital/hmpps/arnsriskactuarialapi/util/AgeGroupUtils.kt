package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.util

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.AgeGroup
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender

fun getAgeGroup(age: Int): AgeGroup {
  return when {
    age < 0 -> throw IllegalArgumentException("Age cannot be negative")
    age < 10 -> throw IllegalArgumentException("Age must be age 10 or more")
    age in 10..11 -> AgeGroup.TEN_TO_UNDER_TWELVE
    age in 12..13 -> AgeGroup.TWELVE_TO_UNDER_FOURTEEN
    age in 14..15 -> AgeGroup.FOURTEEN_TO_UNDER_SIXTEEN
    age in 16..17 -> AgeGroup.SIXTEEN_TO_UNDER_EIGHTEEN
    age in 18..20 -> AgeGroup.EIGHTEEN_TO_UNDER_TWENTY_ONE
    age in 21..24 -> AgeGroup.TWENTY_ONE_TO_UNDER_TWENTY_FIVE
    age in 25..29 -> AgeGroup.TWENTY_FIVE_TO_UNDER_THIRTY
    age in 30..34 -> AgeGroup.THIRTY_TO_UNDER_THIRTY_FIVE
    age in 35..39 -> AgeGroup.THIRTY_FIVE_TO_UNDER_FORTY
    age in 40..49 -> AgeGroup.FORTY_TO_UNDER_FIFTY
    age >= 50 -> AgeGroup.FIFTY_AND_OVER
    else -> throw IllegalArgumentException("Unhandled age: $age")
  }
}

fun getAgeGenderParameter(ageGroup: AgeGroup, gender: Gender): Double {
  if (gender == Gender.MALE) {
    return when (ageGroup) {
      AgeGroup.TEN_TO_UNDER_TWELVE -> 0.0
      AgeGroup.TWELVE_TO_UNDER_FOURTEEN -> 0.08392
      AgeGroup.FOURTEEN_TO_UNDER_SIXTEEN -> 0.07578
      AgeGroup.SIXTEEN_TO_UNDER_EIGHTEEN -> -0.0616
      AgeGroup.EIGHTEEN_TO_UNDER_TWENTY_ONE -> -0.6251
      AgeGroup.TWENTY_ONE_TO_UNDER_TWENTY_FIVE -> -1.0515
      AgeGroup.TWENTY_FIVE_TO_UNDER_THIRTY -> -1.1667
      AgeGroup.THIRTY_TO_UNDER_THIRTY_FIVE -> -1.326
      AgeGroup.THIRTY_FIVE_TO_UNDER_FORTY -> -1.368
      AgeGroup.FORTY_TO_UNDER_FIFTY -> -1.4997
      AgeGroup.FIFTY_AND_OVER -> -2.0253
    }
  } else if (gender == Gender.FEMALE) {
    return when (ageGroup) {
      AgeGroup.TEN_TO_UNDER_TWELVE -> 0.785
      AgeGroup.TWELVE_TO_UNDER_FOURTEEN -> 0.61385
      AgeGroup.FOURTEEN_TO_UNDER_SIXTEEN -> 0.66952
      AgeGroup.SIXTEEN_TO_UNDER_EIGHTEEN -> -0.9592
      AgeGroup.EIGHTEEN_TO_UNDER_TWENTY_ONE -> -0.8975
      AgeGroup.TWENTY_ONE_TO_UNDER_TWENTY_FIVE -> -1.0285
      AgeGroup.TWENTY_FIVE_TO_UNDER_THIRTY -> -1.0528
      AgeGroup.THIRTY_TO_UNDER_THIRTY_FIVE -> -1.1291
      AgeGroup.THIRTY_FIVE_TO_UNDER_FORTY -> -1.4219
      AgeGroup.FORTY_TO_UNDER_FIFTY -> -1.5247
      AgeGroup.FIFTY_AND_OVER -> -2.4498
    }
  } else {
    throw IllegalArgumentException("Unhandled gender: $gender")
  }
}
