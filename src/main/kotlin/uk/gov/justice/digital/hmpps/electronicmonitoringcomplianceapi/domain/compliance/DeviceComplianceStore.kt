package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleVersion
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import java.util.UUID

interface DeviceComplianceStore {
  fun find(
    deviceId: DeviceId,
  ): DeviceCompliance?

  fun findById(
    id: UUID,
  ): DeviceCompliance?

  fun findAll(): List<DeviceCompliance>

  fun findAllSummaries(): List<DeviceComplianceSummary>

  fun save(compliance: DeviceCompliance): DeviceCompliance

  fun getRuleComplianceSummary(
    ruleId: RuleId,
    ruleVersion: RuleVersion,
  ): DeviceRuleComplianceCounts
}
