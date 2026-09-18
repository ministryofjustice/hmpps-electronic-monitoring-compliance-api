package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.compliance

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.ComplianceState
import java.time.Instant
import java.util.UUID

@Entity
@Table(
  name = "device_rule_compliance",
  uniqueConstraints = [
    UniqueConstraint(
      columnNames = [
        "device_compliance_id",
        "rule_id",
        "rule_version",
      ],
    ),
  ],
)
class DeviceRuleComplianceEntity(
  @Id
  val id: UUID,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
    name = "device_compliance_id",
    nullable = false,
  )
  val deviceCompliance: DeviceComplianceEntity,

  @Column(name = "device_id", nullable = false)
  val deviceId: Int,

  @Column(name = "rule_id", nullable = false)
  val ruleId: String,

  @Column(name = "rule_version", nullable = false)
  val ruleVersion: Int,

  @Enumerated(EnumType.STRING)
  @Column(name = "state", nullable = false)
  var state: ComplianceState,

  @Column(name = "state_changed_at")
  var stateChangedAt: Instant?,
)
