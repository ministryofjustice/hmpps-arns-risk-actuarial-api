package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients

import java.math.BigDecimal

enum class SeriousViolentReoffendingPredictorStatic (val label: String, val coefficient: BigDecimal) {

  TWO_YEAR_CONSTANT("Intercept_2", BigDecimal(-0.970734555223987)),
  AAI_MALE("maleaai", BigDecimal(-0.035211251525685)),
  AAI_QUADRATIC_MALE("maleaaiaai", BigDecimal(-0.0002101460036267)),
  AAI_FEMALE("aaifemale", BigDecimal(-0.0194307797540929)),
  AAI_QUADRATIC_FEMALE("aaiaaifemale", BigDecimal(-0.0000241385941049)),
  FEMALE("female", BigDecimal(-1.55855151937225)),
  OFFENCE_VATP_FLAG("ogrs4_targoffVATP", BigDecimal(0.0881806276607277)),
  FIRST_SANCTION("firstsanction", BigDecimal(-2.3335499201698)),
  SECOND_SANCTION("secondsanction", BigDecimal(-1.36161428849118)),
  SANCTION_OCCASIONS("ogrs3_sanctionoccasions", BigDecimal(-0.0250462590018653)),
  YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_MALE("malesecondsanctionyearssincefirs", BigDecimal(-0.117956562302911)),
  YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_FEMALE("femalesecondsanctionyearssincefi", BigDecimal(-0.0402256071602197)),
  OFFENCE_FREE_MONTHS("ofm", BigDecimal(-0.0318548411238394)),
  OFFENCE_FREE_MONTHS_QUADRATIC("ofmofm", BigDecimal(0.0002165688028517)),
  OFFENCE_FREE_MONTHS_CUBIC("ofmofmofm", BigDecimal(0.0000046604929835)),
  OFFENCE_FREE_MONTHS_QUARTIC("ofmofmofmofm", BigDecimal(-0.000000152324083)),
  THREE_PLUS_SANCTIONS_COPAS_V_MALE("malethreeplussanctionsogrs4v_rat", BigDecimal(0.936944231003216)),
  THREE_PLUS_SANCTIONS_COPAS_V_FEMALE("femalethreeplussanctionsogrs4v_r", BigDecimal(0.853626611755616)),
  NEVER_VIOLENT_MALE("maleneverviolent", BigDecimal(-1.5382238276355)),
  NEVER_VIOLENT_FEMALE("femaleneverviolent", BigDecimal(-2.33948463970808)),
  ONCE_VIOLENT("onceviolent", BigDecimal(-0.0013488533112443)),
  VIOLENT_SANCTIONS("ogrs3_ovp_sanct", BigDecimal(0.0141947749755811)),
  VIOLENT_RATE("ogrs4v_rate_violent", BigDecimal(0.250478471909382)),
}
