package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleParameters
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryPercentage
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils.RuleConfigurationFixtures.givenPublishedBatteryLevelConfiguration
import java.time.Instant

class RuleConfigurationTest {
  @Test
  fun `it should update the parameters of a draft configuration`() {
    // Given a draft rule configuration
    val draft = RuleConfiguration.createDraft(
      ruleDefinition = BatteryLevelRuleV1.ruleDefinition,
      revision = RuleConfigurationRevision(2),
      parameters = BatteryLevelRuleParameters(
        threshold = BatteryPercentage(20),
      ),
      createdAt = Instant.parse("2026-10-08T09:00:00Z"),
      createdBy = "test-user",
    )

    val originalId = draft.id
    val originalRevision = draft.revision
    val originalCreatedAt = draft.createdAt
    val originalCreatedBy = draft.createdBy

    // When the parameters are updated
    draft.updateParameters(
      BatteryLevelRuleParameters(
        threshold = BatteryPercentage(50),
      ),
    )

    // Then the parameters should change
    assertThat(draft.parameters).isEqualTo(
      BatteryLevelRuleParameters(
        threshold = BatteryPercentage(50),
      ),
    )

    // And the configuration identity and metadata should remain unchanged
    assertThat(draft.id).isEqualTo(originalId)
    assertThat(draft.revision).isEqualTo(originalRevision)
    assertThat(draft.status).isEqualTo(RuleConfigurationStatus.DRAFT)
    assertThat(draft.createdAt).isEqualTo(originalCreatedAt)
    assertThat(draft.createdBy).isEqualTo(originalCreatedBy)
  }

  @Test
  fun `it should not update the parameters of a published configuration`() {
    // Given a published rule configuration
    val published = givenPublishedBatteryLevelConfiguration(
      threshold = 20,
    )

    // When we attempt to update the parameters
    assertThatThrownBy {
      published.updateParameters(
        BatteryLevelRuleParameters(
          threshold = BatteryPercentage(50),
        ),
      )
    }
      .isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Only draft rule configurations can be changed")

    // Then the original parameters should remain unchanged
    assertThat(published.parameters).isEqualTo(
      BatteryLevelRuleParameters(
        threshold = BatteryPercentage(20),
      ),
    )
  }
}
