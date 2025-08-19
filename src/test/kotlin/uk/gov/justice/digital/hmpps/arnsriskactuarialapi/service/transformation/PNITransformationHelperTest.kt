package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.pniRequest

class PNITransformationHelperTest {

  @Nested
  inner class SexDomainScoreTest {
    @Test
    fun `should return min score`() {
      val request = pniRequest(
        sexualPreoccupation = ProblemLevel.NO_PROBLEMS,
        offenceRelatedSexualInterests = ProblemLevel.NO_PROBLEMS,
        emotionalCongruenceWithChildren = ProblemLevel.NO_PROBLEMS,
      )
      val (score, projected, missing) = SexDomainScore.overallDomainScore(request)
      assertEquals(0, score)
      assertTrue(missing.isEmpty())
    }

    @Test
    fun `should return max score`() {
      val request = pniRequest(
        sexualPreoccupation = ProblemLevel.SIGNIFICANT_PROBLEMS,
        offenceRelatedSexualInterests = ProblemLevel.SIGNIFICANT_PROBLEMS,
        emotionalCongruenceWithChildren = ProblemLevel.SIGNIFICANT_PROBLEMS,
        hasEverCommittedSexualOffence = true,
      )
      val (score, projected, missing) = SexDomainScore.overallDomainScore(request)
      assertEquals(2, score)
      assertTrue(missing.isEmpty())
    }

    @Test
    fun `should ignore omissions when interim score of 4 or more`() {
      val request = pniRequest(
        sexualPreoccupation = ProblemLevel.SIGNIFICANT_PROBLEMS, // 2
        offenceRelatedSexualInterests = ProblemLevel.SIGNIFICANT_PROBLEMS, // 2
        emotionalCongruenceWithChildren = null,
        hasEverCommittedSexualOffence = true,
      )
      val (score, projected, missing) = SexDomainScore.overallDomainScore(request)
      assertEquals(2, score)
      assertEquals(1, missing.size)
    }

    @Test
    fun `should not ignore omissions when interim score is less than 4`() {
      val request = pniRequest(
        sexualPreoccupation = ProblemLevel.SIGNIFICANT_PROBLEMS,
        offenceRelatedSexualInterests = null,
        emotionalCongruenceWithChildren = null,
        hasEverCommittedSexualOffence = true,
      )
      val (score, projected, missing) = SexDomainScore.overallDomainScore(request)
      assertEquals(0, score)
      assertEquals(2, missing.size)
      assertTrue(missing.contains("offenceRelatedSexualInterests"))
      assertTrue(missing.contains("emotionalCongruenceWithChildren"))
    }

    @Test
    fun `should return null and report all missing fields`() {
      val request = pniRequest().copy(
        hasEverCommittedSexualOffence = true,
      )
      val (score, projected, missing) = SexDomainScore.overallDomainScore(request)
      assertEquals(0, score)
      assertEquals(3, missing.size)
      assertTrue(missing.contains("sexualPreoccupation"))
    }
  }

  @Nested
  inner class ThinkingDomainScoreTest {
    @Test
    fun `should return min score`() {
      val request = pniRequest(
        proCriminalAttitudes = ProblemLevel.NO_PROBLEMS,
        hostileOrientation = ProblemLevel.NO_PROBLEMS,
      )
      val (score, projected, missing) = ThinkingDomainScore.overallDomainScore(request)
      assertEquals(0, score)
      assertTrue(missing.isEmpty())
    }

    @Test
    fun `should return max score`() {
      val request = pniRequest(
        proCriminalAttitudes = ProblemLevel.SIGNIFICANT_PROBLEMS,
        hostileOrientation = ProblemLevel.SIGNIFICANT_PROBLEMS,
      )
      val (score, projected, missing) = ThinkingDomainScore.overallDomainScore(request)
      assertEquals(2, score)
      assertTrue(missing.isEmpty())
    }

    @Test
    fun `should ignore omissions when proCriminalAttitudes == 2`() {
      val request = pniRequest(
        proCriminalAttitudes = ProblemLevel.SIGNIFICANT_PROBLEMS,
        hostileOrientation = null,
      )
      val (score, projected, missing) = ThinkingDomainScore.overallDomainScore(request)
      assertEquals(2, score)
      assertEquals(1, missing.size)
    }

    @Test
    fun `should partially score domain`() {
      val request = pniRequest(
        proCriminalAttitudes = ProblemLevel.SOME_PROBLEMS,
        hostileOrientation = null,
      )
      val (score, projected, missing) = ThinkingDomainScore.overallDomainScore(request)
      assertEquals(1, score)
      assertEquals(1, missing.size)
      assertTrue(missing.contains("hostileOrientation"))
    }

    @Test
    fun `should return null and report all missing fields`() {
      val request = pniRequest(
        proCriminalAttitudes = null,
        hostileOrientation = null,
      )
      val (score, projected, missing) = ThinkingDomainScore.overallDomainScore(request)
      assertEquals(0, score)
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
        currentRelationshipWithFamilyMembers = ProblemLevel.NO_PROBLEMS,
        previousCloseRelationships = ProblemLevel.NO_PROBLEMS,
        easilyInfluencedByCriminalAssociates = ProblemLevel.NO_PROBLEMS,
        controllingOrAggressiveBehaviour = ProblemLevel.NO_PROBLEMS,
      )
      val (score, projected, missing) = RelationshipDomainScore.overallDomainScore(request)
      assertEquals(0, score)
      assertTrue(missing.isEmpty())
    }

