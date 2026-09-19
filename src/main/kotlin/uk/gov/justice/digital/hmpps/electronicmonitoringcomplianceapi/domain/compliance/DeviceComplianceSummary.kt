package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId

data class DeviceComplianceSummary(
  val deviceId: DeviceId,
  val status: DeviceStatus,
  val state: ComplianceState?,
)
