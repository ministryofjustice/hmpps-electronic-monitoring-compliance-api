package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.events

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryPercentage
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.EventId
import java.time.Instant

data class BatteryLevelReported(
  val eventId: EventId,
  val deviceId: DeviceId,
  val recordedAt: Instant,
  val batteryPercentage: BatteryPercentage,
)
