package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceComplianceSummary
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
}
