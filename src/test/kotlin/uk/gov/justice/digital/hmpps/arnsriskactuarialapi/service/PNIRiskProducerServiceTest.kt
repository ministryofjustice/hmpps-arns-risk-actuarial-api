package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.NeedScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.ProgrammeNeedIdentifier
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.highOgrs2
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.highOvp
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.lowOgrs2
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.pniRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validPNIRiskScoreRequest

class PNIRiskProducerServiceTest {

  private val service = PNIRiskProducerService()

  @Test
  fun `should calculate ALTERNATIVE PNI with a valid request`() {
    val context = emptyContext().copy(
      OGRS3 = lowOgrs2(),
    )
    val request = validPNIRiskScoreRequest()
    val result = service.getRiskScore(request, context).PNI
    assertNotNull(result)
    assertEquals(ProgrammeNeedIdentifier.ALTERNATIVE, result.pniPathway)
    assertTrue(result.validationError?.isEmpty() == true)
  }

  @Test
  fun `should calculate HIGH PNI with a valid request`() {
    val context = emptyContext().copy(
      OGRS3 = highOgrs2(),
      OVP = highOvp(),
    )
    val request = validPNIRiskScoreRequest().copy(
      sexualPreoccupation = ProblemLevel.SIGNIFICANT_PROBLEMS,
      sexualInterestsOffenceRelated = ProblemLevel.SIGNIFICANT_PROBLEMS,
      emotionalCongruence = ProblemLevel.SIGNIFICANT_PROBLEMS,
      saraRiskToOthers = RiskBand.HIGH,
      custody = true,
    )
    val result = service.getRiskScore(request, context).PNI
    assertNotNull(result)
    assertEquals(ProgrammeNeedIdentifier.HIGH, result.pniPathway)
    assertTrue(result.validationError?.isEmpty() == true)
  }

  @Test
  fun `should calculate MODERATE PNI with a valid request`() {
    val context = emptyContext().copy(
      OGRS3 = lowOgrs2(),
    )
    val request = validPNIRiskScoreRequest().copy(
      sexualPreoccupation = ProblemLevel.SOME_PROBLEMS,
      sexualInterestsOffenceRelated = ProblemLevel.SOME_PROBLEMS,
      emotionalCongruence = ProblemLevel.SOME_PROBLEMS,
      saraRiskToOthers = RiskBand.HIGH,
      custody = false,
      community = true,
    )
    val result = service.getRiskScore(request, context).PNI
    assertNotNull(result)
    assertEquals(ProgrammeNeedIdentifier.MODERATE, result.pniPathway)
    assertTrue(result.validationError?.isEmpty() == true)
  }

  @Test
  fun `should calculate OMISSION PNI with an invalid request and aggregate missing fields from different domains`() {
    val context = emptyContext().copy(
      OGRS3 = lowOgrs2(),
    )
    val request = validPNIRiskScoreRequest().copy(
      sexualPreoccupation = null,
      sexualInterestsOffenceRelated = null,
      emotionalCongruence = null,
      easilyInfluencedByCriminals = null,

    )
    val result = service.getRiskScore(request, context).PNI
    assertNotNull(result)
    assertEquals(ProgrammeNeedIdentifier.OMISSION, result.pniPathway)
    assertTrue(result.validationError?.isNotEmpty() == true)
    val error = result.validationError?.first()
    assertEquals(ValidationErrorType.MISSING_INPUT, error?.type)
    assertEquals("ERR5 - Field is Null", error?.message)
    assertTrue(error?.fields?.contains("sexualPreoccupation") == true)
    assertTrue(error?.fields?.contains("sexualInterestsOffenceRelated") == true)
    assertTrue(error?.fields?.contains("emotionalCongruence") == true)
    assertTrue(error?.fields?.contains("easilyInfluencedByCriminals") == true)
  }

  @Nested
  inner class IntensityTest {

    @Test
    fun `should return true for high intensity when custody is true and OGRS and OVP are high`() {
      val request = pniRequest().copy(
        custody = true,
        ogrs3TwoYear = 80,
        ovp = 65,
      )
      val result = service.isHighIntensity(request, NeedScore.MEDIUM, RiskBand.MEDIUM)
      assertTrue(result)
    }

    @Test
    fun `should return false for high intensity when custody is false even if scores are high`() {
      val request = pniRequest().copy(
        custody = false,
        ogrs3TwoYear = 80,
        ovp = 65,
      )
      val result = service.isHighIntensity(request, NeedScore.HIGH, RiskBand.HIGH)
      assertFalse(result)
    }

    @Test
    fun `should return true for high intensity with high need and high risk`() {
      val request = pniRequest().copy(custody = true)
      val result = service.isHighIntensity(request, NeedScore.HIGH, RiskBand.HIGH)
      assertTrue(result)
    }

    @Test
    fun `should return true for high intensity with high sara and high ogrs`() {
      val request = pniRequest().copy(
        custody = true,
        ogrs3TwoYear = 80,
        saraRiskToPartner = RiskBand.HIGH,
      )
      val result = service.isHighIntensity(request, NeedScore.MEDIUM, RiskBand.MEDIUM)
      assertTrue(result)
    }

    @Test
    fun `should return true for moderate intensity with high ogrs and ovp and community`() {
      val request = pniRequest().copy(
        community = true,
        custody = false,
        ogrs3TwoYear = 80,
        ovp = 65,
      )
      val result = service.isModerateIntensity(request, NeedScore.MEDIUM, RiskBand.MEDIUM)
      assertTrue(result)
    }

    @Test
    fun `should return true for moderate intensity with high sara`() {
      val request = pniRequest().copy(
        community = true,
        saraRiskToOthers = RiskBand.HIGH,
      )
      val result = service.isModerateIntensity(request, NeedScore.MEDIUM, RiskBand.MEDIUM)
      assertTrue(result)
    }

    @Test
    fun `should return true for moderate intensity with medium need and high risk`() {
      val request = pniRequest().copy(community = true)
      val result = service.isModerateIntensity(request, NeedScore.MEDIUM, RiskBand.HIGH)
      assertTrue(result)
    }

    @Test
    fun `should return false for moderate intensity if none of the conditions apply`() {
      val request = pniRequest().copy(community = true)
      val result = service.isModerateIntensity(request, NeedScore.LOW, RiskBand.LOW)
      assertFalse(result)
    }
  }

