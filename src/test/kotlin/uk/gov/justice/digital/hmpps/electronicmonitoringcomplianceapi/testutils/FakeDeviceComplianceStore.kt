package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceCompliance
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceComplianceStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceComplianceSummary
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceRuleComplianceCounts
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleVersion
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import java.util.UUID

class FakeDeviceComplianceStore(
  initialCompliance: List<DeviceCompliance> = emptyList(),
) : DeviceComplianceStore {

  private val compliance =
    initialCompliance
      .associateBy { it.deviceId }
      .toMutableMap()

  val saved =
    mutableListOf<DeviceCompliance>()

  override fun find(
    deviceId: DeviceId,
  ): DeviceCompliance? = compliance[deviceId]

  override fun findById(
    id: UUID,
  ): DeviceCompliance? = compliance.values.firstOrNull {
    it.id == id
  }

  override fun findAll(): List<DeviceCompliance> = compliance.values.toList()

  override fun findAllSummaries(): List<DeviceComplianceSummary> = compliance.values.map {
    DeviceComplianceSummary(
      id = it.id,
      deviceId = it.deviceId,
      status = it.status,
      state = it.state,
    )
  }

  override fun save(
    compliance: DeviceCompliance,
  ): DeviceCompliance {
    this.compliance[compliance.deviceId] =
      compliance

    saved += compliance

    return compliance
  }

  override fun getRuleComplianceSummary(
    ruleId: RuleId,
    ruleVersion: RuleVersion,
  ): DeviceRuleComplianceCounts = DeviceRuleComplianceCounts(
    compliant = 10,
    nonCompliant = 5,
    noData = 2,
    deactivated = 3,
  )
}
