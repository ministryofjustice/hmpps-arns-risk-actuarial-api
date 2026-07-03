package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest

class ValidationUtilsTest {
  @Test
  fun `addIfNull should field name when property is null`() {
    val request = RiskScoreRequest(hasPeerGroupInfluences = null)
    val missingFields = arrayListOf<String>()

    missingFields.addIfNull(request, RiskScoreRequest::hasPeerGroupInfluences)
    assertEquals(listOf("hasPeerGroupInfluences"), missingFields)

    missingFields.addIfNull(request, RiskScoreRequest::gender)
    assertEquals(listOf("hasPeerGroupInfluences", "gender"), missingFields)
  }

  @Test
  fun `addIfNull should not add field name when property is not null`() {
    val request = RiskScoreRequest(hasPeerGroupInfluences = true)
    val missingFields = arrayListOf<String>()

    missingFields.addIfNull(request, RiskScoreRequest::hasPeerGroupInfluences)
    assertEquals(emptyList<String>(), missingFields)
  }

  @Test
  fun `addIfNotNullAndNotZero should not add field name when property is null`() {
    val request = RiskScoreRequest(totalNonContactSexualOffences = null)
    val missingFields = arrayListOf<String>()

    missingFields.addIfNotNullAndNotZero(request, RiskScoreRequest::totalNonContactSexualOffences)
    assertEquals(emptyList<String>(), missingFields)
  }

  @Test
  fun `addIfNotNullAndNotZero should not add field name when property is 0`() {
    val request = RiskScoreRequest(totalNonContactSexualOffences = 0)
    val missingFields = arrayListOf<String>()

    missingFields.addIfNotNullAndNotZero(request, RiskScoreRequest::totalNonContactSexualOffences)
    assertEquals(emptyList<String>(), missingFields)
  }

  @Test
  fun `addIfNotNullAndNotZero should add field name when property is not null and not 0`() {
    val request = RiskScoreRequest(totalNonContactSexualOffences = 1)
    val existingFields = arrayListOf<String>()

    existingFields.addIfNotNullAndNotZero(request, RiskScoreRequest::totalNonContactSexualOffences)
    assertEquals(listOf("totalNonContactSexualOffences"), existingFields)
  }

  @Test
  fun `test getTrueKeys - all null`() {
    val map = mapOf(
      RiskScoreRequest::hasHeroinUsage to null,
      RiskScoreRequest::hasSpiceUsage to null,
      RiskScoreRequest::hasCannabisUsage to null,
    )

    assertEquals(emptyList<String>(), map.getTrueKeys())
  }

  @Test
  fun `test getTrueKeys - all false`() {
    val map = mapOf(
      RiskScoreRequest::hasHeroinUsage to false,
      RiskScoreRequest::hasSpiceUsage to false,
      RiskScoreRequest::hasCannabisUsage to false,
    )

    assertEquals(emptyList<String>(), map.getTrueKeys())
  }

  @Test
  fun `test getTrueKeys - all true`() {
    val map = mapOf(
      RiskScoreRequest::hasHeroinUsage to true,
      RiskScoreRequest::hasSpiceUsage to true,
      RiskScoreRequest::hasCannabisUsage to true,
    )

    assertEquals(listOf("hasHeroinUsage", "hasSpiceUsage", "hasCannabisUsage"), map.getTrueKeys())
  }

  @Test
  fun `test getTrueKeys - mix`() {
    val map = mapOf(
      RiskScoreRequest::hasHeroinUsage to true,
      RiskScoreRequest::hasSpiceUsage to false,
      RiskScoreRequest::hasCannabisUsage to true,
      RiskScoreRequest::hasOtherOpiateUsage to null,
    )

    assertEquals(listOf("hasHeroinUsage", "hasCannabisUsage"), map.getTrueKeys())
  }

  @Test
  fun `sumIntValues should calculate summary for non null values`() {
    val request = RiskScoreRequest(
      totalNonContactSexualOffences = 1,
      totalIndecentImageSanctions = null,
      totalContactChildSexualSanctions = 3,
      totalContactAdultSexualSanctions = null,
    )

    val requiredSexualFields = listOf(
      RiskScoreRequest::totalIndecentImageSanctions,
      RiskScoreRequest::totalContactAdultSexualSanctions,
      RiskScoreRequest::totalContactChildSexualSanctions,
      RiskScoreRequest::totalNonContactSexualOffences,
    )

    val sum = requiredSexualFields.sumIntValues(request)
    assertEquals(4, sum)
  }
}
