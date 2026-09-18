package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.compliance

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceCompliance
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceComplianceStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId

@Component
class DeviceCompliancePersistenceAdapter(
  private val repository: DeviceComplianceRepository,
  private val mapper: DeviceComplianceMapper,
) : DeviceComplianceStore {

  @Transactional
  override fun find(
    deviceId: DeviceId,
  ): DeviceCompliance? {
    val entity =
      repository.findByDeviceId(deviceId.value)
        ?: return null

    return mapper.toDomain(
      entity = entity,
    )
  }

  @Transactional
  override fun save(
    compliance: DeviceCompliance,
  ): DeviceCompliance {
    val entity =
      repository.findById(compliance.id)
        .orElseGet {
          mapper.toEntity(compliance)
        }

    mapper.updateEntity(
      entity = entity,
      compliance = compliance,
    )

    return mapper.toDomain(
      repository.save(entity),
    )
  }
}
