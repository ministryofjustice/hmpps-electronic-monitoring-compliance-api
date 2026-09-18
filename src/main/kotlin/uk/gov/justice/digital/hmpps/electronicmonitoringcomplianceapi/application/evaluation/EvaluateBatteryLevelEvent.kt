package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.evaluation

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceComplianceStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.events.BatteryLevelReported

@Service
class EvaluateBatteryLevelEvent(
  private val ruleConfigurationStore: RuleConfigurationStore,
  private val deviceComplianceStore: DeviceComplianceStore,
) {
  private val rule = BatteryLevelRuleV1()

  fun evaluate(event: BatteryLevelReported) {
    val configuration = ruleConfigurationStore.findPublished(
      rule.definition,
    ) ?: throw IllegalStateException(
      "No published configuration for ${rule.definition.id.value} v${rule.definition.version.value}",
    )
    val deviceCompliance = deviceComplianceStore.find(event.deviceId)
      ?: error("Device compliance not initialised")

    val evaluation = rule.evaluate(
      event = event,
      configuration = configuration,
    )

    deviceCompliance.apply(evaluation)

    deviceComplianceStore.save(deviceCompliance)
  }
}
