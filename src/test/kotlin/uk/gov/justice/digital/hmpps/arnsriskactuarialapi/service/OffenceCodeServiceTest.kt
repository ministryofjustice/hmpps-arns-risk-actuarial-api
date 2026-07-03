package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.ActuarialCategory
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeDetails
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeFlags
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.restclient.ManageOffencesApiRestClient

class OffenceCodeServiceTest {

  private val manageOffencesClient: ManageOffencesApiRestClient = mock()
  private val cacheService: OffenceCodeCacheService = mock()
  private val offenceCodeService: OffenceCodeService = OffenceCodeService(manageOffencesClient, cacheService)

  @Test
  fun `should update offence code mappings with valid Manage Offences API response`() {
    val actuarialMappingResponse = mapOf(
      "10000" to OffenceCodeDetails(
        parentGroupDescription = "parent group description 10000",
        categoryDescription = "category description 10000",
        subCategoryDescription = "sub category description 10000",
        actuarialCategory = ActuarialCategory.SEXUAL_NOT_AGAINST_CHILD,
        flags = OffenceCodeFlags(
          opdViolenceSex = true,
          isViolentSanction = true,
        ),
      ),
      "20000" to OffenceCodeDetails(
        parentGroupDescription = "parent group description 20000",
        categoryDescription = "category description 20000",
        subCategoryDescription = "sub category description 20000",
        actuarialCategory = ActuarialCategory.DRUNKENNESS,
        flags = OffenceCodeFlags(
          opdViolenceSex = false,
          isViolentSanction = true,
        ),
      ),
      "30000" to OffenceCodeDetails(
        parentGroupDescription = "parent group description 30000",
        categoryDescription = "category description 30000",
        subCategoryDescription = "sub category description 30000",
        actuarialCategory = ActuarialCategory.FRAUD_AND_FORGERY,
        flags = OffenceCodeFlags(
          opdViolenceSex = false,
          isViolentSanction = false,
        ),
      ),
    )

    whenever(manageOffencesClient.getActuarialMapping()).thenReturn(actuarialMappingResponse)

    offenceCodeService.updateOffenceCodeMappings()

    verify(manageOffencesClient, times(1)).getActuarialMapping()

    val expectedOffenceCodeMappings = mapOf(
      "10000" to OffenceCodeDetails(
        parentGroupDescription = "parent group description 10000",
        categoryDescription = "category description 10000",
        subCategoryDescription = "sub category description 10000",
        actuarialCategory = ActuarialCategory.SEXUAL_NOT_AGAINST_CHILD,
        flags = OffenceCodeFlags(
          opdViolenceSex = true,
          isViolentSanction = true,
        ),
      ),
      "20000" to OffenceCodeDetails(
        parentGroupDescription = "parent group description 20000",
        categoryDescription = "category description 20000",
        subCategoryDescription = "sub category description 20000",
        actuarialCategory = ActuarialCategory.DRUNKENNESS,
        flags = OffenceCodeFlags(
          opdViolenceSex = false,
          isViolentSanction = true,
        ),
      ),
      "30000" to OffenceCodeDetails(
        parentGroupDescription = "parent group description 30000",
        categoryDescription = "category description 30000",
        subCategoryDescription = "sub category description 30000",
        actuarialCategory = ActuarialCategory.FRAUD_AND_FORGERY,
        flags = OffenceCodeFlags(
          opdViolenceSex = false,
          isViolentSanction = false,
        ),
      ),
    )

    verify(cacheService, times(1)).sync(expectedOffenceCodeMappings)
  }

  @Test
  fun `should throw exception when Manage Offences API returns empty list`() {
    whenever(manageOffencesClient.getActuarialMapping()).thenReturn(emptyMap())

    val exception = assertThrows<IllegalStateException> {
      offenceCodeService.updateOffenceCodeMappings()
    }

    verify(manageOffencesClient, times(1)).getActuarialMapping()
    verify(cacheService, never()).sync(any())

    assertEquals("Received offence code map containing no entries from Manage Offences API.", exception.message)
  }
}
