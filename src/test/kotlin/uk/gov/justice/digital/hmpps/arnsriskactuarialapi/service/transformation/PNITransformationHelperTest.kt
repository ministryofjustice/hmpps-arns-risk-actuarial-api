package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.pniRequest

class PNITransformationHelperTest {

  @Nested
  inner class SexDomainScoreTest {
    @Test
    fun `should return min score`() {
      val request = pniRequest(
        sexualPreoccupation = ProblemLevel.NO_PROBLEMS,
        sexualInterestsOffenceRelated = ProblemLevel.NO_PROBLEMS,
        emotionalCongruence = ProblemLevel.NO_PROBLEMS,
      )
      val (score, missing) = SexDomainScore.overallDomainScore(request)
      assertEquals(0, score)
      assertTrue(missing.isEmpty())
    }

    @Test
    fun `should return max score`() {
      val request = pniRequest(
        sexualPreoccupation = ProblemLevel.SIGNIFICANT_PROBLEMS,
        sexualInterestsOffenceRelated = ProblemLevel.SIGNIFICANT_PROBLEMS,
        emotionalCongruence = ProblemLevel.SIGNIFICANT_PROBLEMS,
        hasCommittedSexualOffence = true,
      )
      val (score, missing) = SexDomainScore.overallDomainScore(request)
      assertEquals(2, score)
      assertTrue(missing.isEmpty())
    }

    @Test
    fun `should ignore omissions when interim score of 4 or more`() {
      val request = pniRequest(
        sexualPreoccupation = ProblemLevel.SIGNIFICANT_PROBLEMS, // 2
        sexualInterestsOffenceRelated = ProblemLevel.SIGNIFICANT_PROBLEMS, // 2
        emotionalCongruence = null,
        hasCommittedSexualOffence = true,
      )
      val (score, missing) = SexDomainScore.overallDomainScore(request)
      assertEquals(2, score)
      assertEquals(0, missing.size)
    }

    @Test
    fun `should not ignore omissions when interim score is less than 4`() {
      val request = pniRequest(
        sexualPreoccupation = ProblemLevel.SIGNIFICANT_PROBLEMS,
        sexualInterestsOffenceRelated = null,
        emotionalCongruence = null,
        hasCommittedSexualOffence = true,
      )
      val (score, missing) = SexDomainScore.overallDomainScore(request)
      assertNull(score)
      assertEquals(2, missing.size)
      assertTrue(missing.contains("sexualInterestsOffenceRelated"))
      assertTrue(missing.contains("emotionalCongruence"))
    }

    @Test
    fun `should return null and report all missing fields`() {
      val request = pniRequest().copy(
        hasCommittedSexualOffence = true,
      )
      val (score, missing) = SexDomainScore.overallDomainScore(request)
      assertNull(score)
      assertEquals(3, missing.size)
      assertTrue(missing.contains("sexualPreoccupation"))
    }
  }

  @Test
  fun `should return 0 and when sexual domain precheck not met`() {
    val request = pniRequest().copy(
      hasCommittedSexualOffence = false,
    )
    val (score, missing) = SexDomainScore.overallDomainScore(request)
    assertEquals(0, score)
    assertEquals(0, missing.size)
  }

  @Nested
  inner class ThinkingDomainScoreTest {
    @Test
    fun `should return min score`() {
      val request = pniRequest(
        proCriminalAttitudes = ProblemLevel.NO_PROBLEMS,
        hostileOrientation = ProblemLevel.NO_PROBLEMS,
      )
      val (score, missing) = ThinkingDomainScore.overallDomainScore(request)
      assertEquals(0, score)
      assertTrue(missing.isEmpty())
    }

    @Test
    fun `should return max score`() {
      val request = pniRequest(
        proCriminalAttitudes = ProblemLevel.SIGNIFICANT_PROBLEMS,
        hostileOrientation = ProblemLevel.SIGNIFICANT_PROBLEMS,
      )
      val (score, missing) = ThinkingDomainScore.overallDomainScore(request)
      assertEquals(2, score)
      assertTrue(missing.isEmpty())
    }

    @Test
    fun `should ignore omissions when proCriminalAttitudes == 2`() {
      val request = pniRequest(
        proCriminalAttitudes = ProblemLevel.SIGNIFICANT_PROBLEMS,
        hostileOrientation = null,
      )
      val (score, missing) = ThinkingDomainScore.overallDomainScore(request)
      assertEquals(2, score)
      assertEquals(0, missing.size)
    }

    @Test
    fun `should not omissions when proCriminalAttitudes less than 2`() {
      val request = pniRequest(
        proCriminalAttitudes = ProblemLevel.SOME_PROBLEMS,
        hostileOrientation = null,
      )
      val (score, missing) = ThinkingDomainScore.overallDomainScore(request)
      assertNull(score)
      assertEquals(1, missing.size)
      assertTrue(missing.contains("hostileOrientation"))
    }

    @Test
    fun `should return null and report all missing fields`() {
      val request = pniRequest(
        proCriminalAttitudes = null,
        hostileOrientation = null,
      )
      val (score, missing) = ThinkingDomainScore.overallDomainScore(request)
      assertNull(score)
      assertEquals(2, missing.size)
      assertTrue(missing.contains("proCriminalAttitudes"))
      assertTrue(missing.contains("hostileOrientation"))
    }
  }

