package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.datastore.mapper

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryPercentage
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.EventId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.events.BatteryLevelReported
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

@Component
class AthenaBatteryLevelEventMapper {

  fun map(row: Map<String, String?>): BatteryLevelReported = BatteryLevelReported(
    eventId = EventId(
      row.required("event_id").toInt(),
    ),
    deviceId = DeviceId(
      row.required("device_id").toInt(),
    ),
    recordedAt = parseRecordedAt(
      row.required("event_recorded_date_utc"),
    ),
    batteryPercentage = BatteryPercentage(
      row.required("event_status_flags").toInt(),
    ),
  )

  private fun Map<String, String?>.required(
    column: String,
  ): String = this[column]
    ?.takeIf { it.isNotBlank() }
    ?: throw IllegalArgumentException(
      "Athena result is missing $column",
    )

  private fun parseRecordedAt(
    value: String,
  ): Instant = LocalDateTime
    .parse(value, ATHENA_TIMESTAMP_FORMATTER)
    .toInstant(ZoneOffset.UTC)

  companion object {
    private val ATHENA_TIMESTAMP_FORMATTER: DateTimeFormatter =
      DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd HH:mm:ss")
        .optionalStart()
        .appendFraction(
          ChronoField.NANO_OF_SECOND,
          0,
          9,
          true,
        )
        .optionalEnd()
        .toFormatter()
  }
}
