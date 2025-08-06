package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import java.time.LocalDate
import java.util.stream.Stream

class OSPDCTransformationHelperTest {

  @ParameterizedTest
  @MethodSource("getTotalContactAdultSexualSanctionsWeightValidInput")
  fun `test getTotalContactAdultSexualSanctionsWeight with valid inputs`(input: Int, expected: Int) {
    val result = getTotalContactAdultSexualSanctionsWeight(input)
    assertEquals(expected, result)
  }

  @Test
  fun `test getTotalContactAdultSexualSanctionsWeight throws for negative value`() {
    val ex = assertThrows<IllegalArgumentException> {
      getTotalContactAdultSexualSanctionsWeight(-1)
    }
    assertTrue(ex.message!!.contains("Invalid contact adult sexual sanctions value: -1"))
  }

  @ParameterizedTest
  @MethodSource("getTotalContactChildSexualSanctionsWeightValidInput")
  fun `test getTotalContactChildSexualSanctionsWeight with valid inputs`(input: Int, expected: Int) {
    val result = getTotalContactChildSexualSanctionsWeight(input)
    assertEquals(expected, result)
  }

  @Test
  fun `test getTotalContactChildSexualSanctionsWeight throws for negative value`() {
    val ex = assertThrows<IllegalArgumentException> {
      getTotalContactChildSexualSanctionsWeight(-1)
    }
    assertTrue(ex.message!!.contains("Invalid contact child sexual sanctions value: -1"))
  }

  @ParameterizedTest
  @MethodSource("getTotalNonContactSexualOffencesExcludingIndecentImagesWeightValidInput")
  fun `test getTotalNonContactSexualOffencesExcludingIndecentImagesWeight with valid inputs`(getTotalNonContactSexualOffencesExcludingIndecentImagesWeight: Int, totalIndecentImageSanctions: Int, expected: Int) {
    val result = getTotalNonContactSexualOffencesExcludingIndecentImagesWeight(getTotalNonContactSexualOffencesExcludingIndecentImagesWeight, totalIndecentImageSanctions)
    assertEquals(expected, result)
  }

  @Test
  fun `test getTotalNonContactSexualOffencesExcludingIndecentImagesWeight throws for negative value`() {
    val ex = assertThrows<IllegalArgumentException> {
      getTotalNonContactSexualOffencesExcludingIndecentImagesWeight(0, 1)
    }

    assertTrue(ex.message!!.contains("Invalid total non-contact sexual offences excluding indecent images value: -1"))
  }

