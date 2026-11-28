package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.backfill

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.evaluation.EvaluateBatteryLevelEvent
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryPercentage
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.EventId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.events.BatteryLevelReported
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils.FakeElectronicMonitoringDataStore
import java.time.Instant

class BackfillBatteryLevelComplianceTest {

  private val evaluateBatteryLevelEvent =
    mock<EvaluateBatteryLevelEvent>()

  @Test
  fun `it should evaluate all battery level events`() {
    val events = listOf(
      givenEvent(
        eventId = 1,
        batteryPercentage = 50,
      ),
      givenEvent(
        eventId = 2,
        batteryPercentage = 10,
      ),
    )

    // Given a fake datastore with two events
    val datastore = FakeElectronicMonitoringDataStore(
      events = events,
    )

    val backfill = BackfillBatteryLevelCompliance(
      datastore = datastore,
      evaluateBatteryLevelEvent = evaluateBatteryLevelEvent,
    )

    // When we backfill the battery level compliance
    val count = backfill.backfill()

    // Then we should have evaluated both events
    assertThat(count).isEqualTo(2)

    // And we should have called the evaluate method for each event
    verify(evaluateBatteryLevelEvent)
      .evaluate(events[0])

    verify(evaluateBatteryLevelEvent)
      .evaluate(events[1])
  }

  private fun givenEvent(
    eventId: Long,
    batteryPercentage: Int,
  ) = BatteryLevelReported(
    eventId = EventId(eventId),
    deviceId = DeviceId(123),
    recordedAt = Instant.parse(
      "2026-01-01T10:00:00Z",
    ),
    batteryPercentage = BatteryPercentage(
      batteryPercentage,
    ),
  )
}
