package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.inbound.scheduling

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.activation.DiscoverDeviceActivations
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.activation.DeviceActivation
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.activation.DeviceActivationId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils.FakeDeviceActivationStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils.FakeElectronicMonitoringDataStore
import java.time.Instant

class DeviceActivationDiscoverySchedulerTest {

  @Test
  fun `it should discover device activations`() {
    val source = FakeElectronicMonitoringDataStore(
      deviceActivations = listOf(
        DeviceActivation(
          id = DeviceActivationId(1L),
          deviceId = DeviceId(123),
          activationDate = Instant.parse("2026-01-01T10:00:00Z"),
          deactivationDate = null,
        ),
      ),
    )

    val store = FakeDeviceActivationStore()

    val scheduler = DeviceActivationDiscoveryScheduler(
      DiscoverDeviceActivations(source, store),
    )

    scheduler.discover()

    assertThat(store.activations).hasSize(1)
  }
}
