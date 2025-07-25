package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ovp.OVPRequestValidated
import java.time.LocalDate
import java.util.stream.Stream

class OVPTransformationHelperTest {

  @Test
  fun `getCurrentAccommodationOffendersScore maps correctly`() {
    assertEquals(2, getCurrentAccommodationOffendersScore(testRequest(currentAccommodation = true)))
    assertEquals(0, getCurrentAccommodationOffendersScore(testRequest(currentAccommodation = false)))
  }

  @Test
  fun `getEmploymentStatusOffendersScore maps correctly`() {
    assertEquals(2, getEmploymentStatusOffendersScore(testRequest(employmentStatus = true)))
    assertEquals(0, getEmploymentStatusOffendersScore(testRequest(employmentStatus = false)))
  }

  @Test
  fun `getImpactOfOffendingOnOthersWeighted maps correctly`() {
    assertEquals(
      0,
      getImpactOfOffendingOnOthersWeighted(testRequest(impactOfOffendingOnOthers = true)),
    )
    assertEquals(
      4,
      getImpactOfOffendingOnOthersWeighted(testRequest(impactOfOffendingOnOthers = false)),
    )
  }

  @Test
  fun `getCurrentPsychiatricTreatmentOrPendingWeighted maps correctly`() {
    assertEquals(
      4,
      getCurrentPsychiatricTreatmentOrPendingWeighted(testRequest(currentPsychiatricTreatmentOrPending = true)),
    )
    assertEquals(
      0,
      getCurrentPsychiatricTreatmentOrPendingWeighted(testRequest(currentPsychiatricTreatmentOrPending = false)),
    )
  }

  @Test
  fun `getCurrentAccommodationWeightedOVP maps correctly`() {
    assertEquals(4, getCurrentAccommodationWeightedOVP(2))
    assertEquals(0, getCurrentAccommodationWeightedOVP(1))
  }

  @Test
  fun `getAgeAtStartOfFollowup valid`() {
    val request = testRequest(
      dateOfBirth = LocalDate.of(2000, 1, 1),
      dateAtStartOfFollowup = LocalDate.of(2020, 1, 1),
    )
    assertEquals(20, getAgeAtStartOfFollowup(request))
  }

  @Test
  fun `getAnyPreviousSanctionsWeighted maps correctly`() {
    assertEquals(0, getAnyPreviousSanctionsWeighted(testRequest(totalNumberOfSanctions = 0)))
    assertEquals(5, getAnyPreviousSanctionsWeighted(testRequest(totalNumberOfSanctions = 1)))
  }

  @Test
  fun `getGenderWeighted maps correctly`() {
    assertEquals(5, getGenderWeighted(testRequest(gender = Gender.MALE)))
    assertEquals(0, getGenderWeighted(testRequest(gender = Gender.FEMALE)))
  }

  @Test
  fun `getTotalNumberOfViolentSanctionsWeighted should map sanctions`() {
    assertEquals(0, getTotalNumberOfViolentSanctionsWeighted(0))
    assertEquals(4, getTotalNumberOfViolentSanctionsWeighted(1))
    assertEquals(24, getTotalNumberOfViolentSanctionsWeighted(17))
  }

  @Test
  fun `getTotalNumberOfNonViolentSanctionsWeighted should map sanctions`() {
    assertEquals(0, getTotalNumberOfNonViolentSanctionsWeighted(0))
    assertEquals(2, getTotalNumberOfNonViolentSanctionsWeighted(3))
    assertEquals(4, getTotalNumberOfNonViolentSanctionsWeighted(20))
  }

  @Test
  fun `getOffenderAgeGroupOVP should map age group`() {
    assertEquals(20, getOffenderAgeGroupOVP(18))
    assertEquals(0, getOffenderAgeGroupOVP(60))
  }

  @Test
  fun `calculateOVPPercentageOneYear should calculate percentage`() {
    val percentage = calculateOVPPercentageOneYear(totalOVPScore = 82)
    assertEquals(80, percentage)
  }

