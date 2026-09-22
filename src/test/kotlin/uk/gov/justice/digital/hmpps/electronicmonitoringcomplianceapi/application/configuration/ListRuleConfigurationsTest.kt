package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.RuleConfigurationSummary
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfiguration
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationRevision
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleParameters
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryPercentage
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils.FakeRuleConfigurationStore
import java.time.Instant
import java.util.UUID

class ListRuleConfigurationsTest {
  @Test
  fun `lists published rule configurations`() {
    // Given a store with a single saved rule configuration
    val configuration = RuleConfiguration.rehydrate(
      id = RuleConfigurationId(UUID.randomUUID()),
      ruleDefinition = BatteryLevelRuleV1.ruleDefinition,
      revision = RuleConfigurationRevision(3),
      parameters = BatteryLevelRuleParameters(
        threshold = BatteryPercentage(20),
      ),
      status = RuleConfigurationStatus.PUBLISHED,
      createdAt = Instant.parse("2026-09-01T10:00:00Z"),
      createdBy = "user",
      publishedAt = Instant.parse("2026-09-02T10:00:00Z"),
      publishedBy = "user",
      effectiveFrom = Instant.parse("2026-09-02T10:00:00Z"),
    )
    val store = FakeRuleConfigurationStore(listOf(configuration))

    // When we list the rule configurations
    val useCase = ListRuleConfigurations(store)
    val result = useCase.list()

    // Then the result should contain a single rule configuration summary
    assertThat(result).containsExactly(
      RuleConfigurationSummary(
        id = configuration.id.value,
        ruleId = "BATTERY_LEVEL",
        ruleVersion = 1,
        revision = 3,
        parameters = RuleConfigurationParametersSummary.BatteryLevel(
          threshold = 20,
        ),
      ),
    )
  }

  @Test
  fun `returns an empty list when there are no published configurations`() {
    // Given an empty store
    val store = FakeRuleConfigurationStore()

    // When we list the rule configurations
    val useCase = ListRuleConfigurations(store)
    val result = useCase.list()

    // Then the result should be an empty list
    assertThat(result).isEmpty()
  }
}
