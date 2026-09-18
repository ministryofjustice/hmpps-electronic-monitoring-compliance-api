package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceStatus

data class Device(
  val id: DeviceId,
  val status: DeviceStatus,
)