    @Test
    fun `should return max score`() {
      val request = pniRequest(
        currentRelationshipWithFamilyMembers = ProblemLevel.SIGNIFICANT_PROBLEMS,
        previousCloseRelationships = ProblemLevel.SIGNIFICANT_PROBLEMS,
        easilyInfluencedByCriminalAssociates = ProblemLevel.SIGNIFICANT_PROBLEMS,
        controllingOrAggressiveBehaviour = ProblemLevel.SIGNIFICANT_PROBLEMS,
      )
      val (score, projected, missing) = RelationshipDomainScore.overallDomainScore(request)
      assertEquals(2, score)
      assertTrue(missing.isEmpty())
    }

    @Test
    fun `should ignore omissions when interim score of 5 or more`() {
      val request = pniRequest(
        currentRelationshipWithFamilyMembers = ProblemLevel.SIGNIFICANT_PROBLEMS,
        previousCloseRelationships = ProblemLevel.SIGNIFICANT_PROBLEMS,
        easilyInfluencedByCriminalAssociates = ProblemLevel.SIGNIFICANT_PROBLEMS,
        controllingOrAggressiveBehaviour = null,
      )
      val (score, projected, missing) = RelationshipDomainScore.overallDomainScore(request)
      assertEquals(2, score)
      assertEquals(1, missing.size)
    }

    @Test
    fun `should return null and report all missing fields`() {
      val request = pniRequest(
        currentRelationshipWithFamilyMembers = null,
        previousCloseRelationships = null,
        easilyInfluencedByCriminalAssociates = null,
        controllingOrAggressiveBehaviour = null,
      )
      val (score, projected, missing) = RelationshipDomainScore.overallDomainScore(request)
      assertEquals(0, score)
      assertEquals(4, missing.size)
      assertTrue(missing.contains("currentRelationshipWithFamilyMembers"))
      assertTrue(missing.contains("previousCloseRelationships"))
      assertTrue(missing.contains("controllingOrAggressiveBehaviour"))
      assertTrue(missing.contains("easilyInfluencedByCriminalAssociates"))
    }
  }

  @Nested
  inner class SelfManagementDomainScoreTest {
    @Test
    fun `should return min score`() {
      val request = pniRequest(
        impulsivityProblems = ProblemLevel.NO_PROBLEMS,
        temperControl = ProblemLevel.NO_PROBLEMS,
        problemSolvingSkills = ProblemLevel.NO_PROBLEMS,
        difficultiesCoping = ProblemLevel.NO_PROBLEMS,
      )
      val (score, projected, missing) = SelfManagementDomainScore.overallDomainScore(request)
      assertEquals(0, score)
      assertTrue(missing.isEmpty())
    }

    @Test
    fun `should return max score`() {
      val request = pniRequest(
        impulsivityProblems = ProblemLevel.SIGNIFICANT_PROBLEMS,
        temperControl = ProblemLevel.SIGNIFICANT_PROBLEMS,
        problemSolvingSkills = ProblemLevel.SIGNIFICANT_PROBLEMS,
        difficultiesCoping = ProblemLevel.SIGNIFICANT_PROBLEMS,
      )
      val (score, projected, missing) = SelfManagementDomainScore.overallDomainScore(request)
      assertEquals(2, score)
      assertTrue(missing.isEmpty())
    }

    @Test
    fun `should ignore omissions when interim score of 5 or more`() {
      val request = pniRequest(
        impulsivityProblems = ProblemLevel.SIGNIFICANT_PROBLEMS,
        temperControl = ProblemLevel.SIGNIFICANT_PROBLEMS,
        problemSolvingSkills = ProblemLevel.SOME_PROBLEMS,
        difficultiesCoping = null,
      )
      val (score, projected, missing) = SelfManagementDomainScore.overallDomainScore(request)
      assertEquals(2, score)
      assertEquals(1, missing.size)
    }

    @Test
    fun `should not ignore omissions when interim score is less than 5`() {
      val request = pniRequest(
        impulsivityProblems = ProblemLevel.SOME_PROBLEMS,
        temperControl = ProblemLevel.NO_PROBLEMS,
        problemSolvingSkills = ProblemLevel.NO_PROBLEMS,
        difficultiesCoping = null,
      )
      val (score, projected, missing) = SelfManagementDomainScore.overallDomainScore(request)
      assertEquals(0, score)
      assertEquals(1, missing.size)
      assertTrue(missing.contains("difficultiesCoping"))
    }

    @Test
    fun `should return null and report all missing fields`() {
      val request = pniRequest(
        impulsivityProblems = null,
        temperControl = null,
        problemSolvingSkills = null,
        difficultiesCoping = null,
      )
      val (score, projected, missing) = SelfManagementDomainScore.overallDomainScore(request)
      assertEquals(0, score)
      assertEquals(4, missing.size)
      assertTrue(missing.contains("impulsivityProblems"))
      assertTrue(missing.contains("temperControl"))
      assertTrue(missing.contains("problemSolvingSkills"))
      assertTrue(missing.contains("difficultiesCoping"))
    }
  }
}
