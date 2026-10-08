package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.usecase.GetRuleConfigurationDraft
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfiguration
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationRevision
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleParameters
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryPercentage
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils.FakeRuleConfigurationStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils.RuleComplianceFixtures.givenPublishedBatteryLevelConfiguration
import java.time.Instant
import java.util.UUID

class GetRuleConfigurationDraftTest {
  @Test
  fun `it should return the draft for the source rule configuration`() {
    // Given a published rule configuration
    val published = givenPublishedBatteryLevelConfiguration(
      revision = 1,
    )

    // And a draft rule configuration for the same rule definition
    val draft = RuleConfiguration.createDraft(
      ruleDefinition = published.ruleDefinition,
      revision = RuleConfigurationRevision(2),
      parameters = BatteryLevelRuleParameters(
        threshold = BatteryPercentage(50),
      ),
      createdAt = Instant.parse("2026-09-23T12:00:00Z"),
      createdBy = "test-user",
    )

    val useCase = GetRuleConfigurationDraft(
        FakeRuleConfigurationStore(
            listOf(published, draft),
        ),
    )

    // When we get the draft for the published rule configuration
    val result = useCase.get(published.id.value)

    // Then it should return the draft rule configuration
    assertThat(result.id).isEqualTo(draft.id.value)
    assertThat(result.revision).isEqualTo(2)
    assertThat(result.status).isEqualTo(
      RuleConfigurationStatus.DRAFT,
    )
    assertThat(result.summary).isNull()
  }

  @Test
  fun `it should throw when source configuration does not exist`() {
    // Given a non-existent rule configuration id
    val id = UUID.randomUUID()

    // And a store with no rule configurations
    val useCase = GetRuleConfigurationDraft(
        FakeRuleConfigurationStore(),
    )

    // When we try to get the draft, it should throw an EntityNotFoundException
    assertThatThrownBy {
      useCase.get(id)
    }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessage(
        "Rule configuration with id $id not found",
      )
  }

  @Test
  fun `it should throw when no draft exists`() {
    // Given a published rule configuration
    val published =
      givenPublishedBatteryLevelConfiguration()

    // And a store with only the published rule configuration
    val useCase = GetRuleConfigurationDraft(
        FakeRuleConfigurationStore(
            listOf(published),
        ),
    )

    // When we try to get the draft, it should throw an EntityNotFoundException
    assertThatThrownBy {
      useCase.get(published.id.value)
    }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessage(
        "No draft rule configuration exists for BATTERY_LEVEL v1",
      )
  }

  @Test
  fun `it should throw when source configuration is a draft`() {
    // Given a draft rule configuration
    val draft = RuleConfiguration.createDraft(
      ruleDefinition = BatteryLevelRuleV1.ruleDefinition,
      revision = RuleConfigurationRevision(2),
      parameters = BatteryLevelRuleParameters(
        threshold = BatteryPercentage(50),
      ),
      createdAt = Instant.parse("2026-09-23T12:00:00Z"),
      createdBy = "test-user",
    )

    val useCase = GetRuleConfigurationDraft(
        FakeRuleConfigurationStore(
            listOf(draft),
        ),
    )

    // When we try to get the draft for the draft rule configuration, it should throw an IllegalArgumentException
    assertThatThrownBy {
      useCase.get(draft.id.value)
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage(
        "A draft can only be retrieved for a published rule configuration",
      )
  }
}
