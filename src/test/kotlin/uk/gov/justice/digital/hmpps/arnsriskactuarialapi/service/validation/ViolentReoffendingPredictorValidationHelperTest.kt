package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.FIXED_TEST_DATE
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validViolentReoffendingPredictorDynamicRiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validViolentReoffendingPredictorStaticRiskScoreRequest

class ViolentReoffendingPredictorValidationHelperTest {

  @Test
  fun `validateViolentReoffendingPredictorStatic valid static request results in no validation errors`() {
    val errors = validateViolentReoffendingPredictorStatic(validViolentReoffendingPredictorStaticRiskScoreRequest())
    assertTrue(errors.isEmpty())
  }

  @Test
  fun `validateViolentReoffendingPredictorStatic missing all mandatory fields error`() {
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
      null,
    )
    val result = validateViolentReoffendingPredictorStatic(request)

    val expectedFields = listOf(
      "dateOfBirth",
      "dateOfCurrentConviction",
      "ageAtFirstSanction",
      "gender",
      "currentOffenceCode",
      "totalNumberOfSanctionsForAllOffences",
      "totalNumberOfViolentSanctions",
      "dateAtStartOfFollowupCalculated",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_MANDATORY_INPUT, error.type)
    assertEquals("Mandatory input field(s) missing", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @Test
  fun `validateViolentReoffendingPredictorDynamic valid dynamic request results in no validation errors`() {
    val errors = validateViolentReoffendingPredictorDynamic(validViolentReoffendingPredictorDynamicRiskScoreRequest())
    assertTrue(errors.isEmpty())
  }

  @Test
  fun `validateViolentReoffendingPredictorDynamic missing all required fields error`() {
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
      null,
    )
    val result = validateViolentReoffendingPredictorDynamic(request)

    val expectedFields = listOf(
      "suitabilityOfAccommodation",
      "isUnemployed",
      "currentRelationshipWithPartner",
      "evidenceOfDomesticAbuse",
      "currentRelationshipStatus",
      "regularOffendingActivities",
      "motivationToTackleDrugMisuse",
      "hasOtherOpiateUsage",
      "hasCrackCocaineUsage",
      "hasPowderCocaineUsage",
      "hasMisusedPrescriptionDrugUsage",
      "hasBenzodiazepinesUsage",
      "hasCannabisUsage",
      "hasSteroidsUsage",
      "hasOtherDrugsUsage",
      "hasKetamineUsage",
      "hasSpiceUsage",
      "hasHallucinogensUsage",
      "hasSolventsUsage",
      "hasMethadoneUsage",
      "currentAlcoholUseProblems",
      "excessiveAlcoholUse",
      "temperControl",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_DYNAMIC_INPUT, error.type)
    assertEquals("Dynamic input field(s) missing", error.message)
    assertEquals(expectedFields, error.fields)
  }
}
