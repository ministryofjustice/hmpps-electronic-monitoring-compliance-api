package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.activation

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.activation.DeviceActivationStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.ElectronicMonitoringDataStore

@Service
class DiscoverDeviceActivations(
  private val source: ElectronicMonitoringDataStore,
  private val store: DeviceActivationStore,
) {
  fun execute() {
    var count = 0

    source.getDeviceActivations().forEach { activation ->
      store.upsert(activation)
      count++
    }

    log.info("Processed {} device activations from source", count)
  }

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
