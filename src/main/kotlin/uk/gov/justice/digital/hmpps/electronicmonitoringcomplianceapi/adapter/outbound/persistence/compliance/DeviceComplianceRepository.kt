package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.compliance

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DeviceComplianceRepository : JpaRepository<DeviceComplianceEntity, UUID> {

  fun findByDeviceId(
    deviceId: Int,
  ): DeviceComplianceEntity?
}
