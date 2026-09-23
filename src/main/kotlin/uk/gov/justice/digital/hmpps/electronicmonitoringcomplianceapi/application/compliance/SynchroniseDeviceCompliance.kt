package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.compliance

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceCompliance
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceComplianceStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.ElectronicMonitoringDataStore

@Service
class SynchroniseDeviceCompliance(
  private val datastore: ElectronicMonitoringDataStore,
  private val ruleConfigurationStore: RuleConfigurationStore,
  private val deviceComplianceStore: DeviceComplianceStore,
) {

  fun synchronise(): Int {
    val ruleDefinitions =
      ruleConfigurationStore
        .findPublished()
        .map { it.ruleDefinition }

    val existing =
      deviceComplianceStore
        .findAll()
        .associateBy { it.deviceId }

    var count = 0

    datastore.getDevices().forEach { device ->
      val compliance = existing[device.id]

      if (compliance == null) {
        deviceComplianceStore.save(
          DeviceCompliance.create(
            deviceId = device.id,
            status = device.status,
            ruleDefinitions = ruleDefinitions,
          ),
        )
      } else {
        compliance.synchronise(
          status = device.status,
          ruleDefinitions = ruleDefinitions,
        )

        deviceComplianceStore.save(compliance)
      }

      count++
    }

    log.info(
      "Device compliance synchronisation completed. Synchronised {} devices",
      count,
    )

    return count
  }

  companion object {
    private val log =
      LoggerFactory.getLogger(SynchroniseDeviceCompliance::class.java)
  }
}
