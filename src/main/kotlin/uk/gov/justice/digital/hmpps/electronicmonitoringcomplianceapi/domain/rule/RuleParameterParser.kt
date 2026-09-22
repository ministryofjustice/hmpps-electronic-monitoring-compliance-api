package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleParameters
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryPercentage

@Component
class RuleParameterParser {
  fun <P : RuleParameters> parse(ruleDefinition: RuleDefinition<P>, parameters: Map<String, Any>): P {
    @Suppress("UNCHECKED_CAST")
    return when (ruleDefinition) {
      BatteryLevelRuleV1.ruleDefinition ->
        parseBatteryLevelRuleParameters(parameters) as P

      else -> throw IllegalArgumentException(
        "Unsupported rule definition $ruleDefinition",
      )
    }
  }

  private fun parseBatteryLevelRuleParameters(parameters: Map<String, Any>): BatteryLevelRuleParameters {
    val threshold =
      (parameters["threshold"] as? Number)
        ?.toInt()
        ?: throw IllegalArgumentException(
          "Missing or invalid threshold parameter",
        )

    return BatteryLevelRuleParameters(
      threshold = BatteryPercentage(threshold),
    )
  }
}