  @Nested
  inner class OverallRiskTest {
    @Test
    fun `isHighRisk returns true when custody is true and ogrs3 is high`() {
      val result = service.isHighRisk(pniRequest().copy(custody = true, ogrs3TwoYear = 80))
      assertTrue(result)
    }

    @Test
    fun `isHighRisk returns true when ovp is high`() {
      val result = service.isHighRisk(pniRequest().copy(ovp = 65))
      assertTrue(result)
    }

    @Test
    fun `isHighRisk returns true when ospDc is high for male`() {
      val result = service.isHighRisk(pniRequest().copy(gender = Gender.MALE, ospDCBand = RiskBand.HIGH))
      assertTrue(result)
    }

    @Test
    fun `isHighRisk returns false when ospDc is high but gender is female`() {
      val result = service.isHighRisk(pniRequest().copy(gender = Gender.FEMALE, ospDCBand = RiskBand.HIGH))
      assertFalse(result)
    }

    @Test
    fun `isHighRisk returns true when ospIic is high for male`() {
      val result = service.isHighRisk(pniRequest().copy(gender = Gender.MALE, ospIICBand = RiskBand.HIGH))
      assertTrue(result)
    }

    @Test
    fun `isHighRisk returns true for rsr high when male and osp scores null`() {
      val result = service.isHighRisk(pniRequest().copy(gender = Gender.MALE, rsr = 4))
      assertTrue(result)
    }

    @Test
    fun `isHighRisk returns true for rsr high when female`() {
      val result = service.isHighRisk(pniRequest().copy(gender = Gender.FEMALE, rsr = 3))
      assertTrue(result)
    }

    @Test
    fun `isHighRisk returns true for high SARA score`() {
      val result = service.isHighRisk(pniRequest().copy(saraRiskToOthers = RiskBand.HIGH))
      assertTrue(result)
    }

    @Test
    fun `isHighRisk returns false when all risk factors are low or null`() {
      val result = service.isHighRisk(pniRequest().copy(custody = false, ogrs3TwoYear = 40, ovp = 10, rsr = 1))
      assertFalse(result)
    }

    @Test
    fun `isMediumRisk returns true for ogrs3 medium`() {
      val result = service.isMediumRisk(pniRequest().copy(ogrs3TwoYear = 60))
      assertTrue(result)
    }

    @Test
    fun `isMediumRisk returns true for ovp medium`() {
      val result = service.isMediumRisk(pniRequest().copy(ovp = 45))
      assertTrue(result)
    }

    @Test
    fun `isMediumRisk returns true for ospDc medium for male`() {
      val result = service.isMediumRisk(pniRequest().copy(gender = Gender.MALE, ospDCBand = RiskBand.MEDIUM))
      assertTrue(result)
    }

    @Test
    fun `isMediumRisk returns false for ospDc medium for female`() {
      val result = service.isMediumRisk(pniRequest().copy(gender = Gender.FEMALE, ospDCBand = RiskBand.MEDIUM))
      assertFalse(result)
    }

    @Test
    fun `isMediumRisk returns true for ospIic medium for male`() {
      val result = service.isMediumRisk(pniRequest().copy(gender = Gender.MALE, ospIICBand = RiskBand.MEDIUM))
      assertTrue(result)
    }

    @Test
    fun `isMediumRisk returns true for rsr medium when female`() {
      val result = service.isMediumRisk(pniRequest().copy(gender = Gender.FEMALE, rsr = 2))
      assertTrue(result)
    }

    @Test
    fun `isMediumRisk returns false for rsr medium when male and osp scores are present`() {
      val result = service.isMediumRisk(pniRequest().copy(gender = Gender.MALE, rsr = 2, ospDCBand = RiskBand.HIGH))
      assertFalse(result)
    }

    @Test
    fun `isMediumRisk returns true for medium SARA`() {
      val result = service.isMediumRisk(pniRequest().copy(saraRiskToPartner = RiskBand.MEDIUM))
      assertTrue(result)
    }

    @Test
    fun `isMediumRisk returns false when all factors are low or null`() {
      val result = service.isMediumRisk(pniRequest().copy(ogrs3TwoYear = 40, ovp = 10, rsr = 0))
      assertFalse(result)
    }
  }
}
