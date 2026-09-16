package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.compliance

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DeviceRuleComplianceRepository : JpaRepository<DeviceRuleComplianceEntity, UUID> {

  fun findByDeviceIdAndRuleIdAndRuleVersion(
    deviceId: Int,
    ruleId: String,
    ruleVersion: Int,
  ): DeviceRuleComplianceEntity?
}
