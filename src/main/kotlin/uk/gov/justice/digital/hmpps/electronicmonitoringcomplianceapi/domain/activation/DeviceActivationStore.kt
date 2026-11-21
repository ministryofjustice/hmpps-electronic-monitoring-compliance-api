package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.activation

interface DeviceActivationStore {
  fun upsert(activation: DeviceActivation)
}
