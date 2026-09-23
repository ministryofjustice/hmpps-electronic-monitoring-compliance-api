package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.aws

import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI

@ConfigurationProperties("aws")
data class AwsProperties(
  val endpointUrl: URI? = null,
)
