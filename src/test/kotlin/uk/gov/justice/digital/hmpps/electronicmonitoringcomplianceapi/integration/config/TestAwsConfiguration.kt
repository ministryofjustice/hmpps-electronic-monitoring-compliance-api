package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider

@TestConfiguration
class TestAwsConfiguration {

  @Bean
  @Primary
  fun testAwsCredentialsProvider(): AwsCredentialsProvider = StaticCredentialsProvider.create(
    AwsBasicCredentials.create(
      "test",
      "test",
    ),
  )
}
