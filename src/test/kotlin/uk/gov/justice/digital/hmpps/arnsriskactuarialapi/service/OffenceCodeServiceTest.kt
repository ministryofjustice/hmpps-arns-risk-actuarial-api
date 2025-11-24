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
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.HoCode
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.HoCodeErrorCode
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.HoCodeFlag
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.HoCodeWeighting
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeValues
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeWeighting
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
          HoCodeFlag(name = "opdViolSex", value = true),
        ),
        weightings = listOf(
          HoCodeWeighting(
            name = "ogrs3Weighting",
            value = 0.0,
            description = "Violence",
            errorCode = null,
          ),
          HoCodeWeighting(
            name = "snsvDynamicWeighting",
            value = -0.006538498404,
            description = "Violence against the person",
            errorCode = null,
          ),
          HoCodeWeighting(
            name = "snsvStaticWeighting",
            value = 0.01927038224,
            description = "Violence against the person",
            errorCode = null,
          ),
          HoCodeWeighting(
            name = "snsvVatpDynamicWeighting",
            value = 0.204895023669854,
            description = "Violence against the person (ABH+)",
            errorCode = null,
          ),
          HoCodeWeighting(
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
          HoCodeFlag(name = "opdViolSex", value = false),
        ),
        weightings = listOf(
          HoCodeWeighting(
            name = "ogrs3Weighting",
            value = null,
            description = "Missing description",
            errorCode = HoCodeErrorCode.NEED_DETAILS_OF_EXACT_OFFENCE,
          ),
          HoCodeWeighting(
            name = "snsvDynamicWeighting",
            value = 0.0819545573517356,
            description = "Drunkenness",
            errorCode = null,
          ),
          HoCodeWeighting(
            name = "snsvStaticWeighting",
            value = 0.0841789642942883,
            description = "Drunkenness",
            errorCode = null,
          ),
          HoCodeWeighting(
            name = "snsvVatpDynamicWeighting",
            value = 0.0,
            description = "Drunkenness",
            errorCode = null,
          ),
          HoCodeWeighting(
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

    val expectedOffenceCodeMappings = mapOf(
      "00100" to OffenceCodeValues(
        ogrs3Weighting = OffenceCodeWeighting(0.0, null),
        snsvStaticWeighting = OffenceCodeWeighting(0.01927038224, null),
        snsvDynamicWeighting = OffenceCodeWeighting(-0.006538498404, null),
        snsvVatpStaticWeighting = OffenceCodeWeighting(0.238802610774108, null),
        snsvVatpDynamicWeighting = OffenceCodeWeighting(0.204895023669854, null),
        opdViolenceSexFlag = true,
      ),
      "08801" to OffenceCodeValues(
        ogrs3Weighting = OffenceCodeWeighting(null, OffenceCodeError.NEED_DETAILS_OF_EXACT_OFFENCE),
        snsvStaticWeighting = OffenceCodeWeighting(0.0841789642942883, null),
        snsvDynamicWeighting = OffenceCodeWeighting(0.0819545573517356, null),
        snsvVatpStaticWeighting = OffenceCodeWeighting(0.0, null),
        snsvVatpDynamicWeighting = OffenceCodeWeighting(0.0, null),
        opdViolenceSexFlag = false,
      ),
      "99999" to OffenceCodeValues(
        ogrs3Weighting = OffenceCodeWeighting(null, null),
        snsvStaticWeighting = OffenceCodeWeighting(null, null),
        snsvDynamicWeighting = OffenceCodeWeighting(null, null),
        snsvVatpStaticWeighting = OffenceCodeWeighting(null, null),
        snsvVatpDynamicWeighting = OffenceCodeWeighting(null, null),
        opdViolenceSexFlag = null,
      ),
    )

    verify(cacheService, times(1)).sync(expectedOffenceCodeMappings)
  }

  @Test
  fun `should throw exception when Manage Offences API returns empty list`() {
    whenever(manageOffencesClient.getActuarialMapping()).thenReturn(emptyList())

    val exception = assertThrows<IllegalStateException> {
      offenceCodeService.updateOffenceCodeMappings()
    }

    verify(manageOffencesClient, times(1)).getActuarialMapping()
    verify(cacheService, never()).sync(any())

    assertEquals("Received empty Actuarial Mapping list from Manage Offences API.", exception.message)
  }
}
