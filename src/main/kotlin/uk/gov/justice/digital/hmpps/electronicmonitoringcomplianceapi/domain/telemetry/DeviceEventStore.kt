
package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry
import java.time.Instant

interface DeviceEventStore {
  fun saveAll(events: List<DeviceEvent>)

  fun findByDeviceAndTimeRange(
    deviceId: DeviceId,
    from: Instant,
    to: Instant,
  ): List<DeviceEvent>
}
