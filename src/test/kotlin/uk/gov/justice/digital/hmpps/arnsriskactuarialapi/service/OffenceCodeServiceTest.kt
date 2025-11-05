package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.HoCode
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.HoCodeErrorCode
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.HoCodeFlags
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.HoCodeWeightings
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeValues
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.restclient.ManageOffencesApiRestClient

class OffenceCodeServiceTest {

  private val manageOffencesClient: ManageOffencesApiRestClient = mock()
  private val cacheService: OffenceCodeCacheService = mock()
  private val offenceCodeService: OffenceCodeService = OffenceCodeService(manageOffencesClient, cacheService)

  @Test
  fun `should update offence code mappings with valid Manage Offences API response`() {
    val actuarialMappingResponse = listOf(
      HoCode(
        category = 1,
        subCategory = 0,
        flags = listOf(
          HoCodeFlags(name = "opdViolSex", value = true),
        ),
        weightings = listOf(
          HoCodeWeightings(
            name = "ogrs3Weighting",
            value = 0.0,
            description = "Violence",
            errorCode = null,
          ),
          HoCodeWeightings(
            name = "snsvDynamicWeighting",
            value = -0.006538498404,
            description = "Violence against the person",
            errorCode = null,
          ),
          HoCodeWeightings(
            name = "snsvStaticWeighting",
            value = 0.01927038224,
            description = "Violence against the person",
            errorCode = null,
          ),
          HoCodeWeightings(
            name = "snsvVatpDynamicWeighting",
            value = 0.204895023669854,
            description = "Violence against the person (ABH+)",
            errorCode = null,
          ),
          HoCodeWeightings(
            name = "snsvVatpStaticWeighting",
            value = 0.238802610774108,
            description = "Violence against the person (ABH+)",
            errorCode = null,
          ),
        ),
      ),
      HoCode(
        category = 88,
        subCategory = 1,
        flags = listOf(
          HoCodeFlags(name = "opdViolSex", value = false),
        ),
        weightings = listOf(
          HoCodeWeightings(
            name = "ogrs3Weighting",
            value = null,
            description = "Missing description",
            errorCode = HoCodeErrorCode.NEED_DETAILS_OF_EXACT_OFFENCE,
          ),
          HoCodeWeightings(
            name = "snsvDynamicWeighting",
            value = 0.0819545573517356,
            description = "Drunkenness",
            errorCode = null,
          ),
          HoCodeWeightings(
            name = "snsvStaticWeighting",
            value = 0.0841789642942883,
            description = "Drunkenness",
            errorCode = null,
          ),
          HoCodeWeightings(
            name = "snsvVatpDynamicWeighting",
            value = 0.0,
            description = "Drunkenness",
            errorCode = null,
          ),
          HoCodeWeightings(
            name = "snsvVatpStaticWeighting",
            value = 0.0,
            description = "Drunkenness",
            errorCode = null,
          ),
        ),
      ),
      HoCode(
        category = 999,
        subCategory = 99,
        flags = emptyList(),
        weightings = emptyList(),
      ),
    )

    whenever(manageOffencesClient.getActuarialMapping()).thenReturn(actuarialMappingResponse)

    offenceCodeService.updateOffenceCodeMappings()

    verify(manageOffencesClient, times(1)).getActuarialMapping()
    verify(cacheService, times(1)).deleteAll()

    val expectedOffenceCodeMappings = mapOf(
      "00100" to OffenceCodeValues(
        ogrs3Weighting = 0.0,
        snsvStaticWeighting = 0.01927038224,
        snsvDynamicWeighting = -0.006538498404,
        snsvVatpStaticWeighting = 0.238802610774108,
        snsvVatpDynamicWeighting = 0.204895023669854,
        opdViolenceSexFlag = true,
      ),
      "08801" to OffenceCodeValues(
        ogrs3Weighting = null,
        snsvStaticWeighting = 0.0841789642942883,
        snsvDynamicWeighting = 0.0819545573517356,
        snsvVatpStaticWeighting = 0.0,
        snsvVatpDynamicWeighting = 0.0,
        opdViolenceSexFlag = false,
      ),
      "99999" to OffenceCodeValues(
        ogrs3Weighting = null,
        snsvStaticWeighting = null,
        snsvDynamicWeighting = null,
        snsvVatpStaticWeighting = null,
        snsvVatpDynamicWeighting = null,
        opdViolenceSexFlag = null,
      ),
    )

    verify(cacheService, times(1)).saveAll(expectedOffenceCodeMappings)
  }

  @Test
  fun `should throw exception when Manage Offences API returns empty list`() {
    whenever(manageOffencesClient.getActuarialMapping()).thenReturn(emptyList())

    val exception = assertThrows<IllegalStateException> {
      offenceCodeService.updateOffenceCodeMappings()
    }

    verify(manageOffencesClient, times(1)).getActuarialMapping()
    verify(cacheService, never()).deleteAll()
    verify(cacheService, never()).saveAll(any())

    assertEquals("Received empty Actuarial Mapping list from Manage Offences API.", exception.message)
  }
}