package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.activation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.activation.DeviceActivation
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.activation.DeviceActivationId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils.FakeDeviceActivationStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils.FakeElectronicMonitoringDataStore
import java.time.Instant

class DiscoverDeviceActivationsTest {
  @Test
  fun `it registers new activations`() {
    // Given a new activation
    val activation = createActivation()

    // And the activation exists in the upstream store
    val dataStore = FakeElectronicMonitoringDataStore(deviceActivations = listOf(activation))
    val store = FakeDeviceActivationStore()
    val useCase = DiscoverDeviceActivations(dataStore, store)

    // When the use case is executed
    useCase.execute()

    // Then the activation is registered in the local store
    assertThat(store.activations).containsExactly(activation)
  }

  private fun createActivation(
    id: Long = 1L,
    deviceId: Int = 123,
    activationDate: Instant =
      Instant.parse("2026-01-01T10:00:00Z"),
    deactivationDate: Instant? = null,
  ) = DeviceActivation(
    id = DeviceActivationId(id),
    deviceId = DeviceId(deviceId),
    activationDate = activationDate,
    deactivationDate = deactivationDate,
  )
}
