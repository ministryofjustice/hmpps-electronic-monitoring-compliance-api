package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.datastore.mapper

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.Device
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import java.time.Instant

@Component
class AthenaDeviceMapper : AthenaMapper() {

  fun map(row: Map<String, String?>): Device {
    val activationDatePassed = parseAthenaTimestamp(
      row.required("device_activation_date"),
    ) < Instant.now()

    val deactivated = row.required("device_deactivation_date") != "9999-12-31 00:00:00.000000"

    return Device(
      id = DeviceId(
        row.required("device_id").toInt(),
      ),
      // TODO - below is likely the correct logic, but the data in Athena is not correct, so we are hardcoding to ACTIVATED for now
      // TODO - i.e. some devices are deactivated but are sending battery level events, some devices are activated but send no events
//      status = if (activationDatePassed && !deactivated) {
//        DeviceStatus.ACTIVATED
//      } else {
//        DeviceStatus.DEACTIVATED
//      },
      status = DeviceStatus.ACTIVATED,
    )
  }
}
