package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import java.util.UUID

data class DeviceComplianceSummary(
  val id: UUID,
  val deviceId: DeviceId,
  val status: DeviceStatus,
  val state: ComplianceState?,
)
