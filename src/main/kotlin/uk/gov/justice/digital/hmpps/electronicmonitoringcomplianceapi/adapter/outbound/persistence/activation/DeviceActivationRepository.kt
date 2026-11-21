package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.activation

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface DeviceActivationRepository : JpaRepository<DeviceActivationEntity, Long> {

  @Modifying
  @Query(
    value = """
        INSERT INTO device_activation (
            id,
            device_id,
            activation_date,
            deactivation_date
        )
        VALUES (
            :id,
            :deviceId,
            :activationDate,
            :deactivationDate
        )
        ON CONFLICT (id)
        DO UPDATE SET
            device_id = EXCLUDED.device_id,
            activation_date = EXCLUDED.activation_date,
            deactivation_date = EXCLUDED.deactivation_date,
            updated_at = now()
        WHERE (
            device_activation.device_id,
            device_activation.activation_date,
            device_activation.deactivation_date
        ) IS DISTINCT FROM (
            EXCLUDED.device_id,
            EXCLUDED.activation_date,
            EXCLUDED.deactivation_date
        )
    """,
    nativeQuery = true,
  )
  fun upsert(
    id: Long,
    deviceId: Long,
    activationDate: Instant,
    deactivationDate: Instant?,
  ): Int
}
