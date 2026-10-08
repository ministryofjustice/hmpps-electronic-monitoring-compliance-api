package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.usecase

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.dto.RuleConfigurationRequest
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleParameterParser
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleParameters
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryPercentage
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils.FakeRuleConfigurationStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils.RuleConfigurationFixtures.givenDraftBatteryLevelConfiguration
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils.RuleConfigurationFixtures.givenPublishedBatteryLevelConfiguration
import java.util.UUID

class UpdateRuleConfigurationDraftTest {

  private val ruleParameterParser = RuleParameterParser()

  @Test
  fun `it should update an existing draft`() {
    // Given an existing draft
    val draft = givenDraftBatteryLevelConfiguration()

    val store = FakeRuleConfigurationStore(
      listOf(draft),
    )

    val useCase = UpdateRuleConfigurationDraft(
      store = store,
      ruleParameterParser = ruleParameterParser,
    )

    // When we update its parameters
    val result = useCase.update(
      id = draft.id.value,
      request = RuleConfigurationRequest(
        parameters = mapOf(
          "threshold" to 50,
        ),
      ),
    )

    // Then the updated configuration should be returned
    assertThat(result.id).isEqualTo(draft.id.value)
    assertThat(result.ruleId).isEqualTo("BATTERY_LEVEL")
    assertThat(result.ruleVersion).isEqualTo(1)
    assertThat(result.revision).isEqualTo(2)
    assertThat(result.status).isEqualTo(RuleConfigurationStatus.DRAFT)
    assertThat(result.summary).isNull()

    assertThat(result.parameters).isEqualTo(
      BatteryLevelRuleParameters(
        threshold = BatteryPercentage(50),
      ),
    )

    // And the updated parameters should be saved
    val saved = store.findById(draft.id.value)

    assertThat(saved).isNotNull
    assertThat(saved!!.parameters).isEqualTo(
      BatteryLevelRuleParameters(
        threshold = BatteryPercentage(50),
      ),
    )
  }

  @Test
  fun `it should preserve the draft revision and creation metadata`() {
    // Given an existing draft
    val draft = givenDraftBatteryLevelConfiguration()

    val originalId = draft.id
    val originalRevision = draft.revision
    val originalCreatedAt = draft.createdAt
    val originalCreatedBy = draft.createdBy

    val store = FakeRuleConfigurationStore(
      listOf(draft),
    )

    val useCase = UpdateRuleConfigurationDraft(
      store = store,
      ruleParameterParser = ruleParameterParser,
    )

    // When we update the draft
    useCase.update(
      id = draft.id.value,
      request = RuleConfigurationRequest(
        parameters = mapOf(
          "threshold" to 50,
        ),
      ),
    )

    // Then the existing configuration should retain its identity
    val saved = store.findById(draft.id.value)!!

    assertThat(saved.id).isEqualTo(originalId)
    assertThat(saved.revision).isEqualTo(originalRevision)
    assertThat(saved.createdAt).isEqualTo(originalCreatedAt)
    assertThat(saved.createdBy).isEqualTo(originalCreatedBy)
    assertThat(saved.status).isEqualTo(RuleConfigurationStatus.DRAFT)
  }

  @Test
  fun `it should throw when the configuration does not exist`() {
    // Given a non-existent configuration
    val id = UUID.randomUUID()

    val useCase = UpdateRuleConfigurationDraft(
      store = FakeRuleConfigurationStore(),
      ruleParameterParser = ruleParameterParser,
    )

    // When we attempt to update it
    assertThatThrownBy {
      useCase.update(
        id = id,
        request = RuleConfigurationRequest(
          parameters = mapOf(
            "threshold" to 50,
          ),
        ),
      )
    }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessage("Rule configuration with id $id not found")
  }

  @Test
  fun `it should not update a published configuration`() {
    // Given a published configuration
    val published = givenPublishedBatteryLevelConfiguration(
      threshold = 20,
    )

    val store = FakeRuleConfigurationStore(
      listOf(published),
    )

    val useCase = UpdateRuleConfigurationDraft(
      store = store,
      ruleParameterParser = ruleParameterParser,
    )

    // When we attempt to update it
    assertThatThrownBy {
      useCase.update(
        id = published.id.value,
        request = RuleConfigurationRequest(
          parameters = mapOf(
            "threshold" to 50,
          ),
        ),
      )
    }
      .isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Only draft rule configurations can be changed")

    // Then the published configuration should remain unchanged
    val saved = store.findById(published.id.value)!!

    assertThat(saved.parameters).isEqualTo(
      BatteryLevelRuleParameters(
        threshold = BatteryPercentage(20),
      ),
    )
  }

  @Test
  fun `it should reject invalid parameters`() {
    // Given an existing draft
    val draft = givenDraftBatteryLevelConfiguration()

    val store = FakeRuleConfigurationStore(
      listOf(draft),
    )

    val useCase = UpdateRuleConfigurationDraft(
      store = store,
      ruleParameterParser = ruleParameterParser,
    )

    // When we attempt to update it with invalid parameters
    assertThatThrownBy {
      useCase.update(
        id = draft.id.value,
        request = RuleConfigurationRequest(
          parameters = emptyMap(),
        ),
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Missing or invalid threshold parameter")

    // Then the original parameters should remain unchanged
    val saved = store.findById(draft.id.value)!!

    assertThat(saved.parameters).isEqualTo(
      BatteryLevelRuleParameters(
        threshold = BatteryPercentage(20),
      ),
    )
  }

  @Test
  fun `it should allow updating a draft multiple times`() {
    // Given an existing draft
    val draft = givenDraftBatteryLevelConfiguration()

    val store = FakeRuleConfigurationStore(
      listOf(draft),
    )

    val useCase = UpdateRuleConfigurationDraft(
      store = store,
      ruleParameterParser = ruleParameterParser,
    )

    // When we update the draft twice
    useCase.update(
      draft.id.value,
      RuleConfigurationRequest(
        parameters = mapOf("threshold" to 40),
      ),
    )

    val result = useCase.update(
      draft.id.value,
      RuleConfigurationRequest(
        parameters = mapOf("threshold" to 50),
      ),
    )

    // Then the latest parameters should be saved without changing revision
    assertThat(result.id).isEqualTo(draft.id.value)
    assertThat(result.revision).isEqualTo(2)

    assertThat(result.parameters).isEqualTo(
      BatteryLevelRuleParameters(
        threshold = BatteryPercentage(50),
      ),
    )
  }
}
