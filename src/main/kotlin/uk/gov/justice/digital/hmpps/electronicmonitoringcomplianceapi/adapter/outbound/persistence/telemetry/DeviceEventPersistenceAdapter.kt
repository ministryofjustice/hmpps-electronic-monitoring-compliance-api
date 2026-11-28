package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.telemetry

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceEvent
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceEventStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.EventId
import java.sql.Timestamp
import java.time.Instant

@Component
class DeviceEventPersistenceAdapter(
  private val jdbcTemplate: JdbcTemplate,
) : DeviceEventStore {

  @Transactional
  override fun saveAll(events: List<DeviceEvent>) {
    if (events.isEmpty()) return

    jdbcTemplate.batchUpdate(
      INSERT_SQL,
      events,
      BATCH_SIZE,
    ) { statement, event ->
      statement.setLong(1, event.id.value)
      statement.setLong(2, event.deviceId.value)
      statement.setTimestamp(3, Timestamp.from(event.recordedDate))
      statement.setTimestamp(4, Timestamp.from(event.startDate))
      statement.setLong(5, event.statusFlags)
      statement.setString(6, event.typeCode)
    }
  }

  override fun findByDeviceAndTimeRange(
    deviceId: DeviceId,
    from: Instant,
    to: Instant,
  ): List<DeviceEvent> {
    require(!to.isBefore(from)) {
      "End of time range must not precede start"
    }

    return jdbcTemplate.query(
      FIND_BY_DEVICE_AND_TIME_RANGE_SQL,
      { rs, _ ->
        DeviceEvent(
          id = EventId(rs.getLong("id")),
          deviceId = DeviceId(rs.getLong("device_id")),
          recordedDate = rs.getTimestamp("recorded_date").toInstant(),
          startDate = rs.getTimestamp("start_date").toInstant(),
          statusFlags = rs.getLong("status_flags"),
          typeCode = rs.getString("type_code"),
        )
      },
      deviceId.value,
      Timestamp.from(from),
      Timestamp.from(to),
    )
  }

  companion object {
    private const val BATCH_SIZE = 500

    private val INSERT_SQL = """
      INSERT INTO device_event (
        id,
        device_id,
        recorded_date,
        start_date,
        status_flags,
        type_code
      )
      VALUES (?, ?, ?, ?, ?, ?)
      ON CONFLICT (id) DO NOTHING
    """.trimIndent()

    private val FIND_BY_DEVICE_AND_TIME_RANGE_SQL = """
      SELECT
        id,
        device_id,
        recorded_date,
        start_date,
        status_flags,
        type_code
      FROM device_event
      WHERE device_id = ?
        AND start_date >= ?
        AND start_date < ?
      ORDER BY start_date ASC, id ASC
    """.trimIndent()
  }
}
