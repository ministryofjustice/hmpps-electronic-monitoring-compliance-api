package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.inbound.scheduling

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.activation.DiscoverDeviceActivations

@Component
class DeviceActivationDiscoveryScheduler(
  private val discoverDeviceActivations: DiscoverDeviceActivations,
) {

  @Scheduled(
    fixedDelayString = "\${compliance.discovery.fixed-delay:PT5M}",
    initialDelayString = "\${compliance.discovery.initial-delay:PT30S}",
  )
  @SchedulerLock(
    name = "deviceActivationDiscovery",
    lockAtMostFor = "PT30M",
  )
  fun discover() {
    discoverDeviceActivations.execute()
  }
}
