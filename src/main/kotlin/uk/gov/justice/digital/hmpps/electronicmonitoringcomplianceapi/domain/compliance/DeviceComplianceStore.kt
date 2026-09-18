package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId

interface DeviceComplianceStore {
  fun find(
    deviceId: DeviceId,
  ): DeviceCompliance?

  fun findAll(): List<DeviceCompliance>

  fun save(compliance: DeviceCompliance): DeviceCompliance
}
