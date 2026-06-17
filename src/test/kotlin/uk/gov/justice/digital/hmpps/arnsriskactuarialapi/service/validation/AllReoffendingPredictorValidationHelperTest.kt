package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.FIXED_TEST_DATE
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validAllReoffendingPredictorDynamicRiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validAllReoffendingPredictorStaticRiskScoreRequest

class AllReoffendingPredictorValidationHelperTest {

  @Test
  fun `validateAllReoffendingPredictorStatic valid static request results in no validation errors`() {
    val errors = validateAllReoffendingPredictorStatic(validAllReoffendingPredictorStaticRiskScoreRequest())
    assertTrue(errors.isEmpty())
  }

  @Test
  fun `validateAllReoffendingPredictorStatic missing all mandatory fields error`() {
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
    val result = validateAllReoffendingPredictorStatic(request)

    val expectedFields = listOf(
      "dateOfBirth",
      "dateOfCurrentConviction",
      "ageAtFirstSanction",
      "gender",
      "currentOffenceCode",
      "totalNumberOfSanctionsForAllOffences",
      "dateAtStartOfFollowupCalculated",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_MANDATORY_INPUT, error.type)
    assertEquals("Mandatory input field(s) missing", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @Test
  fun `validateAllReoffendingPredictorDynamic valid dynamic request results in no validation errors`() {
    val errors = validateAllReoffendingPredictorDynamic(validAllReoffendingPredictorDynamicRiskScoreRequest())
    assertTrue(errors.isEmpty())
  }

  @Test
  fun `validateAllReoffendingPredictorDynamic missing all required fields error`() {
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
    val result = validateAllReoffendingPredictorDynamic(request)

    val expectedFields = listOf(
      "suitabilityOfAccommodation",
      "isUnemployed",
      "currentRelationshipWithPartner",
      "evidenceOfDomesticAbuse",
      "currentRelationshipStatus",
      "regularOffendingActivities",
      "motivationToTackleDrugMisuse",
      "hasHeroinUsage",
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
      "currentAlcoholUseProblems",
      "excessiveAlcoholUse",
      "impulsivityProblems",
      "proCriminalAttitudes",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_DYNAMIC_INPUT, error.type)
    assertEquals("Dynamic input field(s) missing", error.message)
    assertEquals(expectedFields, error.fields)
  }
}
