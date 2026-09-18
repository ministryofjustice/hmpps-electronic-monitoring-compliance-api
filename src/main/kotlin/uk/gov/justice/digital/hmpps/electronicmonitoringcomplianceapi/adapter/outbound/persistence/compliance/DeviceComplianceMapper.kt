package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.compliance

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceCompliance
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId

@Component
class DeviceComplianceMapper(
  private val ruleComplianceMapper: DeviceRuleComplianceMapper,
) {
  fun toDomain(
    entity: DeviceComplianceEntity,
  ): DeviceCompliance = DeviceCompliance.rehydrate(
    id = entity.id,
    deviceId = DeviceId(entity.deviceId),
    status = entity.status,
    ruleCompliance = entity.ruleCompliance.map(
      ruleComplianceMapper::toDomain,
    ),
  )

  fun toEntity(
    compliance: DeviceCompliance,
  ): DeviceComplianceEntity = DeviceComplianceEntity(
    id = compliance.id,
    deviceId = compliance.deviceId.value,
    status = compliance.status,
    state = compliance.state,
  )

  fun updateEntity(
    entity: DeviceComplianceEntity,
    compliance: DeviceCompliance,
  ) {
    entity.status = compliance.status
    entity.state = compliance.state

    val existing =
      entity.ruleCompliance.associateBy { it.id }

    val current =
      compliance.ruleCompliance.associateBy { it.id }

    current.forEach { (id, ruleCompliance) ->
      val child = existing[id]

      if (child == null) {
        entity.ruleCompliance.add(
          ruleComplianceMapper.toEntity(
            deviceCompliance = entity,
            compliance = ruleCompliance,
          ),
        )
      } else {
        child.state = ruleCompliance.state
        child.stateChangedAt =
          ruleCompliance.stateChangedAt
      }
    }

    entity.ruleCompliance.removeIf {
      it.id !in current
    }
  }
}
