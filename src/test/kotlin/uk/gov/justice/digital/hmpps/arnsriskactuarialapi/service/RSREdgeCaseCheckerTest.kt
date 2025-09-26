package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import kotlin.test.assertEquals

class RSREdgeCaseCheckerTest {

  @ParameterizedTest
  @MethodSource("sexualSectionAllNullCases")
  fun `test sexualSectionAllNull`(request: RiskScoreRequest, expected: Boolean) {
    val result = sexualSectionAllNull(request)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("sexualOffenceHistoryTrueButSanctionsZeroCases")
  fun `test sexualOffenceHistoryTrueButSanctionsZero`(request: RiskScoreRequest, expected: Boolean) {
    val result = sexualOffenceHistoryTrueButSanctionsZero(request)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("sexualOffenceHistoryFalseButSanctionsNullCases")
  fun `test sexualOffenceHistoryFalseButSanctionsNull`(request: RiskScoreRequest, expected: Boolean) {
    val result = sexualOffenceHistoryFalseButSanctionsNull(request)
    assertEquals(expected, result)
  }

  companion object {
    @JvmStatic
    fun sexualSectionAllNullCases() = listOf(
      // All nulls -> should return true
      Arguments.of(
        RiskScoreRequest(
          hasEverCommittedSexualOffence = null,
          isCurrentOffenceSexuallyMotivated = null,
          totalContactAdultSexualSanctions = null,
          totalContactChildSexualSanctions = null,
          totalNonContactSexualOffences = null,
          totalIndecentImageSanctions = null,
        ),
        true,
      ),
      // Each field individually non-null -> should return false
      Arguments.of(
        RiskScoreRequest(
          hasEverCommittedSexualOffence = true,
          isCurrentOffenceSexuallyMotivated = null,
          totalContactAdultSexualSanctions = null,
          totalContactChildSexualSanctions = null,
          totalNonContactSexualOffences = null,
          totalIndecentImageSanctions = null,
        ),
        false,
      ),
      Arguments.of(
        RiskScoreRequest(
          hasEverCommittedSexualOffence = null,
          isCurrentOffenceSexuallyMotivated = false,
          totalContactAdultSexualSanctions = null,
          totalContactChildSexualSanctions = null,
          totalNonContactSexualOffences = null,
          totalIndecentImageSanctions = null,
        ),
        false,
      ),
      Arguments.of(
        RiskScoreRequest(
          hasEverCommittedSexualOffence = null,
          isCurrentOffenceSexuallyMotivated = null,
          totalContactAdultSexualSanctions = 1,
          totalContactChildSexualSanctions = null,
          totalNonContactSexualOffences = null,
          totalIndecentImageSanctions = null,
        ),
        false,
      ),
      Arguments.of(
        RiskScoreRequest(
          hasEverCommittedSexualOffence = null,
          isCurrentOffenceSexuallyMotivated = null,
          totalContactAdultSexualSanctions = null,
          totalContactChildSexualSanctions = 1,
          totalNonContactSexualOffences = null,
          totalIndecentImageSanctions = null,
        ),
        false,
      ),
      Arguments.of(
        RiskScoreRequest(
          hasEverCommittedSexualOffence = null,
          isCurrentOffenceSexuallyMotivated = null,
          totalContactAdultSexualSanctions = null,
          totalContactChildSexualSanctions = null,
          totalNonContactSexualOffences = 1,
          totalIndecentImageSanctions = null,
        ),
        false,
      ),
      Arguments.of(
        RiskScoreRequest(
          hasEverCommittedSexualOffence = null,
          isCurrentOffenceSexuallyMotivated = null,
          totalContactAdultSexualSanctions = null,
          totalContactChildSexualSanctions = null,
          totalNonContactSexualOffences = null,
          totalIndecentImageSanctions = 1,
        ),
        false,
      ),
    )

    @JvmStatic
    fun sexualOffenceHistoryTrueButSanctionsZeroCases() = listOf(
      // All conditions met -> should return true
      Arguments.of(
        RiskScoreRequest(
          hasEverCommittedSexualOffence = true,
          isCurrentOffenceSexuallyMotivated = true,
          totalContactAdultSexualSanctions = 0,
          totalContactChildSexualSanctions = 0,
          totalNonContactSexualOffences = 0,
          totalIndecentImageSanctions = 0,
        ),
        true,
      ),
      // Any field not meeting condition -> should return false
      Arguments.of(
        RiskScoreRequest(
          hasEverCommittedSexualOffence = false, // Not true
          isCurrentOffenceSexuallyMotivated = true,
          totalContactAdultSexualSanctions = 0,
          totalContactChildSexualSanctions = 0,
          totalNonContactSexualOffences = 0,
          totalIndecentImageSanctions = 0,
        ),
        false,
      ),
      Arguments.of(
        RiskScoreRequest(
          hasEverCommittedSexualOffence = true,
          isCurrentOffenceSexuallyMotivated = false, // Not true
          totalContactAdultSexualSanctions = 0,
          totalContactChildSexualSanctions = 0,
          totalNonContactSexualOffences = 0,
          totalIndecentImageSanctions = 0,
        ),
        false,
      ),
      Arguments.of(
        RiskScoreRequest(
          hasEverCommittedSexualOffence = true,
          isCurrentOffenceSexuallyMotivated = true,
          totalContactAdultSexualSanctions = 1, // Not zero
          totalContactChildSexualSanctions = 0,
          totalNonContactSexualOffences = 0,
          totalIndecentImageSanctions = 0,
        ),
        false,
      ),
      Arguments.of(
        RiskScoreRequest(
          hasEverCommittedSexualOffence = true,
          isCurrentOffenceSexuallyMotivated = true,
          totalContactAdultSexualSanctions = 0,
          totalContactChildSexualSanctions = 1, // Not zero
          totalNonContactSexualOffences = 0,
          totalIndecentImageSanctions = 0,
        ),
        false,
      ),
      Arguments.of(
        RiskScoreRequest(
          hasEverCommittedSexualOffence = true,
          isCurrentOffenceSexuallyMotivated = true,
          totalContactAdultSexualSanctions = 0,
          totalContactChildSexualSanctions = 0,
          totalNonContactSexualOffences = 1, // Not zero
          totalIndecentImageSanctions = 0,
        ),
        false,
      ),
      Arguments.of(
        RiskScoreRequest(
          hasEverCommittedSexualOffence = true,
          isCurrentOffenceSexuallyMotivated = true,
          totalContactAdultSexualSanctions = 0,
          totalContactChildSexualSanctions = 0,
          totalNonContactSexualOffences = 0,
          totalIndecentImageSanctions = 1, // Not zero
        ),
        false,
      ),
    )

    @JvmStatic
    fun sexualOffenceHistoryFalseButSanctionsNullCases() = listOf(
      // All conditions met -> should return true
      Arguments.of(
        RiskScoreRequest(
          hasEverCommittedSexualOffence = false,
          isCurrentOffenceSexuallyMotivated = null,
          totalContactAdultSexualSanctions = null,
          totalContactChildSexualSanctions = null,
          totalNonContactSexualOffences = null,
          totalIndecentImageSanctions = null,
        ),
        true,
      ),
      // Any field not meeting condition -> should return false
      Arguments.of(
        RiskScoreRequest(
          hasEverCommittedSexualOffence = true, // Not false
          isCurrentOffenceSexuallyMotivated = null,
          totalContactAdultSexualSanctions = null,
          totalContactChildSexualSanctions = null,
          totalNonContactSexualOffences = null,
          totalIndecentImageSanctions = null,
        ),
        false,
      ),
      Arguments.of(
        RiskScoreRequest(
          hasEverCommittedSexualOffence = false,
          isCurrentOffenceSexuallyMotivated = true, // Not null
          totalContactAdultSexualSanctions = null,
          totalContactChildSexualSanctions = null,
          totalNonContactSexualOffences = null,
          totalIndecentImageSanctions = null,
        ),
        false,
      ),
      Arguments.of(
        RiskScoreRequest(
          hasEverCommittedSexualOffence = false,
          isCurrentOffenceSexuallyMotivated = null,
          totalContactAdultSexualSanctions = 0, // Not null
          totalContactChildSexualSanctions = null,
          totalNonContactSexualOffences = null,
          totalIndecentImageSanctions = null,
        ),
        false,
      ),
      Arguments.of(
        RiskScoreRequest(
          hasEverCommittedSexualOffence = false,
          isCurrentOffenceSexuallyMotivated = null,
          totalContactAdultSexualSanctions = null,
          totalContactChildSexualSanctions = 0, // Not null
          totalNonContactSexualOffences = null,
          totalIndecentImageSanctions = null,
        ),
        false,
      ),
      Arguments.of(
        RiskScoreRequest(
          hasEverCommittedSexualOffence = false,
          isCurrentOffenceSexuallyMotivated = null,
          totalContactAdultSexualSanctions = null,
          totalContactChildSexualSanctions = null,
          totalNonContactSexualOffences = 0, // Not null
          totalIndecentImageSanctions = null,
        ),
        false,
      ),
      Arguments.of(
        RiskScoreRequest(
          hasEverCommittedSexualOffence = false,
          isCurrentOffenceSexuallyMotivated = null,
          totalContactAdultSexualSanctions = null,
          totalContactChildSexualSanctions = null,
          totalNonContactSexualOffences = null,
          totalIndecentImageSanctions = 0, // Not null
        ),
        false,
      ),
    )
  }
}
