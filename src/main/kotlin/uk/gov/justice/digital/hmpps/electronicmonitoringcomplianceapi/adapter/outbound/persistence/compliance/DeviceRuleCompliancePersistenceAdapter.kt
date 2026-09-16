package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.compliance

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceRuleCompliance
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceRuleComplianceStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleDefinition
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId

@Component
class DeviceRuleCompliancePersistenceAdapter(
  private val repository: DeviceRuleComplianceRepository,
  private val mapper: DeviceRuleComplianceMapper,
) : DeviceRuleComplianceStore {

  override fun find(
    deviceId: DeviceId,
    ruleDefinition: RuleDefinition<*>,
  ): DeviceRuleCompliance? = repository.findByDeviceIdAndRuleIdAndRuleVersion(
    deviceId = deviceId.value,
    ruleId = ruleDefinition.id.value,
    ruleVersion = ruleDefinition.version.value,
  )?.let(mapper::toDomain)

  override fun save(
    compliance: DeviceRuleCompliance,
  ): DeviceRuleCompliance = repository.save(
    mapper.toEntity(compliance),
  ).let(mapper::toDomain)
}
