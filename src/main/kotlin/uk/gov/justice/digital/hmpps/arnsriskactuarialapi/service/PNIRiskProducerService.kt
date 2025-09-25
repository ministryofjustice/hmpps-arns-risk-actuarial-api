package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.NeedScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.SupervisionStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.ProgrammeNeedIdentifier
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.anyNullSara
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.bothNullSara
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isHighOgrs3
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isHighOvp
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isHighSara
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isMediumSara
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isOgrs3Medium
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isOspDcHigh
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isOspDcMedium
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isOspIicHigh
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isOspIicMedium
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isOvpMedium
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isRsrHigh
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isRsrMedium
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.overallNeedsGroupingCalculation
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.addMissingFields
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validatePNI

@Service
class PNIRiskProducerService : BaseRiskScoreProducer() {

  override fun getRiskScore(
    request: RiskScoreRequest,
    context: RiskScoreContext,
  ): RiskScoreContext {
    val errors = validatePNI(request)

    if (errors.isNotEmpty()) {
      return applyErrorsToContextAndReturn(context, errors)
    }

    val requestValidated = PNIRequestValidated(
      supervisionStatus = request.supervisionStatus!!,
      hasEverCommittedSexualOffence = request.hasEverCommittedSexualOffence,
      isARiskOfSexualHarm = request.isARiskOfSexualHarm,
      sexualPreoccupation = request.sexualPreoccupation,
      offenceRelatedSexualInterests = request.offenceRelatedSexualInterests,
      emotionalCongruenceWithChildren = request.emotionalCongruenceWithChildren,
      proCriminalAttitudes = request.proCriminalAttitudes,
      hostileOrientation = request.hostileOrientation,
      currentRelationshipWithFamilyMembers = request.currentRelationshipWithFamilyMembers,
      previousCloseRelationships = request.previousCloseRelationships,
      easilyInfluencedByCriminalAssociates = request.easilyInfluencedByCriminalAssociates,
      controllingOrAggressiveBehaviour = request.controllingOrAggressiveBehaviour,
      impulsivityProblems = request.impulsivityProblems,
      temperControl = request.temperControl,
      ogrs3TwoYear = context.OGRS3?.ogrs3TwoYear,
      ovp = context.OVP?.provenViolentTypeReoffendingTwoYear,
      ovpBand = context.OVP?.band,
      ospDCBand = context.RSR?.ospdcBand,
      ospIICBand = context.RSR?.ospiicBand,
      rsr = context.RSR?.rsrScore?.toInt(),
      saraRiskToPartner = request.saraRiskToPartner,
      saraRiskToOthers = request.saraRiskToOthers,
      problemSolvingSkills = request.problemSolvingSkills,
      difficultiesCoping = request.difficultiesCoping,
    )

    val overallNeed = overallNeedsGroupingCalculation(requestValidated)
    val overallNeedScore = overallNeed.first
    val additionalErrors = addMissingFields(overallNeed.third.toList(), errors)
    if (overallNeedScore == null) {
      return context.apply { PNI = PNIObject(ProgrammeNeedIdentifier.OMISSION, additionalErrors) }
    }

    val overallRisk = when {
      isHighRisk(requestValidated) -> RiskBand.HIGH
      isMediumRisk(requestValidated) -> RiskBand.MEDIUM
      else -> RiskBand.LOW
    }

    val projectedRisk = when {
      isHighRisk(
        requestValidated.copy(
          ospIICBand = requestValidated.ospIICBand ?: RiskBand.VERY_HIGH,
          ospDCBand = requestValidated.ospDCBand ?: RiskBand.VERY_HIGH,
          rsr = requestValidated.rsr ?: 100,
        ),
      ) -> RiskBand.HIGH

      isMediumRisk(requestValidated) -> RiskBand.MEDIUM
      else -> RiskBand.LOW
    }

    val interimResult = when {
      isHighIntensity(requestValidated, overallNeedScore, overallRisk) -> ProgrammeNeedIdentifier.HIGH

      isModerateIntensity(requestValidated, overallNeedScore, overallRisk) -> ProgrammeNeedIdentifier.MODERATE
      else -> ProgrammeNeedIdentifier.ALTERNATIVE
    }

    var pniPathway = interimResult
    // possible omission scenarios
    if (hasMissingAnswers(overallNeed)) {
      if (anyNullSara(requestValidated) && interimResult == ProgrammeNeedIdentifier.ALTERNATIVE) {
        pniPathway = ProgrammeNeedIdentifier.OMISSION
      }
      if (projectedRisk != overallRisk) {
        pniPathway = ProgrammeNeedIdentifier.OMISSION
      }
      if (bothNullSara(requestValidated) &&
        isCustody(requestValidated)
      ) {
        pniPathway = ProgrammeNeedIdentifier.OMISSION
      }
    }

    return context.apply { PNI = PNIObject(pniPathway, additionalErrors) }
  }

