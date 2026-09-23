package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.datastore.mapper

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryPercentage
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.EventId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.events.BatteryLevelReported

@Component
class AthenaBatteryLevelEventMapper : AthenaMapper() {

  fun map(row: Map<String, String?>): BatteryLevelReported = BatteryLevelReported(
    eventId = EventId(
      row.required("event_id").toInt(),
    ),
    deviceId = DeviceId(
      row.required("device_id").toInt(),
    ),
    recordedAt = parseAthenaTimestamp(
      row.required("event_recorded_date_utc"),
    ),
    batteryPercentage = BatteryPercentage(
      row.required("event_status_flags").toInt(),
    ),
  )
}
