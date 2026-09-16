package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.aws

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider

@Configuration
class AwsConfiguration {

  // The default credentials provider allows access to Cloud Platform resources
  @Bean
  fun awsCredentialsProvider(): AwsCredentialsProvider = DefaultCredentialsProvider.builder().build()
}