  @Test
  fun `calculateOVPPercentageTwoYears should calculate percentage`() {
    val percentage = calculateOVPPercentageTwoYears(totalOVPScore = 82)
    assertEquals(89, percentage)
  }

  @Test
  fun `getOVPBand should map percentages to bands`() {
    assertEquals(RiskBand.LOW, getOVPBand(25))
    assertEquals(RiskBand.MEDIUM, getOVPBand(40))
    assertEquals(RiskBand.HIGH, getOVPBand(70))
    assertEquals(RiskBand.VERY_HIGH, getOVPBand(85))
  }

  @ParameterizedTest
  @ValueSource(ints = [-1, 0, 100, 999])
  fun `getOVPBand should throw for invalid percentages`(percentage: Int) {
    val exception = assertThrows(IllegalArgumentException::class.java) {
      getOVPBand(percentage)
    }
    assertTrue(exception.message!!.contains("Unhandled OVP percentage"))
  }

  companion object {
    @JvmStatic
    fun alcoholMisuseTestCases(): Stream<Arguments> = Stream.of(
      Arguments.of(ProblemLevel.NO_PROBLEMS, ProblemLevel.NO_PROBLEMS, 0),
      Arguments.of(ProblemLevel.SOME_PROBLEMS, ProblemLevel.NO_PROBLEMS, 3),
      Arguments.of(ProblemLevel.NO_PROBLEMS, ProblemLevel.SOME_PROBLEMS, 3),
      Arguments.of(ProblemLevel.SOME_PROBLEMS, ProblemLevel.SOME_PROBLEMS, 5),
      Arguments.of(ProblemLevel.SIGNIFICANT_PROBLEMS, ProblemLevel.SIGNIFICANT_PROBLEMS, 10),
    )
  }

  @ParameterizedTest(name = "returns {2} for alcoholIsCurrentUseAProblem={0}, alcoholExcessive6Months={1}")
  @MethodSource("alcoholMisuseTestCases")
  fun `getAlcoholMisuseWeighted returns expected score`(
    currentUse: ProblemLevel,
    excessive6mo: ProblemLevel,
    expectedScore: Int,
  ) {
    val request = testRequest().copy(
      alcoholIsCurrentUseAProblem = currentUse,
      alcoholExcessive6Months = excessive6mo,
    )
    val result = getAlcoholMisuseWeighted(request)
    assertEquals(expectedScore, result)
  }

  private fun testRequest(
    currentAccommodation: Boolean = true,
    employmentStatus: Boolean = false,
    impactOfOffendingOnOthers: Boolean = false,
    currentPsychiatricTreatmentOrPending: Boolean = true,
    totalNumberOfSanctions: Int = 3,
    gender: Gender = Gender.MALE,
    dateOfBirth: LocalDate = LocalDate.of(2000, 1, 1),
    dateAtStartOfFollowup: LocalDate = LocalDate.of(2021, 1, 1),
  ) = OVPRequestValidated(
    currentAccommodation = currentAccommodation,
    employmentStatus = employmentStatus,
    impactOfOffendingOnOthers = impactOfOffendingOnOthers,
    currentPsychiatricTreatmentOrPending = currentPsychiatricTreatmentOrPending,
    totalNumberOfSanctions = totalNumberOfSanctions,
    gender = gender,
    dateOfBirth = dateOfBirth,
    dateAtStartOfFollowup = dateAtStartOfFollowup,
    // following don't matter for this tests
    totalNumberOfViolentSanctions = 1,
    alcoholIsCurrentUseAProblem = ProblemLevel.NO_PROBLEMS,
    alcoholExcessive6Months = ProblemLevel.NO_PROBLEMS,
    temperControl = ProblemLevel.NO_PROBLEMS,
    proCriminalAttitudes = ProblemLevel.NO_PROBLEMS,
  )
}
