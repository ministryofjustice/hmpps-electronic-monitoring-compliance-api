package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.inbound.web.configuration

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.compliance.DeviceComplianceList
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.compliance.ListDeviceCompliance

@RestController
@RequestMapping(value = ["/v1/device-compliance"])
class DeviceComplianceController(
  private val listDeviceCompliance: ListDeviceCompliance,
) {
  @GetMapping
  fun listDeviceCompliance(): DeviceComplianceList = listDeviceCompliance.list()
}
