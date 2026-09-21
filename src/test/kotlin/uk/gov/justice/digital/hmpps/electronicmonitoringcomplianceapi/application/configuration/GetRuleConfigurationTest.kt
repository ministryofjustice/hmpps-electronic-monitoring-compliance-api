package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceRuleComplianceCounts
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils.FakeDeviceComplianceStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils.FakeRuleConfigurationStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils.RuleComplianceFixtures.givenPublishedBatteryLevelConfiguration
import java.util.UUID

class GetRuleConfigurationTest {

  @Test
  fun `it should get rule configuration with compliance summary`() {
    // Given a published rule configuration
    val configuration = givenPublishedBatteryLevelConfiguration(
      threshold = 20,
    )
    val configurationStore = FakeRuleConfigurationStore(listOf(configuration))
    val complianceStore = FakeDeviceComplianceStore()

    val useCase = GetRuleConfiguration(
      store = configurationStore,
      complianceStore = complianceStore,
    )

    // When we get the rule configuration by ID
    val result = useCase.get(
      configuration.id.value,
    )

    // Then the result should match the configuration and include a compliance summary
    assertThat(result.id).isEqualTo(configuration.id.value)
    assertThat(result.ruleId).isEqualTo(
      BatteryLevelRuleV1.ruleDefinition.id.value,
    )
    assertThat(result.ruleVersion).isEqualTo(
      BatteryLevelRuleV1.ruleDefinition.version.value,
    )
    assertThat(result.revision).isEqualTo(configuration.revision.value)
    assertThat(result.status).isEqualTo(RuleConfigurationStatus.PUBLISHED)
    assertThat(result.parameters).isEqualTo(configuration.parameters)
    assertThat(result.summary).isEqualTo(
      DeviceRuleComplianceCounts(
        compliant = 10,
        nonCompliant = 5,
        noData = 2,
        deactivated = 3,
      ),
    )
  }

  @Test
  fun `it should throw when rule configuration does not exist`() {
    // Given a rule configuration ID that does not exist
    val id = UUID.randomUUID()

    val useCase = GetRuleConfiguration(
      store = FakeRuleConfigurationStore(),
      complianceStore = FakeDeviceComplianceStore(),
    )

    // When we get the rule configuration, it should throw an EntityNotFoundException
    assertThatThrownBy {
      useCase.get(id)
    }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessage(
        "Rule configuration not found: $id",
      )
  }
}