  @ParameterizedTest
  @MethodSource("getAgeAtStartOfFollowupWeightValidAges")
  fun `getAgeAtStartOfFollowupWeight test valid ages return correct weights`(age: Int, expected: Int) {
    val dob = dateYearsAgo(age)
    val followupDate = LocalDate.of(2025, 1, 1)
    val result = getAgeAtStartOfFollowupWeight(dob, followupDate)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getAgeAtStartOfFollowupWeightInvalidAges")
  fun `getAgeAtStartOfFollowupWeight test invalid ages throw exception`(age: Int) {
    val dob = dateYearsAgo(age)
    val followupDate = LocalDate.of(2025, 1, 1)
    val ex = assertThrows<IllegalArgumentException> {
      getAgeAtStartOfFollowupWeight(dob, followupDate)
    }
    assertTrue(ex.message!!.contains("Invalid age at start of follow up"))
  }

  @ParameterizedTest
  @MethodSource("getAgeAtLastSanctionForSexualOffenceWeightValidAges")
  fun `test valid ages return correct weights`(age: Int, expectedWeight: Int) {
    val dob = dateYearsAgo(age)
    val offenceDate = LocalDate.of(2025, 1, 1)
    val result = getAgeAtLastSanctionForSexualOffenceWeight(dob, offenceDate)
    assertEquals(expectedWeight, result)
  }

  @ParameterizedTest
  @MethodSource("getAgeAtLastSanctionForSexualOffenceWeightInvalidAges")
  fun `test invalid ages throw exception`(age: Int) {
    val dob = dateYearsAgo(age)
    val offenceDate = LocalDate.of(2025, 1, 1)
    val ex = assertThrows<IllegalArgumentException> {
      getAgeAtLastSanctionForSexualOffenceWeight(dob, offenceDate)
    }
    assertTrue(ex.message!!.contains("Invalid age at last sanction for sexual offence"))
  }

  @ParameterizedTest
  @MethodSource("getTotalNumberOfSanctionsWeightValidSanctionCounts")
  fun `test valid totalNumberOfSanctions returns correct weight`(input: Int, expected: Int) {
    val result = getTotalNumberOfSanctionsWeight(input)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getTotalNumberOfSanctionsWeightInvalidSanctionCounts")
  fun `test invalid totalNumberOfSanctions throws exception`(input: Int) {
    val exception = assertThrows<IllegalArgumentException> {
      getTotalNumberOfSanctionsWeight(input)
    }
    assertTrue(exception.message!!.contains("Invalid total number of sanctions"))
  }

  @ParameterizedTest
  @MethodSource("strangerVictimValues")
  fun `test all branches of getStrangerVictimWeight`(input: Boolean?, expected: Int) {
    val result = getStrangerVictimWeight(input)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getOSPDCBandValidScores")
  fun `test valid OSPDC scores return correct RiskBand`(score: Int, expected: RiskBand) {
    val result = getOSPDCBand(score)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getOSPDCBandInvalidScores")
  fun `test invalid OSPDC scores throw IllegalArgumentException`(score: Int, expectedMessage: String) {
    val ex = assertThrows<IllegalArgumentException> {
      getOSPDCBand(score)
    }
    assertEquals(expectedMessage, ex.message)
  }

  @ParameterizedTest
  @MethodSource("getOSPDCScoreValidScores")
  fun `test OSPDC score calculation for given input`(input: Int, expected: Double) {
    val result = getOSPDCScore(input)
    assertEquals(expected, result, 0.0001)
  }

  private fun dateYearsAgo(years: Int): LocalDate = LocalDate.of(2025, 1, 1).minusYears(years.toLong())

  companion object {
    @JvmStatic
    fun getTotalContactAdultSexualSanctionsWeightValidInput(): Stream<Arguments> = Stream.of(
      Arguments.of(0, 0),
      Arguments.of(1, 5),
      Arguments.of(2, 10),
      Arguments.of(3, 15),
      Arguments.of(Int.MAX_VALUE, 15),
    )

    @JvmStatic
    fun getTotalContactChildSexualSanctionsWeightValidInput(): Stream<Arguments> = Stream.of(
      Arguments.of(0, 0),
      Arguments.of(1, 3),
      Arguments.of(2, 6),
      Arguments.of(3, 9),
      Arguments.of(Int.MAX_VALUE, 9),
    )

    @JvmStatic
    fun getTotalNonContactSexualOffencesExcludingIndecentImagesWeightValidInput(): Stream<Arguments> = Stream.of(
      Arguments.of(1, 1, 0),
      Arguments.of(2, 1, 2),
      Arguments.of(4, 2, 4),
      Arguments.of(6, 3, 6),
      Arguments.of(Int.MAX_VALUE, 1, 6),
    )

    @JvmStatic
    fun getAgeAtStartOfFollowupWeightValidAges(): Stream<Arguments> = Stream.of(
      Arguments.of(18, 14),
      Arguments.of(21, 13),
      Arguments.of(24, 12),
      Arguments.of(27, 11),
      Arguments.of(30, 10),
      Arguments.of(33, 9),
      Arguments.of(36, 8),
      Arguments.of(39, 7),
      Arguments.of(42, 6),
      Arguments.of(45, 5),
      Arguments.of(48, 4),
      Arguments.of(51, 3),
      Arguments.of(54, 2),
      Arguments.of(57, 1),
      Arguments.of(60, 0),
      Arguments.of(150, 0), // Ensure upper bound coverage
    )

    @JvmStatic
    fun getAgeAtStartOfFollowupWeightInvalidAges(): Stream<Arguments> = Stream.of(
      Arguments.of(17),
      Arguments.of(0),
      Arguments.of(-1),
    )

    @JvmStatic
    fun getAgeAtLastSanctionForSexualOffenceWeightValidAges(): Stream<Arguments> = Stream.of(
      Arguments.of(10, 0),
      Arguments.of(15, 0),
      Arguments.of(16, 5),
      Arguments.of(17, 5),
      Arguments.of(18, 10),
      Arguments.of(35, 10),
      Arguments.of(100, 10),
    )

    @JvmStatic
    fun getAgeAtLastSanctionForSexualOffenceWeightInvalidAges(): Stream<Arguments> = Stream.of(
      Arguments.of(9),
      Arguments.of(0),
      Arguments.of(-1),
    )

    @JvmStatic
    fun getTotalNumberOfSanctionsWeightValidSanctionCounts(): Stream<Arguments> = Stream.of(
      Arguments.of(1, 0), // exact match
      Arguments.of(2, 6), // in 2..Int.MAX_VALUE
      Arguments.of(5, 6),
      Arguments.of(100, 6),
      Arguments.of(Int.MAX_VALUE, 6),
    )

    @JvmStatic
    fun getTotalNumberOfSanctionsWeightInvalidSanctionCounts(): Stream<Arguments> = Stream.of(
      Arguments.of(0),
      Arguments.of(-1),
      Arguments.of(-100),
    )

    @JvmStatic
    fun strangerVictimValues(): Stream<Arguments> = Stream.of(
      Arguments.of(true, 4),
      Arguments.of(false, 0),
      Arguments.of(null, 2),
    )

    @JvmStatic
    fun getOSPDCBandValidScores(): Stream<Arguments> = Stream.of(
      Arguments.of(0, RiskBand.NOT_APPLICABLE),
      Arguments.of(1, RiskBand.LOW),
      Arguments.of(21, RiskBand.LOW),
      Arguments.of(22, RiskBand.MEDIUM),
      Arguments.of(29, RiskBand.MEDIUM),
      Arguments.of(30, RiskBand.HIGH),
      Arguments.of(35, RiskBand.HIGH),
      Arguments.of(36, RiskBand.VERY_HIGH),
      Arguments.of(64, RiskBand.VERY_HIGH),
    )

    @JvmStatic
    fun getOSPDCBandInvalidScores(): Stream<Arguments> = Stream.of(
      Arguments.of(-1, "Invalid OSP/DC 64 point score value: -1"),
      Arguments.of(65, "Invalid OSP/DC 64 point score value: 65"),
    )

    @JvmStatic
    fun getOSPDCScoreValidScores(): Stream<Arguments> = Stream.of(
      Arguments.of(0, 0.0001),
      Arguments.of(32, 0.028756873716863567),
      Arguments.of(64, 0.8311640984549419),
    )
  }
}
