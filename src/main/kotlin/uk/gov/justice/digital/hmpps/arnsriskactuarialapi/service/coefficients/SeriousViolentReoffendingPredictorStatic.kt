package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.ActuarialCategory
import java.math.BigDecimal

enum class SeriousViolentReoffendingPredictorStatic(val label: String, val coefficient: BigDecimal) {

  TWO_YEAR_CONSTANT("Intercept_2", BigDecimal(-0.970734555223987)),
  AAI_MALE("maleaai", BigDecimal(-0.035211251525685)),
  AAI_QUADRATIC_MALE("maleaaiaai", BigDecimal(-0.0002101460036267)),
  AAI_FEMALE("aaifemale", BigDecimal(-0.0194307797540929)),
  AAI_QUADRATIC_FEMALE("aaiaaifemale", BigDecimal(-0.0000241385941049)),
  FEMALE("female", BigDecimal(-1.55855151937225)),
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

fun getSeriousViolentReoffendingPredictorStaticOffenceCodeCoefficient(category: ActuarialCategory, currentOffenceCode: String) = when (category) {
  ActuarialCategory.UNKNOWN -> throw IllegalArgumentException("Offence code mapping for $currentOffenceCode is UNKNOWN, ensure this is validated before the calculation")
  ActuarialCategory.BURGLARY_DOMESTIC -> BigDecimal("0.427440297941078")
  ActuarialCategory.BURGLARY_OTHER -> BigDecimal("0.283654004619677")
  ActuarialCategory.DRUNKENNESS -> BigDecimal("-0.169817158710549")
  ActuarialCategory.DRINK_DRIVING -> BigDecimal("-0.0870700781552191")
  ActuarialCategory.MOTORING_OFFENCES -> BigDecimal("0.0805237507490763")
  ActuarialCategory.VEHICLE_RELATED_THEFT -> BigDecimal("0.286214108247545")
  ActuarialCategory.FRAUD_AND_FORGERY -> BigDecimal("-0.0932918356073765")
  ActuarialCategory.WELFARE_FRAUD -> BigDecimal("-2.75700217295462")
  ActuarialCategory.DRUG_IMPORT_EXPORT_OR_PRODUCTION -> BigDecimal("-0.151563625759297")
  ActuarialCategory.DRUG_POSSESSION_OR_SUPPLY -> BigDecimal("0.196404663980207")
  ActuarialCategory.VIOLENCE_AGAINST_THE_PERSON_ABH_PLUS -> BigDecimal("0.0032426570273524").add(BigDecimal("0.0881806276607277"))
  ActuarialCategory.VIOLENCE_AGAINST_THE_PERSON_SUB_ABH -> BigDecimal("0.0881806276607277")
  ActuarialCategory.PUBLIC_ORDER_AND_HARRASSMENT -> BigDecimal("-0.102680989477238")
  ActuarialCategory.WEAPONS_NON_FIREARM -> BigDecimal("0.316916255872188").add(BigDecimal("0.0881806276607277"))
  ActuarialCategory.FIREARMS_MOST_SERIOUS -> BigDecimal("-0.109313634130027").add(BigDecimal("0.0881806276607277"))
  ActuarialCategory.FIREARMS_OTHER -> BigDecimal("0.265777493636431").add(BigDecimal("0.0881806276607277"))
  ActuarialCategory.HANDLING_STOLEN_GOODS -> BigDecimal("0.20156598193072")
  ActuarialCategory.CRIMINAL_DAMAGE -> BigDecimal("0.254391121973677")
  ActuarialCategory.ACQUISITIVE_VIOLENCE -> BigDecimal("0.34905826404493")
  ActuarialCategory.OTHER_OFFENCES -> BigDecimal("0.175156031546492")
  ActuarialCategory.ABSCONDING_OR_BAIL -> BigDecimal("0.361929924513904")
  ActuarialCategory.SEXUAL_AGAINST_CHILD -> BigDecimal("-0.269534814977957")
  ActuarialCategory.SEXUAL_NOT_AGAINST_CHILD -> BigDecimal("-0.481426313870417")
  ActuarialCategory.THEFT_NON_MOTOR -> BigDecimal("-0.126315026107379")
  ActuarialCategory.NEED_DETAILS_OF_EXACT_OFFENCE -> throw IllegalArgumentException("Offence code mapping for $currentOffenceCode is NEED_DETAILS_OF_EXACT_OFFENCE, ensure this is validated before the calculation")
}
