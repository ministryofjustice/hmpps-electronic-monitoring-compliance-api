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
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.ElectronicMonitoringDataStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.events.BatteryLevelReported

@Component
class AthenaElectronicMonitoringDataStore(
  private val datastoreAthenaClient: AthenaClient,
  private val properties: ElectronicMonitoringDataStoreProperties,
  private val batteryLevelEventMapper: AthenaBatteryLevelEventMapper,
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
    """
    SELECT
      event_id,
      device_id,
      event_recorded_date_utc,
      event_status_flags
    FROM "${properties.athena.database}"."${properties.athena.eventsTable}"
    WHERE event_type_code = 'EV_REPORT_TRACKER_BATTERY_PERCENTAGE'
    ORDER BY device_id, event_recorded_date_utc
    """.trimIndent()

  private fun Row.toMap(
    columns: List<String>,
  ): Map<String, String?> = columns
    .zip(
      data().map { it.varCharValue() },
    )
    .toMap()
}
