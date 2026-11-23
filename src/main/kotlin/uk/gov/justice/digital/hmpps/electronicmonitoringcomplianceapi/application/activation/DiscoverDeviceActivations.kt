package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.activation

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.activation.DeviceActivationStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.ElectronicMonitoringDataStore

@Service
class DiscoverDeviceActivations(
  private val source: ElectronicMonitoringDataStore,
  private val store: DeviceActivationStore,
) {
  fun execute() {
    source.getDeviceActivations().forEach { activation ->
      store.upsert(activation)
    }
  }
}
