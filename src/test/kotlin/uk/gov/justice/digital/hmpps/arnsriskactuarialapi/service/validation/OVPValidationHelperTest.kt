package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.FIXED_TEST_DATE
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import java.time.LocalDate

class OVPValidationHelperTest {

  @Test
  fun `getMissingFieldsValidation no errors`() {
    val result = validateOVP(validOVPRiskScoreRequest())
    assertTrue(result.isEmpty())
  }

  @Test
  fun `validateOVP totalNumberOfSanctions validation error`() {
    val request = validOVPRiskScoreRequest()
      .copy(totalNumberOfSanctionsForAllOffences = 0 as Integer)
    val errors = validateOVP(request)
    assertEquals(1, errors.size)
    assertEquals(ValidationErrorType.TOTAL_NUMBER_OF_SANCTIONS_LESS_THAN_ONE, errors[0].type)
    assertEquals("Total number of sanctions must be one or greater", errors[0].message)
    assertEquals(listOf("totalNumberOfSanctionsForAllOffences"), errors[0].fields)
  }

  @Test
  fun `getMissingFieldsValidation missing field error with all field populated`() {
    val request = RiskScoreRequest(
      RiskScoreVersion.V1_0,
      null,
      FIXED_TEST_DATE,
      null,
      null,
      null,
      null,
      null,
      null,
    )
    val result = validateOVP(request)

    val expectedFields = listOf(
      "gender",
      "dateOfBirth",
      "dateAtStartOfFollowup",
      "totalNumberOfSanctionsForAllOffences",
      "totalNumberOfViolentSanctions",
      "doesRecogniseImpactOfOffendingOnOthers",
      "isCurrentlyOfNoFixedAbodeOrTransientAccommodation",
      "isUnemployed",
      "currentAlcoholUseProblems",
      "excessiveAlcoholUse",
      "hasCurrentPsychiatricTreatment",
      "temperControl",
      "proCriminalAttitudes",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_MANDATORY_INPUT, error.type)
    assertEquals("Mandatory input field(s) missing", error.message)
    assertEquals(expectedFields, error.fields)
  }

  private fun validOVPRiskScoreRequest(): RiskScoreRequest = RiskScoreRequest(
    version = RiskScoreVersion.V1_0,
    gender = Gender.MALE,
    dateOfBirth = LocalDate.of(1990, 1, 1),
    dateAtStartOfFollowup = LocalDate.of(2021, 1, 1),
    totalNumberOfSanctionsForAllOffences = 1 as Integer?,
    totalNumberOfViolentSanctions = 1 as Integer?,
    doesRecogniseImpactOfOffendingOnOthers = true,
    isCurrentlyOfNoFixedAbodeOrTransientAccommodation = true,
    isUnemployed = false,
    currentAlcoholUseProblems = ProblemLevel.SOME_PROBLEMS,
    excessiveAlcoholUse = ProblemLevel.SIGNIFICANT_PROBLEMS,
    hasCurrentPsychiatricTreatment = true,
    temperControl = ProblemLevel.SOME_PROBLEMS,
    proCriminalAttitudes = ProblemLevel.SOME_PROBLEMS,
  )
}
