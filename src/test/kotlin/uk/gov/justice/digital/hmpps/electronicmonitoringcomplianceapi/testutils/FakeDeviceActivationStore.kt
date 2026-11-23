package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.activation.DeviceActivation
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.activation.DeviceActivationId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.activation.DeviceActivationStore

class FakeDeviceActivationStore : DeviceActivationStore {
  private val stored =
    mutableMapOf<DeviceActivationId, DeviceActivation>()

  override fun upsert(activation: DeviceActivation) {
    stored[activation.id] = activation
  }

  val activations: List<DeviceActivation>
    get() = stored.values.toList()
}
