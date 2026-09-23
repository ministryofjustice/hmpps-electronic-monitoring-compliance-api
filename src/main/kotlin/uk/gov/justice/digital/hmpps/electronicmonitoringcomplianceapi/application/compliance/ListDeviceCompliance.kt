package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.compliance

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.ComplianceState
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceComplianceStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceStatus

@Service
class ListDeviceCompliance(
  private val store: DeviceComplianceStore,
) {
  fun list(): DeviceComplianceList {
    val devices = store.findAllSummaries()

    return DeviceComplianceList(
      summary = DeviceComplianceCounts(
        compliant = devices.count { it.state == ComplianceState.COMPLIANT },
        nonCompliant = devices.count { it.state == ComplianceState.NON_COMPLIANT },
        deactivated = devices.count { it.status == DeviceStatus.DEACTIVATED },
      ),
      devices = devices,
    )
  }
}
