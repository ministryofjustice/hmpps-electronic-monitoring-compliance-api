package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ElectronicMonitoringComplianceApi

fun main(args: Array<String>) {
  runApplication<ElectronicMonitoringComplianceApi>(*args)
}
