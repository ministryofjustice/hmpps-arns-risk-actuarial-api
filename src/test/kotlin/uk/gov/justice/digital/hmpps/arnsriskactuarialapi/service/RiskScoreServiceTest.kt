package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.OGRS3Object
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest

@ExtendWith(MockitoExtension::class)
class RiskScoreServiceTest {

  @Mock
  private lateinit var oGRS3RiskScoreService: OGRS3RiskScoreService

  @InjectMocks
  private lateinit var riskScoreService: RiskScoreService

  @Test
  fun `riskScoreProducer returns risk score response with algorithm version as 1_0`() {
    val request = RiskScoreRequest(
      "1_0",
      null,
      null,
      null,
      null,
      null,
      null,
      null,
    )

    whenever(oGRS3RiskScoreService.getRiskScore(request))
      .thenReturn(OGRS3Object("1_0", null, null, null, null))


    val result = riskScoreService.riskScoreProducer(request)
    Assertions.assertEquals("1_0", result.OGRS3.algorithmVersion)
  }
}
