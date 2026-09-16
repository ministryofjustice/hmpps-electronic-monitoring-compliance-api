package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleDefinition
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId

interface DeviceRuleComplianceStore {
  fun find(
    deviceId: DeviceId,
    ruleDefinition: RuleDefinition<*>,
  ): DeviceRuleCompliance?

  fun save(compliance: DeviceRuleCompliance): DeviceRuleCompliance
}
