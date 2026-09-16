package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.datastore

import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI

@ConfigurationProperties("electronic-monitoring-datastore")
data class ElectronicMonitoringDataStoreProperties(
  val roleArn: String,
  val region: String,
  val sessionName: String,
  val athena: AthenaProperties,
) {
  data class AthenaProperties(
    val database: String,
    val eventsTable: String,
    val workGroup: String,
    val endpointUrl: URI? = null,
  )
}
