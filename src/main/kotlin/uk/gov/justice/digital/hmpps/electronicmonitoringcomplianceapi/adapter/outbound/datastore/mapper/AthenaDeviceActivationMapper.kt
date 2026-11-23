package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.datastore.mapper

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.activation.DeviceActivation
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.activation.DeviceActivationId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import java.time.Instant

@Component
class AthenaDeviceActivationMapper : AthenaMapper() {
  fun map(row: Map<String, String?>): DeviceActivation = DeviceActivation(
    id = DeviceActivationId(row.required("device_activation_id").toLong()),
    deviceId = DeviceId(row.required("device_id").toInt()), // TODO - should be Long
    activationDate = parseAthenaTimestamp(row.required("device_activation_date")),
    deactivationDate = mapDeactivationDate(row["device_deactivation_date"]),
  )

  private fun mapDeactivationDate(value: String?): Instant? {
    if (value.isNullOrBlank()) return null
    return parseAthenaTimestamp(value)
  }
}