  override fun applyErrorsToContextAndReturn(
    context: RiskScoreContext,
    validationErrorResponses: List<ValidationErrorResponse>,
  ): RiskScoreContext = context.apply {
    PNI = PNIObject(
      ProgrammeNeedIdentifier.OMISSION,
      validationErrorResponses,
    )
  }

  /**
   * In PNI REMAND should be treated in the same way as CUSTODY
   */
  private fun isCustody(requestValidated: PNIRequestValidated): Boolean = requestValidated.supervisionStatus == SupervisionStatus.CUSTODY || requestValidated.supervisionStatus == SupervisionStatus.REMAND

  private fun hasMissingAnswers(overallNeed: Triple<NeedScore?, NeedScore?, List<String>>): Boolean = overallNeed.third.isNotEmpty()

  /**
   * High intensity programmes are only available in prisons and will therefore only be provided in these cases.
   */
  internal fun isHighIntensity(
    request: PNIRequestValidated,
    need: NeedScore,
    risk: RiskBand,
  ): Boolean {
    if (request.supervisionStatus != SupervisionStatus.CUSTODY) return false
    return isHighOgrsWithHighOVP(request) ||
      isHighOgrsWithHighSara(request) ||
      isHighNeedWithHighRisk(need, risk)
  }

  /**
   * Moderate Intensity programmes are available in prisons and community.
   */
  internal fun isModerateIntensity(
    request: PNIRequestValidated,
    need: NeedScore,
    risk: RiskBand,
  ): Boolean = (isHighOgrsWithHighOVP(request) && request.supervisionStatus == SupervisionStatus.COMMUNITY) ||
    (isHighNeedWithHighRisk(need, risk) && request.supervisionStatus == SupervisionStatus.COMMUNITY) ||
    isHighSara(request) ||
    isMediumSara(request) ||
    isMediumNeedWithHighRisk(need, risk) ||
    isHighNeedWithMediumRisk(need, risk) ||
    isMediumNeedWithMediumRisk(need, risk)

  private fun isHighOgrsWithHighOVP(request: PNIRequestValidated) = isHighOgrs3(request) && isHighOvp(request)

  private fun isHighOgrsWithHighSara(request: PNIRequestValidated) = isHighOgrs3(request) && isHighSara(request)

  private fun isHighNeedWithHighRisk(
    need: NeedScore,
    risk: RiskBand,
  ) = need == NeedScore.HIGH && risk == RiskBand.HIGH

  private fun isMediumNeedWithHighRisk(
    need: NeedScore,
    risk: RiskBand,
  ) = need == NeedScore.MEDIUM && risk == RiskBand.HIGH

  private fun isHighNeedWithMediumRisk(
    need: NeedScore,
    risk: RiskBand,
  ) = need == NeedScore.HIGH && risk == RiskBand.MEDIUM

  private fun isMediumNeedWithMediumRisk(
    need: NeedScore,
    risk: RiskBand,
  ) = need == NeedScore.MEDIUM && risk == RiskBand.MEDIUM

  internal fun isHighRisk(
    requestValidated: PNIRequestValidated,
  ): Boolean = isHighOgrs3(requestValidated) ||
    isHighOvp(requestValidated) ||
    isOspDcHigh(requestValidated) ||
    isOspIicHigh(requestValidated) ||
    isRsrHigh(requestValidated) ||
    isHighSara(requestValidated)

  internal fun isMediumRisk(
    requestValidated: PNIRequestValidated,
  ): Boolean = isOgrs3Medium(requestValidated) ||
    isOvpMedium(requestValidated) ||
    isOspDcMedium(requestValidated) ||
    isOspIicMedium(requestValidated) ||
    isMediumSara(requestValidated) ||
    isRsrMedium(requestValidated)
}
