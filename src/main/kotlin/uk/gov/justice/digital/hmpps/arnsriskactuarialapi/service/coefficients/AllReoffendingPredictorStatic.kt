package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients

import java.math.BigDecimal

enum class AllReoffendingPredictorStatic(val label: String, val coefficient: BigDecimal) {

  TWO_YEAR_CONSTANT("Intercept_2", BigDecimal(5.01702292499072)),
  AAI_MALE("maleaai", BigDecimal(-0.142428460338541)),
  AAI_QUADRATIC_MALE("maleaaiaai", BigDecimal(0.0011000413899151)),
  AAI_CUBIC_MALE("maleaaiaaiaai", BigDecimal(0.0000198538471606)),
  AAI_QUARTIC_MALE("maleaaiaaiaaiaai", BigDecimal(-0.0000002648918335)),
  AAI_FEMALE("aaifemale", BigDecimal(-0.0175014516689226)),
  AAI_QUADRATIC_FEMALE("aaiaaifemale", BigDecimal(0.0001625346907234)),
  AAI_CUBIC_FEMALE("aaiaaiaaifemale", BigDecimal(0.0000003645241305)),
  AAI_QUARTIC_FEMALE("aaiaaiaaiaaifemale", BigDecimal(-0.0000000746220588)),
  OFFENCE_VATP_FLAG("ogrs4_targoffVATP", BigDecimal(-0.110075583936778)),
  FEMALE("female", BigDecimal(-2.95224200717183)),
  FIRST_SANCTION("firstsanction", BigDecimal(-3.94357049933093)),
  SECOND_SANCTION("secondsanction", BigDecimal(-3.06189212045904)),
  SANCTION_OCCASIONS("ogrs3_sanctionoccasions", BigDecimal(-0.0042757301284995)),
  YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_MALE("malesecondsanctionyearssincefirs", BigDecimal(-0.0432051839161834)),
  YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_FEMALE("femalesecondsanctionyearssincefi", BigDecimal(-0.0427998315841607)),
  OFFENCE_FREE_MONTHS("ofm", BigDecimal(-0.0762086223169624)),
  OFFENCE_FREE_MONTHS_QUADRATIC("ofmofm", BigDecimal(0.0016230134182902)),
  OFFENCE_FREE_MONTHS_CUBIC("ofmofmofm", BigDecimal(0.0000224473135387)),
  OFFENCE_FREE_MONTHS_QUARTIC("ofmofmofmofm", BigDecimal(-0.0000012808638685)),
  THREE_PLUS_SANCTIONS_COPAS_G_MALE("malethreeplussanctionsogrs4g_rat", BigDecimal(1.55298303083717)),
  THREE_PLUS_SANCTIONS_COPAS_SQUARED_MALE("malethreeplussanctionsogrs4g_rao", BigDecimal(0.129334968245905)),
  THREE_PLUS_SANCTIONS_COPAS_G_FEMALE("femalethreeplussanctionsogrs4g_r", BigDecimal(1.01980307124196)),
  THREE_PLUS_SANCTIONS_COPAS_SQUARED_FEMALE("femalethreeplussanctionsogrs4g_o", BigDecimal(-0.039439197041062)),
}
