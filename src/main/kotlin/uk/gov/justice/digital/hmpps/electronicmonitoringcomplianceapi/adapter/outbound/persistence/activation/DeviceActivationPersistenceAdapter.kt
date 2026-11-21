package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.activation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.activation.DeviceActivation
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.activation.DeviceActivationStore

@Component
class DeviceActivationPersistenceAdapter(
  private val repository: DeviceActivationRepository,
) : DeviceActivationStore {

  @Transactional
  override fun upsert(activation: DeviceActivation) {
    repository.upsert(
      id = activation.id.value,
      deviceId = activation.deviceId.value.toLong(),
      activationDate = activation.activationDate,
      deactivationDate = activation.deactivationDate,
    )
  }
}
