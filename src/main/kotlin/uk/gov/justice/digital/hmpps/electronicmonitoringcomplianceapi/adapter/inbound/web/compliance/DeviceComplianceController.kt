package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.inbound.web.compliance

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.compliance.DeviceComplianceList
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.compliance.DeviceComplianceResponse
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.compliance.GetDeviceCompliance
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.compliance.ListDeviceCompliance
import java.util.UUID

@RestController
@RequestMapping(value = ["/v1/device-compliance"])
class DeviceComplianceController(
  private val getDeviceCompliance: GetDeviceCompliance,
  private val listDeviceCompliance: ListDeviceCompliance,
) {
  @GetMapping(value = ["/{deviceComplianceId}"])
  fun getDeviceCompliance(
    @PathVariable("deviceComplianceId")
    deviceComplianceId: UUID,
  ): DeviceComplianceResponse = getDeviceCompliance.get(deviceComplianceId)

  @GetMapping
  fun listDeviceCompliance(): DeviceComplianceList = listDeviceCompliance.list()
}
