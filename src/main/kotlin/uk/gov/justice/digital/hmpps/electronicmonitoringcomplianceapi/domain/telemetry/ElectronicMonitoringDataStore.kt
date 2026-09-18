package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.events.BatteryLevelReported

interface ElectronicMonitoringDataStore {
  fun getBatteryLevelReportedEvents(): Sequence<BatteryLevelReported>

  fun getDevices(): Sequence<Device>
}
