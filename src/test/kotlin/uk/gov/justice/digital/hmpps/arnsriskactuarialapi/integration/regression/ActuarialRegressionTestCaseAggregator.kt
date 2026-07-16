package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.regression

import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.params.aggregator.AggregateWith
import org.junit.jupiter.params.aggregator.ArgumentsAccessor
import org.junit.jupiter.params.aggregator.ArgumentsAggregator
import java.time.LocalDate

class ActuarialRegressionTestCaseAggregator: ArgumentsAggregator {
  override fun aggregateArguments(
    accessor: ArgumentsAccessor,
    context: ParameterContext,
  ): ActuarialRegressionTestCase {
    return ActuarialRegressionTestCase(
      id = accessor.getInteger(0),
      dateOfBirth = accessor.getString(1)?.let { LocalDate.parse(it) },
      gender = accessor.getString(2),
      offenceCode = accessor.getString(3),
      totalSanctionsCount = accessor.getInteger(4),
      totalViolentSanctions = accessor.getInteger(5),
      firstSanctionDate = accessor.getString(6)?.let { LocalDate.parse(it) },
      lastSanctionDate = accessor.getString(7)?.let { LocalDate.parse(it) },
      communityDate = accessor.getString(8)?.let { LocalDate.parse(it) },
      twoPointTwo = accessor.getInteger(9),
      threePointFour = accessor.getInteger(10),
      fourPointTwo = accessor.getInteger(11),
      sixPointFour = accessor.getInteger(12),
      sixPointSeven = accessor.getInteger(13),
      sixPointEight = accessor.getInteger(14),
      sevenPointTwo = accessor.getInteger(15),
      amphetamines = accessor.getString(16),
      benzodiazipines = accessor.getString(17),
      cannabis = accessor.getString(18),
      crackCocaine = accessor.getString(19),
      ecstasy = accessor.getString(20),
      hallucinogens = accessor.getString(21),
      heroin = accessor.getString(22),
      ketamine = accessor.getString(23),
      methadone = accessor.getString(24),
      misusedPrescribed = accessor.getString(25),
      otherDrugs = accessor.getString(26),
      otherOpiate = accessor.getString(27),
      powderCocaine = accessor.getString(28),
      solvents = accessor.getString(29),
      spice = accessor.getString(30),
      steroids = accessor.getString(31),
      eightPointEight = accessor.getInteger(32),
      ninePointOne = accessor.getInteger(33),
      ninePointTwo = accessor.getInteger(34),
      elevenPointTwo = accessor.getInteger(35),
      elevenPointFour = accessor.getInteger(36),
      twelvePointOne = accessor.getInteger(37),
      aggravatedBurglary = accessor.getInteger(38),
      arson = accessor.getInteger(39),
      criminalDamage = accessor.getInteger(40),
      firearms = accessor.getInteger(41),
      gbh = accessor.getInteger(42),
      homicide = accessor.getInteger(43),
      kidnap = accessor.getInteger(44),
      robbery = accessor.getInteger(45),
      weaponsNotFirearms = accessor.getInteger(46),
      seriousViolenceBriefPredictions = accessor.getDouble(47),
      seriousViolenceExtendedPredictions = accessor.getDouble(48),
      violenceBriefPredictions = accessor.getDouble(49),
      violenceExtendedPredictions = accessor.getDouble(50),
      allBriefPredictions = accessor.getDouble(51),
      allExtendedPredictions = accessor.getDouble(52),
    )
  }
}

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@AggregateWith(ActuarialRegressionTestCaseAggregator::class)
annotation class CsvToActuarialRegressionTestCase
