package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.compliance

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceComplianceSummary
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceRuleComplianceCounts
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

  @Query(
    """
      SELECT
        COUNT(*) FILTER (
            WHERE dc.status = 'ACTIVATED'
              AND drc.state = 'COMPLIANT'
        ) AS compliant,
    
        COUNT(*) FILTER (
            WHERE dc.status = 'ACTIVATED'
              AND drc.state = 'NON_COMPLIANT'
        ) AS non_compliant,
    
        COUNT(*) FILTER (
            WHERE dc.status = 'ACTIVATED'
              AND drc.state = 'NO_DATA'
        ) AS no_data,
    
        COUNT(*) FILTER (
            WHERE dc.status = 'DEACTIVATED'
        ) AS deactivated
    FROM device_rule_compliance drc
    JOIN device_compliance dc
      ON dc.id = drc.device_compliance_id
    
    WHERE drc.rule_id = :ruleId
      AND drc.rule_version = :ruleVersion
    """,
    nativeQuery = true,
  )
  fun getRuleComplianceSummary(
    ruleId: String,
    ruleVersion: Int,
  ): DeviceRuleComplianceCounts
}
