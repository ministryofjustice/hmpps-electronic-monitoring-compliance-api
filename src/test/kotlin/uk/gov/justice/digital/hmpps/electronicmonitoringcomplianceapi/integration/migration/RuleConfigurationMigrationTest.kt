package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.migration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.configuration.RuleConfigurationRepository
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.MigrationIntegrationTestBase

class RuleConfigurationMigrationTest : MigrationIntegrationTestBase() {

  @Autowired
  private lateinit var repository: RuleConfigurationRepository

  @Test
  fun `it should create the initial battery level rule configuration`() {
    val configurations =
      repository.findAllByStatus(RuleConfigurationStatus.PUBLISHED)

    val configuration = configurations.single {
      it.ruleId == "BATTERY_LEVEL" &&
        it.ruleVersion == 1
    }

    assertThat(configuration.ruleId).isEqualTo("BATTERY_LEVEL")
    assertThat(configuration.ruleVersion).isEqualTo(1)
    assertThat(configuration.revision).isEqualTo(1)
    assertThat(configuration.parameters["threshold"]).isEqualTo(20)
  }
}
