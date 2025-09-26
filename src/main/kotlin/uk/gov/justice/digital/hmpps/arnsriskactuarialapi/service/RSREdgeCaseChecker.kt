package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest

fun sexualSectionAllNull(request: RiskScoreRequest): Boolean = request.hasEverCommittedSexualOffence == null &&
  request.isCurrentOffenceSexuallyMotivated == null &&
  request.totalContactAdultSexualSanctions == null &&
  request.totalContactChildSexualSanctions == null &&
  request.totalNonContactSexualOffences == null &&
  request.totalIndecentImageSanctions == null

fun sexualOffenceHistoryTrueButSanctionsZero(request: RiskScoreRequest): Boolean = request.hasEverCommittedSexualOffence == true &&
  request.isCurrentOffenceSexuallyMotivated == true &&
  request.totalContactAdultSexualSanctions == 0 &&
  request.totalContactChildSexualSanctions == 0 &&
  request.totalNonContactSexualOffences == 0 &&
  request.totalIndecentImageSanctions == 0

fun sexualOffenceHistoryFalseButSanctionsNull(request: RiskScoreRequest): Boolean = request.hasEverCommittedSexualOffence == false &&
  request.isCurrentOffenceSexuallyMotivated == null &&
  request.totalContactAdultSexualSanctions == null &&
  request.totalContactChildSexualSanctions == null &&
  request.totalNonContactSexualOffences == null &&
  request.totalIndecentImageSanctions == null
