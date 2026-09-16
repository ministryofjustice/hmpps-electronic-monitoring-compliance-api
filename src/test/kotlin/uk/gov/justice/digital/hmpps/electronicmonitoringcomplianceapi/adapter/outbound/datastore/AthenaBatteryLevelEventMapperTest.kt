package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.datastore

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.datastore.mapper.AthenaBatteryLevelEventMapper
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryPercentage
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.EventId
import java.time.Instant

class AthenaBatteryLevelEventMapperTest {

  private val mapper = AthenaBatteryLevelEventMapper()

  @Test
  fun `it should map a battery level event`() {
    val event = mapper.map(
      mapOf(
        "event_id" to "123",
        "device_id" to "456",
        "event_recorded_date_utc" to "2026-01-01 10:30:15.123",
        "event_status_flags" to "20",
      ),
    )

    assertThat(event.eventId)
      .isEqualTo(EventId(123))

    assertThat(event.deviceId)
      .isEqualTo(DeviceId(456))

    assertThat(event.recordedAt)
      .isEqualTo(
        Instant.parse("2026-01-01T10:30:15.123Z"),
      )

    assertThat(event.batteryPercentage)
      .isEqualTo(BatteryPercentage(20))
  }
}
