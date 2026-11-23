package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.activation.DeviceActivation
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.Device
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.ElectronicMonitoringDataStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.events.BatteryLevelReported

class FakeElectronicMonitoringDataStore(
  private val events: List<BatteryLevelReported> = listOf(),
  private val devices: List<Device> = listOf(),
  private val deviceActivations: List<DeviceActivation> = listOf(),
) : ElectronicMonitoringDataStore {
  override fun getBatteryLevelReportedEvents(): Sequence<BatteryLevelReported> = events.asSequence()

  override fun getDevices(): Sequence<Device> = devices.asSequence()

  override fun getDeviceActivations(): Sequence<DeviceActivation> = deviceActivations.asSequence()
}
