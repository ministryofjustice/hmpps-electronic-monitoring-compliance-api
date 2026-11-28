package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.adapter.outbound.persistence.telemetry

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceEvent
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceEventStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.EventId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.IntegrationTestBase
import java.time.Instant

class DeviceEventPersistenceAdapterTest : IntegrationTestBase() {

  @Autowired
  private lateinit var store: DeviceEventStore

  @Autowired
  private lateinit var jdbcTemplate: JdbcTemplate

  @BeforeEach
  fun setUp() {
    jdbcTemplate.update("DELETE FROM device_event")
  }

  @Test
  fun `it should persist a device event`() {
    val event = createEvent()

    store.saveAll(listOf(event))

    val result = findAllEvents()

    assertThat(result).containsExactly(event)
  }

  @Test
  fun `it should persist multiple events`() {
    val events = (1L..10L).map { id ->
      createEvent(id = id)
    }

    store.saveAll(events)

    assertThat(findAllEvents()).containsExactlyElementsOf(events)
  }

  @Test
  fun `it should persist events across multiple JDBC batches`() {
    val events = (1L..1200L).map { id ->
      createEvent(id = id)
    }

    store.saveAll(events)

    assertThat(countEvents()).isEqualTo(1200)
    assertThat(findAllEvents())
      .containsExactlyInAnyOrderElementsOf(events)
  }

  @Test
  fun `it should not duplicate events when synchronised repeatedly`() {
    val event = createEvent()

    store.saveAll(listOf(event))
    store.saveAll(listOf(event))

    assertThat(countEvents()).isEqualTo(1)
  }

  @Test
  fun `it should ignore duplicate event ids within a batch`() {
    val events = listOf(
      createEvent(id = 1L),
      createEvent(id = 1L),
      createEvent(id = 2L),
    )

    store.saveAll(events)

    assertThat(countEvents()).isEqualTo(2)
  }

  @Test
  fun `it should preserve the first event when a duplicate has different values`() {
    val original = createEvent(
      id = 1L,
      statusFlags = 25,
    )

    val duplicate = original.copy(
      statusFlags = 30,
    )

    store.saveAll(listOf(original))
    store.saveAll(listOf(duplicate))

    assertThat(findAllEvents()).containsExactly(original)
  }

  @Test
  fun `it should retrieve events for a device within a time range`() {
    val events = listOf(
      createEvent(
        id = 1L,
        startDate = Instant.parse("2026-01-01T09:59:00Z"),
      ),
      createEvent(
        id = 2L,
        startDate = Instant.parse("2026-01-01T10:00:00Z"),
      ),
      createEvent(
        id = 3L,
        startDate = Instant.parse("2026-01-01T10:05:00Z"),
      ),
      createEvent(
        id = 4L,
        startDate = Instant.parse("2026-01-01T10:10:00Z"),
      ),
      createEvent(
        id = 5L,
        deviceId = 456,
        startDate = Instant.parse("2026-01-01T10:05:00Z"),
      ),
    )

    store.saveAll(events)

    val result = store.findByDeviceAndTimeRange(
      deviceId = DeviceId(123),
      from = Instant.parse("2026-01-01T10:00:00Z"),
      to = Instant.parse("2026-01-01T10:10:00Z"),
    )

    assertThat(result).containsExactly(events[1], events[2])
  }

  @Test
  fun `it should return an empty list when no events match`() {
    val result = store.findByDeviceAndTimeRange(
      deviceId = DeviceId(123),
      from = Instant.parse("2026-01-01T10:00:00Z"),
      to = Instant.parse("2026-01-01T11:00:00Z"),
    )

    assertThat(result).isEmpty()
  }

  @Test
  fun `it should reject an invalid time range`() {
    assertThatThrownBy {
      store.findByDeviceAndTimeRange(
        deviceId = DeviceId(123),
        from = Instant.parse("2026-01-01T11:00:00Z"),
        to = Instant.parse("2026-01-01T10:00:00Z"),
      )
    }.isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `it should handle an empty batch`() {
    store.saveAll(emptyList())

    assertThat(countEvents()).isZero()
  }

  @Test
  fun `it should roll back all batches when an insert fails`() {
    val events = (1L..1200L).map { id ->
      createEvent(id = id)
    }.toMutableList()

    events[1100] = createEvent(
      id = 1101L,
      typeCode = "X".repeat(151),
    )

    assertThatThrownBy {
      store.saveAll(events)
    }.isInstanceOf(DataIntegrityViolationException::class.java)

    assertThat(countEvents()).isZero()
  }

  @Test
  fun `it should persist different event types for the same device`() {
    val batteryEvent = createEvent(
      id = 1L,
      typeCode = "EV_REPORT_TRACKER_BATTERY_PERCENTAGE",
    )

    val tamperEvent = createEvent(
      id = 2L,
      typeCode = "EV_PARTIAL_CALLBACK",
    )

    store.saveAll(listOf(batteryEvent, tamperEvent))

    assertThat(findAllEvents())
      .containsExactly(batteryEvent, tamperEvent)
  }

  private fun createEvent(
    id: Long = 1L,
    deviceId: Long = 123,
    typeCode: String = "EV_REPORT_TRACKER_BATTERY_PERCENTAGE",
    recordedDate: Instant =
      Instant.parse("2026-01-01T10:00:00Z"),
    startDate: Instant = Instant.parse("2026-01-01T10:00:00Z"),
    statusFlags: Long = 25,
  ) = DeviceEvent(
    id = EventId(id),
    deviceId = DeviceId(deviceId),
    recordedDate = recordedDate,
    startDate = startDate,
    statusFlags = statusFlags,
    typeCode = typeCode,
  )

  private fun countEvents(): Long = jdbcTemplate.queryForObject(
    "SELECT COUNT(*) FROM device_event",
    Long::class.java,
  )!!

  private fun findAllEvents(): List<DeviceEvent> = jdbcTemplate.query(
    """
        SELECT id, device_id,
               recorded_date, start_date, status_flags, type_code
        FROM device_event
        ORDER BY id
    """.trimIndent(),
  ) { rs, _ ->
    DeviceEvent(
      id = EventId(rs.getLong("id")),
      deviceId = DeviceId(rs.getLong("device_id")),
      recordedDate = rs.getTimestamp("recorded_date").toInstant(),
      startDate = rs.getTimestamp("start_date").toInstant(),
      statusFlags = rs.getLong("status_flags"),
      typeCode = rs.getString("type_code"),
    )
  }
}
