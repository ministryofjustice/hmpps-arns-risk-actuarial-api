package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp.OGPInputValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp.OGPObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.getMissingFieldsValidation

@Service
class OGPRiskProducerService : RiskScoreProducer {

  fun getRiskScore(riskScoreRequest: RiskScoreRequest): OGPObject {
    val errors = getMissingFieldsValidation(riskScoreRequest, PROPERTIES_TO_ERRORS)

    if (!errors.isEmpty()) {
      return OGPObject(riskScoreRequest.version, null, null, null, errors)
    }

    val validRequest = OGPInputValidated(
      riskScoreRequest.version,
      riskScoreRequest.ogrs3TwoYear!!.toInt(),
      riskScoreRequest.currentAccommodation!!,
      riskScoreRequest.employmentStatus!!,
      riskScoreRequest.regularOffendingActivities!!,
      riskScoreRequest.currentDrugMisuse!!,
      riskScoreRequest.motivationDrug!!,
      riskScoreRequest.problemSolvingSkills!!,
      riskScoreRequest.awarenessOfConsequences!!,
      riskScoreRequest.understandsPeoplesViews!!,
      riskScoreRequest.proCriminalAttitudes!!,
    )

    return getOGPOutput(validRequest, errors)
  }

  override fun getRiskScore(
    request: RiskScoreRequest,
    context: RiskScoreContext,
  ): RiskScoreContext = context.copy(
    OGP = this.getRiskScore(coalesceForOGP(request, context.OGRS3?.ogrs3TwoYear)),
  )

  companion object {

    val PROPERTIES_TO_ERRORS = mapOf(
      "ogrs3TwoYear" to "OGRS3 Two Year",
      "currentAccommodation" to "Current accommodation",
      "employmentStatus" to "Employment status",
      "regularOffendingActivities" to "Regular offending activities",
      "currentDrugMisuse" to "Current drug misuse",
      "motivationDrug" to "Motivation drug",
      "problemSolvingSkills" to "Problem solving skills",
      "awarenessOfConsequences" to "Awareness of consequences",
      "understandsPeoplesViews" to "Understands Peoples Views",
      "proCriminalAttitudes" to "Procriminal attitudes",
    )

    fun coalesceForOGP(
      riskScoreRequest: RiskScoreRequest,
      ogrs3TwoYear: Int?,
    ): RiskScoreRequest = RiskScoreRequest(
      version = riskScoreRequest.version,
      ogrs3TwoYear = ogrs3TwoYear as Integer?,
      currentAccommodation = riskScoreRequest.currentAccommodation,
      employmentStatus = riskScoreRequest.employmentStatus,
      regularOffendingActivities = riskScoreRequest.regularOffendingActivities,
      currentDrugMisuse = riskScoreRequest.currentDrugMisuse,
      motivationDrug = riskScoreRequest.motivationDrug,
      problemSolvingSkills = riskScoreRequest.problemSolvingSkills,
      awarenessOfConsequences = riskScoreRequest.awarenessOfConsequences,
      understandsPeoplesViews = riskScoreRequest.understandsPeoplesViews,
      proCriminalAttitudes = riskScoreRequest.proCriminalAttitudes,
    )

    fun getOGPOutput(
      validRequest: OGPInputValidated,
      errors: MutableList<ValidationErrorResponse>,
    ): OGPObject {
      // TODO
      return OGPObject("1_0", null, null, null, null)
    }
  }
}
