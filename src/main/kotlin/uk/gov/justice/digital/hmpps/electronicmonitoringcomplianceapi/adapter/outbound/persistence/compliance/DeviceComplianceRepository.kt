package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.compliance

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceComplianceSummary
import java.util.UUID

interface DeviceComplianceRepository : JpaRepository<DeviceComplianceEntity, UUID> {
  fun findByDeviceId(
    deviceId: Int,
  ): DeviceComplianceEntity?

  @Query(
    """
      SELECT
        id,
        deviceId,
        status,
        state
      FROM DeviceComplianceEntity
      ORDER BY deviceId
    """,
  )
  fun findAllSummaries(): List<DeviceComplianceSummary>
}
