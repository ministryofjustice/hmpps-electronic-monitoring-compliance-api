package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.evaluation

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceRuleCompliance
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceRuleComplianceStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.events.BatteryLevelReported

@Service
class EvaluateBatteryLevelEvent(
  private val ruleConfigurationStore: RuleConfigurationStore,
  private val deviceRuleComplianceStore: DeviceRuleComplianceStore,
) {
  private val rule = BatteryLevelRuleV1()

  fun evaluate(event: BatteryLevelReported) {
    val configuration = ruleConfigurationStore.findPublished(
      rule.definition,
    ) ?: throw IllegalStateException(
      "No published configuration for ${rule.definition.id.value} v${rule.definition.version.value}",
    )

    val evaluation = rule.evaluate(
      event = event,
      configuration = configuration,
    )

    val compliance = deviceRuleComplianceStore.find(
      deviceId = event.deviceId,
      ruleDefinition = rule.definition,
    )

    if (compliance == null) {
      deviceRuleComplianceStore.save(
        DeviceRuleCompliance.from(evaluation),
      )
      return
    }

    compliance.apply(evaluation)

    deviceRuleComplianceStore.save(compliance)
  }
}
