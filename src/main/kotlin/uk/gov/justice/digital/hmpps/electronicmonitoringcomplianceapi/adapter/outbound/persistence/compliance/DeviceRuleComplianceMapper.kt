package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.compliance

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceRuleCompliance
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleDefinition
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId

@Component
class DeviceRuleComplianceMapper {

  fun toDomain(
    entity: DeviceRuleComplianceEntity,
  ): DeviceRuleCompliance = DeviceRuleCompliance.rehydrate(
    id = entity.id,
    deviceId = DeviceId(entity.deviceId),
    ruleDefinition = entity.ruleDefinition(),
    state = entity.state,
    stateChangedAt = entity.stateChangedAt,
  )

  fun toEntity(
    deviceCompliance: DeviceComplianceEntity,
    compliance: DeviceRuleCompliance,
  ): DeviceRuleComplianceEntity = DeviceRuleComplianceEntity(
    id = compliance.id,
    deviceCompliance = deviceCompliance,
    deviceId = compliance.deviceId.value,
    ruleId = compliance.ruleDefinition.id.value,
    ruleVersion = compliance.ruleDefinition.version.value,
    state = compliance.state,
    stateChangedAt = compliance.stateChangedAt,
  )

  private fun DeviceRuleComplianceEntity.ruleDefinition(): RuleDefinition<*> = when {
    matches(BatteryLevelRuleV1.ruleDefinition) ->
      BatteryLevelRuleV1.ruleDefinition

    else ->
      throw IllegalArgumentException(
        "Unknown rule definition: $ruleId v$ruleVersion",
      )
  }

  private fun DeviceRuleComplianceEntity.matches(
    definition: RuleDefinition<*>,
  ): Boolean = ruleId == definition.id.value &&
    ruleVersion == definition.version.value
}
