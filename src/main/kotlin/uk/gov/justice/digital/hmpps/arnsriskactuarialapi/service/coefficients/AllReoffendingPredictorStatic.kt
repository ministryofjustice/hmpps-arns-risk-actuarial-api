package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.ActuarialCategory
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

fun getAllReoffendingPredictorStaticOffenceCodeCoefficient(category: ActuarialCategory, currentOffenceCode: String) = when (category) {
  ActuarialCategory.UNKNOWN -> throw IllegalArgumentException("Offence code mapping for $currentOffenceCode is UNKNOWN, ensure this is validated before the calculation")
  ActuarialCategory.BURGLARY_DOMESTIC -> BigDecimal("0.155944202809011")
  ActuarialCategory.BURGLARY_OTHER -> BigDecimal("0.2060248043228")
  ActuarialCategory.DRUNKENNESS -> BigDecimal("0.587490417262633")
  ActuarialCategory.DRINK_DRIVING -> BigDecimal("-0.264724530382273")
  ActuarialCategory.MOTORING_OFFENCES -> BigDecimal("-0.0439980822830184")
  ActuarialCategory.VEHICLE_RELATED_THEFT -> BigDecimal("0.173405416410866")
  ActuarialCategory.FRAUD_AND_FORGERY -> BigDecimal("-0.434791453303048")
  ActuarialCategory.WELFARE_FRAUD -> BigDecimal("-1.15395495579948")
  ActuarialCategory.DRUG_IMPORT_EXPORT_OR_PRODUCTION -> BigDecimal("-0.468252524851408")
  ActuarialCategory.DRUG_POSSESSION_OR_SUPPLY -> BigDecimal("0.0204101986121863")
  ActuarialCategory.VIOLENCE_AGAINST_THE_PERSON_ABH_PLUS -> BigDecimal("-0.110075583936778")
  ActuarialCategory.VIOLENCE_AGAINST_THE_PERSON_SUB_ABH -> BigDecimal("-0.110075583936778")
  ActuarialCategory.PUBLIC_ORDER_AND_HARRASSMENT -> BigDecimal("0.0854410288820027")
  ActuarialCategory.WEAPONS_NON_FIREARM -> BigDecimal("-0.110075583936778")
  ActuarialCategory.FIREARMS_MOST_SERIOUS -> BigDecimal("-0.110075583936778")
  ActuarialCategory.FIREARMS_OTHER -> BigDecimal("-0.110075583936778")
  ActuarialCategory.HANDLING_STOLEN_GOODS -> BigDecimal("0.088414958679896")
  ActuarialCategory.CRIMINAL_DAMAGE -> BigDecimal("0.12167599899735")
  ActuarialCategory.ACQUISITIVE_VIOLENCE -> BigDecimal("-0.119211282701221")
  ActuarialCategory.OTHER_OFFENCES -> BigDecimal("0.0384434393037725")
  ActuarialCategory.ABSCONDING_OR_BAIL -> BigDecimal("0.325485504296161")
  ActuarialCategory.SEXUAL_AGAINST_CHILD -> BigDecimal("0.111620464721896")
  ActuarialCategory.SEXUAL_NOT_AGAINST_CHILD -> BigDecimal("0.169969798103235")
  ActuarialCategory.THEFT_NON_MOTOR -> BigDecimal("0.462548123919817")
  ActuarialCategory.NEED_DETAILS_OF_EXACT_OFFENCE -> throw IllegalArgumentException("Offence code mapping for $currentOffenceCode is NEED_DETAILS_OF_EXACT_OFFENCE, ensure this is validated before the calculation")
}
