package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.activation

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "device_activation")
class DeviceActivationEntity(
  @Id
  @Column(name = "id", nullable = false)
  val id: Long,

  @Column(name = "device_id", nullable = false)
  val deviceId: Long,

  @Column(name = "activation_date", nullable = false)
  val activationDate: Instant,

  @Column(name = "deactivation_date")
  var deactivationDate: Instant?,

  @Column(name = "discovered_at", nullable = false)
  val discoveredAt: Instant = Instant.now(),

  @Column(name = "updated_at", nullable = false)
  var updatedAt: Instant = Instant.now(),
)
