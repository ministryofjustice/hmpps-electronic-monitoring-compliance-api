package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.aws

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.athena.AthenaClient
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.datastore.ElectronicMonitoringDataStoreProperties

@Configuration
@EnableConfigurationProperties(ElectronicMonitoringDataStoreProperties::class, AwsProperties::class)
class DatastoreAwsConfiguration {

  @Bean
  fun datastoreStsClient(
    awsCredentialsProvider: AwsCredentialsProvider,
    awsProperties: AwsProperties,
    dataStoreProperties: ElectronicMonitoringDataStoreProperties,
  ): StsClient {
    val builder = StsClient.builder()
      .region(Region.of(dataStoreProperties.region))
      .credentialsProvider(awsCredentialsProvider)

    awsProperties.endpointUrl?.let {
      builder.endpointOverride(it)
    }

    return builder.build()
  }

  @Bean
  fun datastoreAwsCredentialsProvider(
    datastoreStsClient: StsClient,
    properties: ElectronicMonitoringDataStoreProperties,
  ): AwsCredentialsProvider = StsAssumeRoleCredentialsProvider.builder()
    .stsClient(datastoreStsClient)
    .refreshRequest { it ->
      it
        .roleArn(properties.roleArn)
        .roleSessionName(properties.sessionName)
    }
    .build()

  @Bean
  fun datastoreAthenaClient(
    @Qualifier("datastoreAwsCredentialsProvider")
    datastoreAwsCredentialsProvider: AwsCredentialsProvider,
    properties: ElectronicMonitoringDataStoreProperties,
  ): AthenaClient {
    val builder = AthenaClient.builder()
      .region(Region.of(properties.region))
      .credentialsProvider(datastoreAwsCredentialsProvider)

    properties.athena.endpointUrl?.let {
      builder.endpointOverride(it)
    }

    return builder.build()
  }
}
