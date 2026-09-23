package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.compliance

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.ComplianceState
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceStatus
import java.time.Instant

data class DeviceComplianceResponse(
  val deviceId: Int,
  val status: DeviceStatus,
  val state: ComplianceState?,
  val stateChangedAt: Instant?,
  val rules: List<RuleComplianceResponse>,
)
