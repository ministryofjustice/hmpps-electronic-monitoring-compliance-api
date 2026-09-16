package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.adapter.outbound.datastore

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.datastore.AthenaElectronicMonitoringDataStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryPercentage
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.EventId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.events.BatteryLevelReported
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.wiremock.AthenaColumn
import java.time.Instant

class AthenaElectronicMonitoringDataStoreTest : IntegrationTestBase() {

  @Autowired
  private lateinit var datastore: AthenaElectronicMonitoringDataStore

  @Test
  fun `it should get battery level reported events`() {
    // Given Athena contains battery level events
    stubQueryExecution(
      columns = listOf(
        AthenaColumn(
          name = "event_id",
          type = "integer",
        ),
        AthenaColumn(
          name = "device_id",
          type = "integer",
        ),
        AthenaColumn(
          name = "event_recorded_date_utc",
          type = "timestamp",
        ),
        AthenaColumn(
          name = "event_status_flags",
          type = "integer",
        ),
      ),
      rows = listOf(
        listOf(
          "1",
          "123",
          "2026-01-01 10:00:00.000",
          "50",
        ),
        listOf(
          "2",
          "123",
          "2026-01-01 10:05:00.000",
          "10",
        ),
      ),
    )

    // When the battery level events are retrieved
    val events = datastore
      .getBatteryLevelReportedEvents()
      .toList()

    // Then the events should be mapped to the domain
    assertThat(events).containsExactly(
      BatteryLevelReported(
        eventId = EventId(1),
        deviceId = DeviceId(123),
        recordedAt = Instant.parse("2026-01-01T10:00:00Z"),
        batteryPercentage = BatteryPercentage(50),
      ),
      BatteryLevelReported(
        eventId = EventId(2),
        deviceId = DeviceId(123),
        recordedAt = Instant.parse("2026-01-01T10:05:00Z"),
        batteryPercentage = BatteryPercentage(10),
      ),
    )

    // And Athena should have been queried
    verifyAthenaStartQueryExecutionCount(1)
    verifyAthenaGetQueryExecutionCount(1)
    verifyAthenaGetQueryResultsCount(1)

    // And the query should have been for battery level events
    verifyAthenaStartQueryExecutionWithQuery(
      query = """
        SELECT
          event_id,
          device_id,
          event_recorded_date_utc,
          event_status_flags
        FROM "test_database"."events"
        WHERE event_type_code = 'EV_REPORT_TRACKER_BATTERY_PERCENTAGE'
        ORDER BY device_id, event_recorded_date_utc
      """.trimIndent(),
      executionParameters = emptyList(),
    )
  }

  @Test
  fun `it should poll Athena until the query succeeds`() {
    // Given the Athena query is initially still running
    stubQueryExecution(
      columns = listOf(
        AthenaColumn("event_id", "integer"),
        AthenaColumn("device_id", "integer"),
        AthenaColumn("event_recorded_date_utc", "timestamp"),
        AthenaColumn("event_status_flags", "integer"),
      ),
      rows = emptyList(),
      retryCount = 3,
    )

    // When the battery level events are retrieved
    val events = datastore
      .getBatteryLevelReportedEvents()
      .toList()

    // Then no events should be returned
    assertThat(events).isEmpty()

    // And Athena should be polled until the query succeeds
    verifyAthenaStartQueryExecutionCount(1)
    verifyAthenaGetQueryExecutionCount(3)
    verifyAthenaGetQueryResultsCount(1)
  }
}
