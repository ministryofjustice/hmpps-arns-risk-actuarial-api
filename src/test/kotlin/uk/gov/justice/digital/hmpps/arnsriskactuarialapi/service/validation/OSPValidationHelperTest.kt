package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.SupervisionStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validOSPDCRiskScoreRequest
import java.time.LocalDate
import java.util.stream.Stream
import kotlin.collections.listOf

class OSPValidationHelperTest {

  @Test
  fun `ospdcInitialValidation no errors`() {
    val result = validateOSP(validOSPDCRiskScoreRequest(), true)
    assertTrue(result.isEmpty())
  }

  @ParameterizedTest
  @MethodSource("missingFieldValidationProvider")
  fun `ospInitialValidation missing field error with all field populated`(
    includeOptional: Boolean,
    expectedFields: List<String>,
  ) {
    val request = validOSPDCRiskScoreRequest().copy(
      gender = null,
      dateOfBirth = null,
      hasEverCommittedSexualOffence = null,
      totalContactAdultSexualSanctions = null,
      totalContactChildSexualSanctions = null,
      totalNonContactSexualOffences = null,
      totalIndecentImageSanctions = null,
      dateAtStartOfFollowupUserInput = null,
      dateOfMostRecentSexualOffence = null,
      totalNumberOfSanctionsForAllOffences = null,
      supervisionStatus = null,
      mostRecentOffenceDate = null,
    )

    val result = validateOSP(request, includeOptional)

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_MANDATORY_INPUT, error.type)
    assertEquals("Mandatory input field(s) missing", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `ospInitialValidation missing field error with all field populated when hasEverCommittedSexualOffence is true`(isOSPDC: Boolean) {
    val request = validOSPDCRiskScoreRequest().copy(
      gender = Gender.MALE,
      dateOfBirth = LocalDate.of(1980, 1, 1),
      hasEverCommittedSexualOffence = true,
      totalContactAdultSexualSanctions = null,
      totalContactChildSexualSanctions = null,
      totalNonContactSexualOffences = null,
      totalIndecentImageSanctions = null,
      dateAtStartOfFollowupUserInput = LocalDate.of(2030, 1, 1),
      dateOfMostRecentSexualOffence = null,
      totalNumberOfSanctionsForAllOffences = 1 as Integer?,
      supervisionStatus = SupervisionStatus.COMMUNITY,
      mostRecentOffenceDate = null,
    )

    val result = validateOSP(request, isOSPDC)

    val expectedFields = listOf(
      "totalContactAdultSexualSanctions",
      "totalContactChildSexualSanctions",
      "totalNonContactSexualOffences",
      "totalIndecentImageSanctions",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.SEXUAL_OFFENDING_MISSING_COUNTS, error.type)
    assertEquals("Sexual motivation/offending identified - complete sexual offence counts", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `ospdcInitialValidation sexual offence missing count error with all field populated when hasEverCommittedSexualOffence is true`(isOSPDC: Boolean) {
    val request = validOSPDCRiskScoreRequest().copy(
      gender = Gender.MALE,
      dateOfBirth = LocalDate.of(1980, 1, 1),
      hasEverCommittedSexualOffence = true,
      totalContactAdultSexualSanctions = 0,
      totalContactChildSexualSanctions = 0,
      totalNonContactSexualOffences = 0,
      totalIndecentImageSanctions = 0,
      dateAtStartOfFollowupUserInput = LocalDate.of(2030, 1, 1),
      dateOfMostRecentSexualOffence = null,
      totalNumberOfSanctionsForAllOffences = 1 as Integer?,
      supervisionStatus = SupervisionStatus.COMMUNITY,
      mostRecentOffenceDate = null,
    )

    val result = validateOSP(request, isOSPDC)

    val expectedFields = listOf(
      "totalContactAdultSexualSanctions",
      "totalContactChildSexualSanctions",
      "totalIndecentImageSanctions",
      "totalNonContactSexualOffences",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.SEXUAL_OFFENDING_MISSING_COUNTS, error.type)
    assertEquals("Sexual motivation/offending identified - complete sexual offence counts", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `ospInitialValidation missing sexual motivation question`(isOSPDC: Boolean) {
    val request = validOSPDCRiskScoreRequest().copy(
      gender = Gender.MALE,
      hasEverCommittedSexualOffence = true,
      isCurrentOffenceSexuallyMotivated = null,
    )

    val result = validateOSP(request, isOSPDC)

    val expectedFields = listOf(
      "isCurrentOffenceSexuallyMotivated",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_MANDATORY_INPUT, error.type)
    assertEquals("Mandatory input field(s) missing", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `ospdcInitialValidation no error when hasEverCommittedSexualOffence is true`(isOSPDC: Boolean) {
    val request = validOSPDCRiskScoreRequest().copy(
      hasEverCommittedSexualOffence = true,
    )

    val result = validateOSP(request, isOSPDC)

    assertTrue(result.isEmpty())
  }

  companion object {
    @JvmStatic
    fun missingFieldValidationProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(
        true,
        listOf(
          "gender",
          "hasEverCommittedSexualOffence",
          "dateOfBirth",
          "dateAtStartOfFollowupUserInput",
          "totalNumberOfSanctionsForAllOffences",
          "supervisionStatus",
        ),
      ),
      Arguments.of(
        false,
        listOf(
          "gender",
        ),
      ),
    )
  }
}
