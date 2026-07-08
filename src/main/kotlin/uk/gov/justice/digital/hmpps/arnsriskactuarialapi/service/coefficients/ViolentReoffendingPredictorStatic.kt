package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.ActuarialCategory
import java.math.BigDecimal

enum class ViolentReoffendingPredictorStatic(val label: String, val coefficient: BigDecimal) {

  TWO_YEAR_CONSTANT("Intercept_2", BigDecimal(3.12324433235579)),
  AAI_MALE("maleaai", BigDecimal(-0.104477239763612)),
  AAI_QUADRATIC_MALE("maleaaiaai", BigDecimal(0.0008606154365776)),
  AAI_CUBIC_MALE("maleaaiaaiaai", BigDecimal(0.000013815779629)),
  AAI_QUARTIC_MALE("maleaaiaaiaaiaai", BigDecimal(-0.0000001960154357)),
  AAI_FEMALE("aaifemale", BigDecimal(-0.0151569773369266)),
  AAI_QUADRATIC_FEMALE("aaiaaifemale", BigDecimal(-0.0000517057104954)),
  AAI_CUBIC_FEMALE("aaiaaiaaifemale", BigDecimal(-0.0000007587337191)),
  AAI_QUARTIC_FEMALE("aaiaaiaaiaaifemale", BigDecimal(0.0000000203053155)),
  FEMALE("female", BigDecimal(-1.84281451035478)),
  FIRST_SANCTION("firstsanction", BigDecimal(-1.90092364059771)),
  SECOND_SANCTION("secondsanction", BigDecimal(-1.10995084554834)),
  SANCTION_OCCASIONS("ogrs3_sanctionoccasions", BigDecimal(-0.0067658067744931)),
  YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_MALE("malesecondsanctionyearssincefirs", BigDecimal(-0.0517946785377468)),
  YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_FEMALE("femalesecondsanctionyearssincefi", BigDecimal(-0.0524808327112117)),
  OFFENCE_FREE_MONTHS("ofm", BigDecimal(-0.073627004774941)),
  OFFENCE_FREE_MONTHS_QUADRATIC("ofmofm", BigDecimal(0.0015286971037078)),
  OFFENCE_FREE_MONTHS_CUBIC("ofmofmofm", BigDecimal(0.0000095033186151)),
  OFFENCE_FREE_MONTHS_QUARTIC("ofmofmofmofm", BigDecimal(-0.0000008860433091)),
  THREE_PLUS_SANCTIONS_COPAS_V_MALE("malethreeplussanctionsogrs4v_rat", BigDecimal(0.620598396891494)),
  THREE_PLUS_SANCTIONS_COPAS_V_FEMALE("femalethreeplussanctionsogrs4v_r", BigDecimal(0.507851479286236)),
  NEVER_VIOLENT_MALE("maleneverviolent", BigDecimal(-2.19912021141319)),
  NEVER_VIOLENT_FEMALE("femaleneverviolent", BigDecimal(-2.8090905835064)),
  ONCE_VIOLENT("onceviolent", BigDecimal(0.164824968220639)),
  VIOLENT_SANCTIONS("ogrs3_ovp_sanct", BigDecimal(0.0185294325031695)),
  VIOLENT_RATE("ogrs4v_rate_violent", BigDecimal(0.522272685793084)),
}

fun getViolentReoffendingPredictorStaticOffenceCodeCoefficient(category: ActuarialCategory, currentOffenceCode: String) = when (category) {
  ActuarialCategory.UNKNOWN -> throw IllegalArgumentException("Offence code mapping for $currentOffenceCode is UNKNOWN, ensure this is validated before the calculation")
  ActuarialCategory.BURGLARY_DOMESTIC -> BigDecimal("0.189290151743428")
  ActuarialCategory.BURGLARY_OTHER -> BigDecimal("0.152185842790808")
  ActuarialCategory.DRUNKENNESS -> BigDecimal("0.897225111715064")
  ActuarialCategory.DRINK_DRIVING -> BigDecimal("-0.0955339743007365")
  ActuarialCategory.MOTORING_OFFENCES -> BigDecimal("-0.184914428055833")
  ActuarialCategory.VEHICLE_RELATED_THEFT -> BigDecimal("0.165882021264931")
  ActuarialCategory.FRAUD_AND_FORGERY -> BigDecimal("-0.362999252067375")
  ActuarialCategory.WELFARE_FRAUD -> BigDecimal("-0.987504454326342")
  ActuarialCategory.DRUG_IMPORT_EXPORT_OR_PRODUCTION -> BigDecimal("-0.422720654391483")
  ActuarialCategory.DRUG_POSSESSION_OR_SUPPLY -> BigDecimal("-0.0876488981693274")
  ActuarialCategory.VIOLENCE_AGAINST_THE_PERSON_ABH_PLUS -> BigDecimal("0.109809951520084")
  ActuarialCategory.VIOLENCE_AGAINST_THE_PERSON_SUB_ABH -> BigDecimal("0.109809951520084")
  ActuarialCategory.PUBLIC_ORDER_AND_HARRASSMENT -> BigDecimal("0.356342355096462")
  ActuarialCategory.WEAPONS_NON_FIREARM -> BigDecimal("0.109809951520084")
  ActuarialCategory.FIREARMS_MOST_SERIOUS -> BigDecimal("0.109809951520084")
  ActuarialCategory.FIREARMS_OTHER -> BigDecimal("0.109809951520084")
  ActuarialCategory.HANDLING_STOLEN_GOODS -> BigDecimal("0.0120019188396128")
  ActuarialCategory.CRIMINAL_DAMAGE -> BigDecimal("0.371503082545136")
  ActuarialCategory.ACQUISITIVE_VIOLENCE -> BigDecimal("-0.0032926198476618")
  ActuarialCategory.OTHER_OFFENCES -> BigDecimal("0.0998951874967628")
  ActuarialCategory.ABSCONDING_OR_BAIL -> BigDecimal("0.351192370764659")
  ActuarialCategory.SEXUAL_AGAINST_CHILD -> BigDecimal("-0.930849896127774")
  ActuarialCategory.SEXUAL_NOT_AGAINST_CHILD -> BigDecimal("-0.159028472471635")
  ActuarialCategory.THEFT_NON_MOTOR -> BigDecimal("0.260179775875471")
  ActuarialCategory.NEED_DETAILS_OF_EXACT_OFFENCE -> throw IllegalArgumentException("Offence code mapping for $currentOffenceCode is NEED_DETAILS_OF_EXACT_OFFENCE, ensure this is validated before the calculation")
}
