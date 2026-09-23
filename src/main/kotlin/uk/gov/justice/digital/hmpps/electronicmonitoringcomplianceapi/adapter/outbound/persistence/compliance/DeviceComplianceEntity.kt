package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.compliance

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.ComplianceState
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceStatus
import java.util.UUID

@Entity
@Table(
  name = "device_compliance",
  uniqueConstraints = [
    UniqueConstraint(columnNames = ["device_id"]),
  ],
)
class DeviceComplianceEntity(
  @Id
  val id: UUID,

  @Column(name = "device_id", nullable = false)
  val deviceId: Int,

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  var status: DeviceStatus,

  @Enumerated(EnumType.STRING)
  @Column(name = "state")
  var state: ComplianceState?,

  @OneToMany(
    mappedBy = "deviceCompliance",
    cascade = [CascadeType.ALL],
    orphanRemoval = true,
    fetch = FetchType.LAZY,
  )
  val ruleCompliance: MutableList<DeviceRuleComplianceEntity> =
    mutableListOf(),
)
