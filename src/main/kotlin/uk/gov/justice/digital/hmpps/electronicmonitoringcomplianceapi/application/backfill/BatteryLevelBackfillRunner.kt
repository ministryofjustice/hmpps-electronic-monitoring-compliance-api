package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.backfill

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.compliance.SynchroniseDeviceCompliance
import kotlin.system.exitProcess

@Component
@ConditionalOnProperty(
  name = ["backfill.battery-level.enabled"],
  havingValue = "true",
)
class BatteryLevelBackfillRunner(
  private val synchroniseDeviceCompliance: SynchroniseDeviceCompliance,
  private val backfillBatteryLevelCompliance: BackfillBatteryLevelCompliance,
) : ApplicationRunner {

  override fun run(args: ApplicationArguments) {
    log.info("Synchronising device compliance")

    val deviceCount =
      synchroniseDeviceCompliance.synchronise()

    log.info(
      "Synchronised {} devices",
      deviceCount,
    )

    log.info("Starting battery level compliance backfill")

    val eventCount =
      backfillBatteryLevelCompliance.backfill()

    log.info(
      "Battery level compliance backfill completed. Evaluated {} events",
      eventCount,
    )

    exitProcess(0)
  }

  companion object {
    private val log =
      LoggerFactory.getLogger(BatteryLevelBackfillRunner::class.java)
  }
}
