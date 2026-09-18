package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.datastore

import org.springframework.stereotype.Component
import software.amazon.awssdk.services.athena.AthenaClient
import software.amazon.awssdk.services.athena.model.GetQueryExecutionRequest
import software.amazon.awssdk.services.athena.model.GetQueryResultsRequest
import software.amazon.awssdk.services.athena.model.QueryExecutionContext
import software.amazon.awssdk.services.athena.model.QueryExecutionState
import software.amazon.awssdk.services.athena.model.Row
import software.amazon.awssdk.services.athena.model.StartQueryExecutionRequest
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.datastore.mapper.AthenaBatteryLevelEventMapper
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.datastore.mapper.AthenaDeviceMapper
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.Device
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.ElectronicMonitoringDataStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.events.BatteryLevelReported

@Component
class AthenaElectronicMonitoringDataStore(
  private val datastoreAthenaClient: AthenaClient,
  private val properties: ElectronicMonitoringDataStoreProperties,
  private val batteryLevelEventMapper: AthenaBatteryLevelEventMapper,
  private val deviceMapper: AthenaDeviceMapper,
) : ElectronicMonitoringDataStore {

  override fun getBatteryLevelReportedEvents(): Sequence<BatteryLevelReported> = sequence {
    val queryExecutionId = startQuery(
      batteryLevelEventsQuery(),
    )

    waitForQuery(queryExecutionId)

    val request = GetQueryResultsRequest.builder()
      .queryExecutionId(queryExecutionId)
      .build()

    var firstRow = true

    datastoreAthenaClient
      .getQueryResultsPaginator(request)
      .forEach { page ->
        val columnNames = page
          .resultSet()
          .resultSetMetadata()
          .columnInfo()
          .map { it.name() }

        page.resultSet().rows().forEach { row ->
          if (firstRow) {
            firstRow = false
            return@forEach
          }

          yield(
            batteryLevelEventMapper.map(
              row.toMap(columnNames),
            ),
          )
        }
      }
  }

  override fun getDevices(): Sequence<Device> = sequence {
    val queryExecutionId = startQuery(
      devicesQuery(),
    )

    waitForQuery(queryExecutionId)

    val request = GetQueryResultsRequest.builder()
      .queryExecutionId(queryExecutionId)
      .build()

    var firstRow = true

    datastoreAthenaClient
      .getQueryResultsPaginator(request)
      .forEach { page ->
        val columnNames = page
          .resultSet()
          .resultSetMetadata()
          .columnInfo()
          .map { it.name() }

        page.resultSet().rows().forEach { row ->
          if (firstRow) {
            firstRow = false
            return@forEach
          }

          yield(
            deviceMapper.map(
              row.toMap(columnNames),
            ),
          )
        }
      }
  }

  private fun startQuery(
    query: String,
  ): String = datastoreAthenaClient
    .startQueryExecution(
      StartQueryExecutionRequest.builder()
        .queryString(query)
        .queryExecutionContext(
          QueryExecutionContext.builder()
            .database(properties.athena.database)
            .build(),
        )
        .workGroup(properties.athena.workGroup)
        .build(),
    )
    .queryExecutionId()

  private fun waitForQuery(
    queryExecutionId: String,
  ) {
    while (true) {
      val execution = datastoreAthenaClient
        .getQueryExecution(
          GetQueryExecutionRequest.builder()
            .queryExecutionId(queryExecutionId)
            .build(),
        )
        .queryExecution()

      when (execution.status().state()) {
        QueryExecutionState.SUCCEEDED ->
          return

        QueryExecutionState.FAILED ->
          throw IllegalStateException(
            "Athena query failed: ${execution.status().stateChangeReason()}",
          )

        QueryExecutionState.CANCELLED ->
          throw IllegalStateException(
            "Athena query was cancelled",
          )

        else ->
          Thread.sleep(500)
      }
    }
  }

  private fun batteryLevelEventsQuery(): String =
    // TODO - Revert to older version, data in datastore doesn't align across tables / databases, i.e. devices exist in events that don't exist in device activations
    """
      SELECT
        event_id,
        event.device_id,
        event_recorded_date_utc,
        event_status_flags
      FROM "${properties.athena.eventsDatabase}"."${properties.athena.eventsTable}"
      JOIN (
          SELECT
            device_id,
            device_activation_date,
            device_deactivation_date
          FROM (
            SELECT
              device_id,
              device_activation_date,
              device_deactivation_date,
              ROW_NUMBER() OVER (
                PARTITION BY device_id
                ORDER BY __datetime_added DESC
              ) AS row_number
            FROM "${properties.athena.database}"."${properties.athena.deviceActivationsTable}"
          )
          WHERE row_number = 1
          ORDER BY device_id
      ) da ON da.device_id = event.device_id
      WHERE event_type_code = 'EV_REPORT_TRACKER_BATTERY_PERCENTAGE'
      AND event_recorded_date_utc >= date_add('day', -7, current_date)
      ORDER BY device_id, event_recorded_date_utc
    """.trimIndent()

  private fun devicesQuery(): String =
    """
    SELECT
      device_id,
      device_activation_date,
      device_deactivation_date
    FROM (
      SELECT
        device_id,
        device_activation_date,
        device_deactivation_date,
        ROW_NUMBER() OVER (
          PARTITION BY device_id
          ORDER BY __datetime_added DESC
        ) AS row_number
      FROM "${properties.athena.database}"."${properties.athena.deviceActivationsTable}"
    )
    WHERE row_number = 1
    ORDER BY device_id
    """.trimIndent()

  private fun Row.toMap(
    columns: List<String>,
  ): Map<String, String?> = columns
    .zip(
      data().map { it.varCharValue() },
    )
    .toMap()
}
