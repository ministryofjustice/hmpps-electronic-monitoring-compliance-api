package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.compliance

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceComplianceStore
import java.util.UUID

@Service
class GetDeviceCompliance(
  private val store: DeviceComplianceStore,
) {
  fun get(deviceComplianceId: UUID): DeviceComplianceResponse {
    val deviceCompliance = store.findById(deviceComplianceId)
      ?: throw EntityNotFoundException("Device compliance with id $deviceComplianceId not found")

    return DeviceComplianceResponse(
      deviceId = deviceCompliance.deviceId.value,
      status = deviceCompliance.status,
      state = deviceCompliance.state,
      stateChangedAt = deviceCompliance.ruleCompliance.mapNotNull { it.stateChangedAt }.maxOfOrNull { it },
      rules = deviceCompliance.ruleCompliance.map {
        RuleComplianceResponse(
          ruleId = it.ruleDefinition.id.value,
          ruleVersion = it.ruleDefinition.version.value,
          state = it.state,
          stateChangedAt = it.stateChangedAt,
        )
      },
    )
  }
}
