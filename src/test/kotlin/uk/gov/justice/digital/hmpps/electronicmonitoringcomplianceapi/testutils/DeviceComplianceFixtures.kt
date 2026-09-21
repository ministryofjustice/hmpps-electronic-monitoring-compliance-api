package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceCompliance
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleDefinition
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId

object DeviceComplianceFixtures {
  fun givenDeviceCompliance(
    deviceId: Int = 123,
    status: DeviceStatus = DeviceStatus.ACTIVATED,
    ruleDefinitions: List<RuleDefinition<*>> = listOf(
      BatteryLevelRuleV1.ruleDefinition,
    ),
  ): DeviceCompliance = DeviceCompliance.create(
    deviceId = DeviceId(deviceId),
    status = status,
    ruleDefinitions = ruleDefinitions,
  )
}
