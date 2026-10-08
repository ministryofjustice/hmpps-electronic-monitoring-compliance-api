package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.usecase

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils.FakeRuleConfigurationStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils.RuleConfigurationFixtures.givenDraftBatteryLevelConfiguration
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils.RuleConfigurationFixtures.givenPublishedBatteryLevelConfiguration
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class PublishRuleConfigurationTest {

  private val publishedAt = Instant.parse("2026-10-08T10:00:00Z")

  private val clock = Clock.fixed(
    publishedAt,
    ZoneOffset.UTC,
  )

  @Test
  fun `it should publish an existing draft`() {
    // Given an existing draft
    val draft = givenDraftBatteryLevelConfiguration()

    val store = FakeRuleConfigurationStore(
      listOf(draft),
    )

    val useCase = PublishRuleConfiguration(
      store = store,
      clock = clock,
    )

    // When we publish the draft
    val result = useCase.publish(
      id = draft.id.value,
      user = "test-user",
    )

    // Then it should return the published configuration
    assertThat(result.id).isEqualTo(draft.id.value)
    assertThat(result.ruleId).isEqualTo("BATTERY_LEVEL")
    assertThat(result.ruleVersion).isEqualTo(1)
    assertThat(result.revision).isEqualTo(2)
    assertThat(result.status).isEqualTo(RuleConfigurationStatus.PUBLISHED)
    assertThat(result.summary).isNull()

    // And the published state should be persisted
    val saved = store.findById(draft.id.value)!!

    assertThat(saved.status).isEqualTo(RuleConfigurationStatus.PUBLISHED)
    assertThat(saved.publishedAt).isEqualTo(publishedAt)
    assertThat(saved.publishedBy).isEqualTo("test-user")
    assertThat(saved.effectiveFrom).isEqualTo(publishedAt)
  }

  @Test
  fun `it should preserve the existing published revision`() {
    // Given an existing published revision and a draft
    val published = givenPublishedBatteryLevelConfiguration(
      revision = 1,
      threshold = 20,
    )

    val draft = givenDraftBatteryLevelConfiguration(
      threshold = 50,
    )

    val originalEffectiveFrom = published.effectiveFrom
    val originalPublishedAt = published.publishedAt

    val store = FakeRuleConfigurationStore(
      listOf(published, draft),
    )

    val useCase = PublishRuleConfiguration(
      store = store,
      clock = clock,
    )

    // When we publish the draft
    useCase.publish(
      id = draft.id.value,
      user = "test-user",
    )

    // Then both revisions should have a state of published
    val previousRevision = store.findById(published.id.value)!!
    val newRevision = store.findById(draft.id.value)!!

    assertThat(previousRevision.status).isEqualTo(RuleConfigurationStatus.PUBLISHED)
    assertThat(previousRevision.effectiveFrom).isEqualTo(originalEffectiveFrom)
    assertThat(previousRevision.publishedAt).isEqualTo(originalPublishedAt)

    assertThat(newRevision.status).isEqualTo(RuleConfigurationStatus.PUBLISHED)
    assertThat(newRevision.effectiveFrom).isEqualTo(publishedAt)

    assertThat(previousRevision.revision.value).isEqualTo(1)
    assertThat(newRevision.revision.value).isEqualTo(2)
  }

  @Test
  fun `it should throw when the configuration does not exist`() {
    // Given an unknown configuration ID
    val id = UUID.randomUUID()

    val useCase = PublishRuleConfiguration(
      store = FakeRuleConfigurationStore(),
      clock = clock,
    )

    // When we attempt to publish it
    assertThatThrownBy {
      useCase.publish(
        id = id,
        user = "test-user",
      )
    }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessage("Rule configuration with id $id not found")
  }

  @Test
  fun `it should reject an already published configuration`() {
    // Given an already published configuration
    val published = givenPublishedBatteryLevelConfiguration()

    val store = FakeRuleConfigurationStore(
      listOf(published),
    )

    val useCase = PublishRuleConfiguration(
      store = store,
      clock = clock,
    )

    // When we attempt to publish it again
    assertThatThrownBy {
      useCase.publish(
        id = published.id.value,
        user = "test-user",
      )
    }
      .isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Only draft rule configurations can be published")

    // Then the existing published configuration should remain unchanged
    val saved = store.findById(published.id.value)!!

    assertThat(saved.status).isEqualTo(RuleConfigurationStatus.PUBLISHED)
    assertThat(saved.effectiveFrom).isEqualTo(published.effectiveFrom)
  }
}
