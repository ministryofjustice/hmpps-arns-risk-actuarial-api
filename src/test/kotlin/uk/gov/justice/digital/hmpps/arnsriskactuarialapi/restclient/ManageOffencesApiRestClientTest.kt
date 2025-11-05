package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.restclient

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.HoCode
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.HoCodeErrorCode
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.HoCodeFlags
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.HoCodeWeightings
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.exceptions.ExternalApiAuthorisationException
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.exceptions.ExternalApiForbiddenException
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.exceptions.ExternalApiUnknownException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockitoExtension::class)
class ManageOffencesApiRestClientTest {

  private val webClientMock: AuthenticatingRestClient = mock()
  private val requestHeadersSpecMock: RequestHeadersSpec<*> = mock()
  private val responseSpecMock: ResponseSpec = mock()

  private lateinit var apiClient: ManageOffencesApiRestClient

  @BeforeEach
  fun setup() {
    apiClient = ManageOffencesApiRestClient(webClientMock)

    whenever(webClientMock.get("/actuarial-mapping")) doReturn requestHeadersSpecMock
    whenever(requestHeadersSpecMock.retrieve()) doReturn responseSpecMock
    whenever(responseSpecMock.onStatus(any(), any())) doReturn responseSpecMock
  }

  @Test
  fun `getActuarialMapping returns list of HoCode`() {
    val expectedHoCodes = listOf(
      HoCode(
        category = 1,
        subCategory = 0,
        flags = listOf(
          HoCodeFlags(name = "FLAG_ONE", value = true),
        ),
        weightings = listOf(
          HoCodeWeightings(
            name = "WEIGHTING_ONE",
            value = 1.0,
            description = "First weighting description",
            errorCode = null,
          ),
          HoCodeWeightings(
            name = "WEIGHTING_TWO",
            value = 2.5,
            description = "Second weighting description",
            errorCode = null,
          ),
        ),
      ),
      HoCode(
        category = 1,
        subCategory = 1,
        flags = listOf(
          HoCodeFlags(name = "FLAG_ONE", value = true),
          HoCodeFlags(name = "FLAG_TWO", value = false),
        ),
        weightings = listOf(
          HoCodeWeightings(
            name = "WEIGHTING_ONE",
            value = null,
            description = "First weighting description",
            errorCode = HoCodeErrorCode.NEED_DETAILS_OF_EXACT_OFFENCE,
          ),
        ),
      ),
    )

    whenever(responseSpecMock.bodyToMono(any<ParameterizedTypeReference<List<HoCode>>>()))
      .doReturn(Mono.just(expectedHoCodes))

    val result = apiClient.getActuarialMapping()

    assertEquals(expectedHoCodes.size, result.size)
    assertEquals(expectedHoCodes, result)
  }

//  @Test
//  fun `getActuarialMapping throws ExternalApiAuthorisationException on 401`() {
//
//
//    val thrown = assertFailsWith<ExternalApiAuthorisationException> {
//      apiClient.getActuarialMapping()
//    }
//
//    val expectedException = ExternalApiAuthorisationException(
//      "Unauthorized access",
//      HttpMethod.GET,
//      "/actuarial-mapping",
//      ExternalService.MANAGE_OFFENCES_API,
//    )
//
//    assertEquals(expectedException, thrown)
//  }
//
//  @Test
//  fun `getActuarialMapping throws ExternalApiForbiddenException on 403`() {
//
//
//    val thrown = assertFailsWith<ExternalApiForbiddenException> {
//      apiClient.getActuarialMapping()
//    }
//
//    val expectedException = ExternalApiForbiddenException(
//      "Forbidden access",
//      HttpMethod.GET,
//      "/actuarial-mapping",
//      ExternalService.MANAGE_OFFENCES_API,
//    )
//
//    assertEquals(expectedException, thrown)
//  }
//
//  @Test
//  fun `getActuarialMapping throws ExternalApiUnknownException on 500`() {
//
//
//    val thrown = assertFailsWith<ExternalApiUnknownException> {
//      apiClient.getActuarialMapping()
//    }
//
//    val expectedException = ExternalApiUnknownException(
//      "Server error",
//      HttpMethod.GET,
//      "/actuarial-mapping",
//      ExternalService.MANAGE_OFFENCES_API,
//    )
//
//    assertEquals(expectedException, thrown)
//  }
}