
package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry
import java.time.Instant

data class DeviceEvent(
  val id: EventId,
  val deviceId: DeviceId,
  val recordedDate: Instant,
  val startDate: Instant,
  val statusFlags: Long,
  val typeCode: String,
)