  @Nested
  inner class RelationshipDomainScoreTest {
    @Test
    fun `should return min score`() {
      val request = pniRequest(
        currentRelationshipFamilyMembers = ProblemLevel.NO_PROBLEMS,
        previousCloseRelationships = ProblemLevel.NO_PROBLEMS,
        easilyInfluencedByCriminals = ProblemLevel.NO_PROBLEMS,
        controllingBehaviour = ProblemLevel.NO_PROBLEMS,
      )
      val (score, missing) = RelationshipDomainScore.overallDomainScore(request)
      assertEquals(0, score)
      assertTrue(missing.isEmpty())
    }

    @Test
    fun `should return max score`() {
      val request = pniRequest(
        currentRelationshipFamilyMembers = ProblemLevel.SIGNIFICANT_PROBLEMS,
        previousCloseRelationships = ProblemLevel.SIGNIFICANT_PROBLEMS,
        easilyInfluencedByCriminals = ProblemLevel.SIGNIFICANT_PROBLEMS,
        controllingBehaviour = ProblemLevel.SIGNIFICANT_PROBLEMS,
      )
      val (score, missing) = RelationshipDomainScore.overallDomainScore(request)
      assertEquals(2, score)
      assertTrue(missing.isEmpty())
    }

    @Test
    fun `should ignore omissions when interim score of 5 or more`() {
      val request = pniRequest(
        currentRelationshipFamilyMembers = ProblemLevel.SIGNIFICANT_PROBLEMS,
        previousCloseRelationships = ProblemLevel.SIGNIFICANT_PROBLEMS,
        easilyInfluencedByCriminals = ProblemLevel.SIGNIFICANT_PROBLEMS,
        controllingBehaviour = null,
      )
      val (score, missing) = RelationshipDomainScore.overallDomainScore(request)
      assertEquals(2, score)
      assertEquals(0, missing.size)
    }

    @Test
    fun `should not ignore omissions when interim score is less than 4`() {
      val request = pniRequest(
        currentRelationshipFamilyMembers = ProblemLevel.SIGNIFICANT_PROBLEMS,
        previousCloseRelationships = ProblemLevel.SIGNIFICANT_PROBLEMS,
        easilyInfluencedByCriminals = null,
        controllingBehaviour = null,
      )
      val (score, missing) = RelationshipDomainScore.overallDomainScore(request)
      assertNull(score)
      assertEquals(2, missing.size)
      assertTrue(missing.contains("controllingBehaviour"))
      assertTrue(missing.contains("easilyInfluencedByCriminals"))
    }

    @Test
    fun `should return null and report all missing fields`() {
      val request = pniRequest(
        currentRelationshipFamilyMembers = null,
        previousCloseRelationships = null,
        easilyInfluencedByCriminals = null,
        controllingBehaviour = null,
      )
      val (score, missing) = RelationshipDomainScore.overallDomainScore(request)
      assertNull(score)
      assertEquals(4, missing.size)
      assertTrue(missing.contains("currentRelationshipFamilyMembers"))
      assertTrue(missing.contains("previousCloseRelationships"))
      assertTrue(missing.contains("controllingBehaviour"))
      assertTrue(missing.contains("easilyInfluencedByCriminals"))
    }
  }

  @Nested
  inner class SelfManagementDomainScoreTest {
    @Test
    fun `should return min score`() {
      val request = pniRequest(
        impulsivityBehaviour = ProblemLevel.NO_PROBLEMS,
        temperControl = ProblemLevel.NO_PROBLEMS,
        problemSolvingSkills = ProblemLevel.NO_PROBLEMS,
        difficultiesCoping = ProblemLevel.NO_PROBLEMS,
      )
      val (score, missing) = SelfManagementDomainScore.overallDomainScore(request)
      assertEquals(0, score)
      assertTrue(missing.isEmpty())
    }

    @Test
    fun `should return max score`() {
      val request = pniRequest(
        impulsivityBehaviour = ProblemLevel.SIGNIFICANT_PROBLEMS,
        temperControl = ProblemLevel.SIGNIFICANT_PROBLEMS,
        problemSolvingSkills = ProblemLevel.SIGNIFICANT_PROBLEMS,
        difficultiesCoping = ProblemLevel.SIGNIFICANT_PROBLEMS,
      )
      val (score, missing) = SelfManagementDomainScore.overallDomainScore(request)
      assertEquals(2, score)
      assertTrue(missing.isEmpty())
    }

    @Test
    fun `should ignore omissions when interim score of 5 or more`() {
      val request = pniRequest(
        impulsivityBehaviour = ProblemLevel.SIGNIFICANT_PROBLEMS,
        temperControl = ProblemLevel.SIGNIFICANT_PROBLEMS,
        problemSolvingSkills = ProblemLevel.SOME_PROBLEMS,
        difficultiesCoping = null,
      )
      val (score, missing) = SelfManagementDomainScore.overallDomainScore(request)
      assertEquals(2, score)
      assertEquals(0, missing.size)
    }

    @Test
    fun `should not ignore omissions when interim score is less than 5`() {
      val request = pniRequest(
        impulsivityBehaviour = ProblemLevel.SOME_PROBLEMS,
        temperControl = ProblemLevel.NO_PROBLEMS,
        problemSolvingSkills = ProblemLevel.NO_PROBLEMS,
        difficultiesCoping = null,
      )
      val (score, missing) = SelfManagementDomainScore.overallDomainScore(request)
      assertNull(score)
      assertEquals(1, missing.size)
      assertTrue(missing.contains("difficultiesCoping"))
    }

    @Test
    fun `should return null and report all missing fields`() {
      val request = pniRequest(
        impulsivityBehaviour = null,
        temperControl = null,
        problemSolvingSkills = null,
        difficultiesCoping = null,
      )
      val (score, missing) = SelfManagementDomainScore.overallDomainScore(request)
      assertNull(score)
      assertEquals(4, missing.size)
      assertTrue(missing.contains("impulsivityBehaviour"))
      assertTrue(missing.contains("temperControl"))
      assertTrue(missing.contains("problemSolvingSkills"))
      assertTrue(missing.contains("difficultiesCoping"))
    }
  }
}
