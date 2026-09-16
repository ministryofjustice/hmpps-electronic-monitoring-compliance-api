package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.backfill

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.evaluation.EvaluateBatteryLevelEvent
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.ElectronicMonitoringDataStore

@Service
class BackfillBatteryLevelCompliance(
  private val datastore: ElectronicMonitoringDataStore,
  private val evaluateBatteryLevelEvent: EvaluateBatteryLevelEvent,
) {
  fun backfill(): Long {
    var count = 0L

    datastore
      .getBatteryLevelReportedEvents()
      .forEach { event ->
        evaluateBatteryLevelEvent.evaluate(event)
        count++
      }

    return count
  }
}
