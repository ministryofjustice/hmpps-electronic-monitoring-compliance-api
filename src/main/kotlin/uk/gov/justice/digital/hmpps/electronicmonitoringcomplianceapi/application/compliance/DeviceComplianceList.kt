package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.compliance

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceComplianceSummary

data class DeviceComplianceList(
  val summary: DeviceComplianceCounts,
  val devices: List<DeviceComplianceSummary>,
)
