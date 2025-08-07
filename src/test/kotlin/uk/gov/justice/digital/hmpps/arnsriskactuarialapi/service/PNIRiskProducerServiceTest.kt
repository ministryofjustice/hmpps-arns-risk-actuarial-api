package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CustodyOrCommunity
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.NeedScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.ProgrammeNeedIdentifier
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyOGRS3
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyOVP
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyRSR
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
      inCustodyOrCommunity = CustodyOrCommunity.CUSTODY,
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
      inCustodyOrCommunity = CustodyOrCommunity.COMMUNITY,
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
        inCustodyOrCommunity = CustodyOrCommunity.CUSTODY,
        ogrs3TwoYear = 80,
        ovp = 65,
      )
      val result = service.isHighIntensity(request, NeedScore.MEDIUM, RiskBand.MEDIUM)
      assertTrue(result)
    }

    @Test
    fun `should return false for high intensity when custody is false even if scores are high`() {
      val request = pniRequest().copy(
        inCustodyOrCommunity = CustodyOrCommunity.COMMUNITY,
        ogrs3TwoYear = 80,
        ovp = 65,
      )
      val result = service.isHighIntensity(request, NeedScore.HIGH, RiskBand.HIGH)
      assertFalse(result)
    }

    @Test
    fun `should return true for high intensity with high need and high risk`() {
      val request = pniRequest().copy(inCustodyOrCommunity = CustodyOrCommunity.CUSTODY)
      val result = service.isHighIntensity(request, NeedScore.HIGH, RiskBand.HIGH)
      assertTrue(result)
    }

    @Test
    fun `should return true for high intensity with high sara and high ogrs`() {
      val request = pniRequest().copy(
        inCustodyOrCommunity = CustodyOrCommunity.CUSTODY,
        ogrs3TwoYear = 80,
        saraRiskToPartner = RiskBand.HIGH,
      )
      val result = service.isHighIntensity(request, NeedScore.MEDIUM, RiskBand.MEDIUM)
      assertTrue(result)
    }

    @Test
    fun `should return true for moderate intensity with high ogrs and ovp and community`() {
      val request = pniRequest().copy(
        inCustodyOrCommunity = CustodyOrCommunity.COMMUNITY,
        ogrs3TwoYear = 80,
        ovp = 65,
      )
      val result = service.isModerateIntensity(request, NeedScore.MEDIUM, RiskBand.MEDIUM)
      assertTrue(result)
    }

    @Test
    fun `should return true for moderate intensity with high sara`() {
      val request = pniRequest().copy(
        inCustodyOrCommunity = CustodyOrCommunity.COMMUNITY,
        saraRiskToOthers = RiskBand.HIGH,
      )
      val result = service.isModerateIntensity(request, NeedScore.MEDIUM, RiskBand.MEDIUM)
      assertTrue(result)
    }

    @Test
    fun `should return true for moderate intensity with medium need and high risk`() {
      val request = pniRequest().copy(inCustodyOrCommunity = CustodyOrCommunity.COMMUNITY)
      val result = service.isModerateIntensity(request, NeedScore.MEDIUM, RiskBand.HIGH)
      assertTrue(result)
    }

    @Test
    fun `should return false for moderate intensity if none of the conditions apply`() {
      val request = pniRequest().copy(inCustodyOrCommunity = CustodyOrCommunity.COMMUNITY)
      val result = service.isModerateIntensity(request, NeedScore.LOW, RiskBand.LOW)
      assertFalse(result)
    }
  }

  @Nested
  inner class OverallRiskTest {
    @Test
    fun `isHighRisk returns true when custody is true and ogrs3 is high`() {
      val result =
        service.isHighRisk(pniRequest().copy(inCustodyOrCommunity = CustodyOrCommunity.CUSTODY, ogrs3TwoYear = 80))
      assertTrue(result)
    }

    @Test
    fun `isHighRisk returns true when ovp is high`() {
      val result = service.isHighRisk(pniRequest().copy(ovp = 65))
      assertTrue(result)
    }

    @Test
    fun `isHighRisk returns true when ospDc is high for male`() {
      val result = service.isHighRisk(pniRequest().copy(ospDCBand = RiskBand.HIGH))
      assertTrue(result)
    }

    @Test
    fun `isHighRisk returns false when ospDc is high but gender is female`() {
      val result = service.isHighRisk(pniRequest().copy(ospDCBand = RiskBand.NOT_APPLICABLE))
      assertFalse(result)
    }

    @Test
    fun `isHighRisk returns true when ospIic is high for male`() {
      val result = service.isHighRisk(pniRequest().copy(ospIICBand = RiskBand.HIGH))
      assertTrue(result)
    }

    @Test
    fun `isHighRisk returns true for rsr high when male and osp scores null`() {
      val result = service.isHighRisk(pniRequest().copy(rsr = 4))
      assertTrue(result)
    }

    @Test
    fun `isHighRisk returns true for rsr high when female`() {
      val result =
        service.isHighRisk(pniRequest().copy(ospDCBand = RiskBand.NOT_APPLICABLE, ospIICBand = RiskBand.LOW, rsr = 3))
      assertTrue(result)
    }

    @Test
    fun `isHighRisk returns true for high SARA score`() {
      val result = service.isHighRisk(pniRequest().copy(saraRiskToOthers = RiskBand.HIGH))
      assertTrue(result)
    }

    @Test
    fun `isHighRisk returns false when all risk factors are low or null`() {
      val result = service.isHighRisk(
        pniRequest().copy(
          inCustodyOrCommunity = CustodyOrCommunity.COMMUNITY,
          ogrs3TwoYear = 40,
          ovp = 10,
          rsr = 1,
        ),
      )
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
      val result = service.isMediumRisk(pniRequest().copy(ospDCBand = RiskBand.MEDIUM))
      assertTrue(result)
    }

    @Test
    fun `isMediumRisk returns false for ospDc medium for female`() {
      val result = service.isMediumRisk(pniRequest().copy(ospDCBand = RiskBand.NOT_APPLICABLE))
      assertFalse(result)
    }

    @Test
    fun `isMediumRisk returns true for ospIic medium for male`() {
      val result = service.isMediumRisk(pniRequest().copy(ospIICBand = RiskBand.MEDIUM))
      assertTrue(result)
    }

    @Test
    fun `isMediumRisk returns true for rsr medium when female`() {
      val result =
        service.isMediumRisk(pniRequest().copy(ospDCBand = RiskBand.NOT_APPLICABLE, ospIICBand = RiskBand.LOW, rsr = 2))
      assertTrue(result)
    }

    @Test
    fun `isMediumRisk returns false for rsr medium when male and osp scores are present`() {
      val result = service.isMediumRisk(pniRequest().copy(rsr = 2, ospDCBand = RiskBand.HIGH))
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

  @ParameterizedTest(name = "[{index}] {arguments}")
  @CsvFileSource(
    resources = ["/data/PNI/PNI_test_data_small.csv"],
    useHeadersInDisplayName = true,
    nullValues = ["Null", "Missing", "M"],
    ignoreLeadingAndTrailingWhitespace = true,
  )
  fun `requests from CSV file`(
    @Suppress("UNUSEDUPARAMETER")
    id: String?,
    @Suppress("UNUSEDUPARAMETER")
    scenario: String?,
    expectedOutcome: String?,
    community: String?,
    s1U30: String?,
    s6U11: String?,
    s11U11: String?,
    s11U12: String?,
    s6U12: String?,
    s12U1: String?,
    s12U9: String?,
    s6U1: String?,
    s6U6: String?,
    s7U3: String?,
    s11U3: String?,
    s11U2: String?,
    s11U4: String?,
    s11U6: String?,
    s10U1: String?,
    ogrsY2: String?,
    ovpRiskReconElm: String?,
    ospCdc: String?,
    ospIiic: String?,
    rsrPercentageScore: String?,
    saraRiskLevelToPartner: String?,
    saraRiskLevelToOther: String?,
    @Suppress("UNUSEDUPARAMETER")
    sexDomain: String?,
    @Suppress("UNUSEDUPARAMETER")
    thinkingDomain: String?,
    @Suppress("UNUSEDUPARAMETER")
    relaDomain: String?,
    @Suppress("UNUSEDUPARAMETER")
    smDomain: String?,
    @Suppress("UNUSEDUPARAMETER")
    overallNeedsCheck: String?,
    @Suppress("UNUSEDUPARAMETER")
    overallRiskScore: String?,
    @Suppress("UNUSEDUPARAMETER")
    expectedPni: String?,
    @Suppress("UNUSEDUPARAMETER")
    sdScore: String?,
    @Suppress("UNUSEDUPARAMETER")
    tdScore: String?,
    @Suppress("UNUSEDUPARAMETER")
    rdScore: String?,
    @Suppress("UNUSEDUPARAMETER")
    smScore: String?,
    @Suppress("UNUSEDUPARAMETER")
    needsScoreMissingIgnored: String?,
    @Suppress("UNUSEDUPARAMETER")
    convertedRsrLevel: String?,
    @Suppress("UNUSEDUPARAMETER")
    removeLeading: String?,
    @Suppress("UNUSEDUPARAMETER")
    sdCountMissing: String?,
    @Suppress("UNUSEDUPARAMETER")
    tdCountMissing: String?,
    @Suppress("UNUSEDUPARAMETER")
    rdCountMissing: String?,
    @Suppress("UNUSEDUPARAMETER")
    smCountMissing: String?,
    @Suppress("UNUSEDUPARAMETER")
    sdPossibleRawScore: String?,
    @Suppress("UNUSEDUPARAMETER")
    tdPossibleRawScore: String?,
    @Suppress("UNUSEDUPARAMETER")
    rdPossibleRawScore: String?,
    @Suppress("UNUSEDUPARAMETER")
    smPossibleRawScore: String?,
    @Suppress("UNUSEDUPARAMETER")
    sdPossibleBand: String?,
    @Suppress("UNUSEDUPARAMETER")
    tdPossibleBand: String?,
    @Suppress("UNUSEDUPARAMETER")
    rdPossibleBand: String?,
    @Suppress("UNUSEDUPARAMETER")
    smPossibleBand: String?,
    @Suppress("UNUSEDUPARAMETER")
    projectedNeedsScore: String?,
    @Suppress("UNUSEDUPARAMETER")
    couldNeedsScoreChange: String?,
    @Suppress("UNUSEDUPARAMETER")
    usedNeedScore: String?,
    @Suppress("UNUSEDUPARAMETER")
    transform6U11: String?,
  ) {
    val request = RiskScoreRequest().copy(
      temperControl = s11U4.toProblemScore(),
      proCriminalAttitudes = s12U1.toProblemScore(),
      sexualPreoccupation = s11U11.toProblemScore(),
      sexualInterestsOffenceRelated = s11U12.toProblemScore(),
      emotionalCongruence = s6U12.toProblemScore(),
      saraRiskToPartner = saraRiskLevelToPartner.toSaraRiskBand(),
      saraRiskToOthers = saraRiskLevelToOther.toSaraRiskBand(),
      hostileOrientation = s12U9.toProblemScore(),
      currentRelationshipFamilyMembers = s6U1.toProblemScore(),
      previousCloseRelationships = s6U6.toProblemScore(),
      easilyInfluencedByCriminals = s7U3.toProblemScore(),
      controllingBehaviour = s11U3.toProblemScore(),
      impulsivityBehaviour = s11U2.toProblemScore(),
      problemSolvingSkills = s11U6.toProblemScore(),
      difficultiesCoping = s10U1.toProblemScore(),
      inCustodyOrCommunity = community.toCustodyOrCommunity(),
      hasCommittedSexualOffence = s1U30.toBoolean(),
      riskSexualHarm = s6U11.toBoolean(),
    )

    val rsr = emptyRSR().copy(
      ospdcBand = ospCdc.toRiskBand(),
      ospiicBand = ospIiic.toRiskBand(),
      rsrScore = rsrPercentageScore?.toIntOrNull(),
    )
    val ovp = emptyOVP().copy(
      provenViolentTypeReoffendingTwoYear = ovpRiskReconElm.bandToOVPScore(),
      band = ovpRiskReconElm.toRiskBand(),
    )
    val ogrs3 = emptyOGRS3().copy(
      ogrs3TwoYear = ogrsY2?.bandToOGRSScore(),
    )
    val result = service.getRiskScore(
      request,
      emptyContext().copy(
        RSR = rsr,
        OVP = ovp,
        OGRS3 = ogrs3,
      ),
    )
    val expectedPathway =
      expectedOutcome?.let {
        when (it) {
          "'H'" -> ProgrammeNeedIdentifier.HIGH
          "'M'" -> ProgrammeNeedIdentifier.MODERATE
          "'A'" -> ProgrammeNeedIdentifier.ALTERNATIVE
          "'O'" -> ProgrammeNeedIdentifier.OMISSION
          else -> null
        }
      } ?: throw IllegalArgumentException("Cannot parse PNI outcome from CSV")

    assertEquals(expectedPathway, result.PNI?.pniPathway)
  }
}

private fun String?.toProblemScore(): ProblemLevel? = when (this?.toIntOrNull()) {
  0 -> ProblemLevel.NO_PROBLEMS
  1 -> ProblemLevel.SOME_PROBLEMS
  2 -> ProblemLevel.SIGNIFICANT_PROBLEMS
  else -> null
}


private fun String?.toSaraRiskBand(): RiskBand? = when (this?.toIntOrNull()) {
  1 -> RiskBand.LOW
  2 -> RiskBand.MEDIUM
  3 -> RiskBand.HIGH
  4 -> RiskBand.VERY_HIGH
  else -> null
}

private fun String?.toRiskBand(): RiskBand? = when (this) {
  "'H'" -> RiskBand.HIGH
  "'V'" -> RiskBand.VERY_HIGH
  "'M'" -> RiskBand.MEDIUM
  "'L'" -> RiskBand.LOW
  "'NA'" -> RiskBand.NOT_APPLICABLE
  "'NULL'" -> null
  else -> null
}

private fun String?.bandToOVPScore(): Int? = when (this.toRiskBand()) {
  RiskBand.LOW -> 10 // between 1 and 29
  RiskBand.MEDIUM -> 40 // between 30 and 59
  RiskBand.HIGH -> 70 // between 60 and 79
  RiskBand.VERY_HIGH -> 85 // between 80 and 99
  RiskBand.NOT_APPLICABLE -> null
  null -> null
}

private fun String?.bandToOGRSScore(): Int? = when (this.toRiskBand()) {
  RiskBand.LOW -> 30 // between 0 and 49
  RiskBand.MEDIUM -> 60 // between 50 and 74
  RiskBand.HIGH -> 80 // between 75 and 89
  RiskBand.VERY_HIGH -> 95 // 90 plus
  RiskBand.NOT_APPLICABLE -> null
  null -> null
}

private fun String?.toCustodyOrCommunity(): CustodyOrCommunity = when (this) {
  "'YES'" -> CustodyOrCommunity.COMMUNITY
  else -> CustodyOrCommunity.CUSTODY
}

private fun String?.toBoolean(): Boolean? = when (this) {
  "'YES'" -> true
  "'NO'" -> false
  "'NULL'" -> null
  else -> null
}
