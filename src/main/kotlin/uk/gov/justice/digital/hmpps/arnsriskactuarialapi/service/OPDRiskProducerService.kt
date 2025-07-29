package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd.OPDObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd.OPDRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.opdInitialValidation

@Service
class OPDRiskProducerService : RiskScoreProducer {

  @Autowired
  lateinit var offenceGroupParametersService: OffenceGroupParametersService

  override fun getRiskScore(
    request: RiskScoreRequest,
    context: RiskScoreContext,
  ): RiskScoreContext {
    val errors = opdInitialValidation(request)

    if (errors.isNotEmpty()) {
      return context
        .copy(OPD = OPDObject(opdEligibility = false, opdCheck = null, errors))
    }

    // request validated
    val validatedRequest = OPDRequestValidated(
      overallRiskForAssessment = request.overallRiskForAssessment!!,
      highestRiskLevel = request.highestRiskLevel!!,
      gender = request.gender!!,
      currentOffence = request.currentOffence!!,
      custodialSentence = request.custodialSentence!!,
      currentPsychologicalProblems = request.currentPsychologicalProblems,
      experienceOfChildhood = request.experienceOfChildhood,
      difficultiesCoping = request.difficultiesCoping,
      domesticAbusePartner = request.domesticAbusePartner,
      domesticAbuseFamily = request.domesticAbuseFamily,
      ageAtFirstSanction = request.ageAtFirstSanction,
      financialRelianceOnOthers = request.financialRelianceOnOthers,
      manipulativePredatoryBehaviour = request.manipulativePredatoryBehaviour,
      childhoodBehaviour = request.childhoodBehaviour,
      currentPsychiatricProblems = request.currentPsychiatricProblems,
      attitudesStableBehaviour = request.attitudesStableBehaviour,
      impulsivityBehaviour = request.impulsivityBehaviour,
      attitudeTowardsSupervision = request.attitudeTowardsSupervision,
      opdOverride = request.opdOverride ?: false,
      eligibleForMappa = request.eligibleForMappa ?: false,
      carryingOrUsingWeapon = request.carryingOrUsingWeapon ?: false,
      violenceOrThreatOfViolence = request.violenceOrThreatOfViolence ?: false,
      excessiveOrSadisticViolence = request.excessiveOrSadisticViolence ?: false,
      offenceArson = request.offenceArson ?: false,
      offenderMotivations = request.offenderMotivations ?: false,
      offenceLinkedRiskOfSeriousHarm = request.offenceLinkedRiskOfSeriousHarm ?: false,
      accommodationLinkedRiskOfSeriousHarm = request.accommodationLinkedRiskOfSeriousHarm ?: false,
      domesticAbuse = request.domesticAbuse ?: false,
      relationshipLinkedSeriousHarm = request.relationshipLinkedSeriousHarm ?: false,
      wellbeingEmotionalLinkedRiskOfSeriousHarm = request.wellbeingEmotionalLinkedRiskOfSeriousHarm ?: false,
      thinkingAndBehaviourLinedToRiskOfSeriousHarm = request.thinkingAndBehaviourLinedToRiskOfSeriousHarm
        ?: false,
      historyOfPsychiatricTreatment = request.historyOfPsychiatricTreatment ?: false,
      medicationMentalHealth = request.medicationMentalHealth ?: false,
      patientSecureUnitOrHospital = request.patientSecureUnitOrHospital ?: false,
      obsessiveBehaviour = request.obsessiveBehaviour ?: false,
      selfHarmSuicideAttempt = request.selfHarmSuicideAttempt ?: false,
      concernsAboutSuicidePast = request.concernsAboutSuicidePast ?: false,
      concernsAboutSelfHarmPast = request.concernsAboutSelfHarmPast ?: false,
      assaultedOrThreatenedStaff = request.assaultedOrThreatenedStaff ?: false,
      escapeOrAbsconded = request.escapeOrAbsconded ?: false,
      controlIssues = request.controlIssues ?: false,
      breachOfTrust = request.breachOfTrust ?: false,
      impactOfOffendingOnOthers = request.impactOfOffendingOnOthers ?: false,
    )

    // transformation
    val isViolentOrSexualType =
      offenceGroupParametersService.isViolentOrSexualType(validatedRequest.currentOffence)

    val isOpdApplicable = when (validatedRequest.gender) {
      Gender.MALE -> isOpdApplicableMale(validatedRequest, isViolentOrSexualType)
      Gender.FEMALE -> isOpdApplicableFemale(validatedRequest)
    }

    if (!isOpdApplicable) {
      return context.copy(
        OPD = OPDObject(
          opdEligibility = false,
          opdCheck = null,
          validationError = emptyList(),
        ),
      )
    }

    // proceed with score

    return context.copy(
      OPD = OPDObject(
        opdEligibility = true,
        opdCheck = null,
        validationError = emptyList(),
      ),
    )
  }

  fun isOpdApplicableMale(
    request: OPDRequestValidated,
    isViolentOrSexualType: Boolean,
  ): Boolean = (request.overallRiskForAssessment in arrayOf(RiskBand.HIGH, RiskBand.VERY_HIGH)) &&
    isViolentOrSexualType &&
    request.custodialSentence

  fun isOpdApplicableFemale(
    request: OPDRequestValidated,
  ): Boolean = (request.overallRiskForAssessment in arrayOf(RiskBand.HIGH, RiskBand.VERY_HIGH)) ||
    request.eligibleForMappa
}
