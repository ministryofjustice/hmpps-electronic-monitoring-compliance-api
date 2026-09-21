package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleParameters
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleParameters

@Service
class ListRuleConfigurations(
  private val store: RuleConfigurationStore,
) {
  fun list(): List<RuleConfigurationSummary> = store.findPublished().map {
    RuleConfigurationSummary(
      id = it.id.value,
      ruleId = it.ruleDefinition.id.value,
      ruleVersion = it.ruleDefinition.version.value,
      revision = it.revision.value,
      parameters = it.parameters.toSummary(),
    )
  }

  private fun RuleParameters.toSummary(): RuleConfigurationParametersSummary = when (this) {
    is BatteryLevelRuleParameters ->
      RuleConfigurationParametersSummary.BatteryLevel(
        threshold = threshold.value,
      )

    else -> throw IllegalArgumentException(
      "Unsupported rule parameters: ${this::class.simpleName}",
    )
  }
}
