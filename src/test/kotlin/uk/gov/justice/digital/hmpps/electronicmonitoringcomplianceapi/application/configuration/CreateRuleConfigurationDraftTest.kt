package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration

import jakarta.persistence.EntityExistsException
import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfiguration
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationRevision
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleParameterParser
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleParameters
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryPercentage
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils.FakeRuleConfigurationStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils.RuleComplianceFixtures.givenPublishedBatteryLevelConfiguration
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class CreateRuleConfigurationDraftTest {

  private val clock = Clock.fixed(
    Instant.parse("2026-09-22T10:00:00Z"),
    ZoneOffset.UTC,
  )

  private val parser = RuleParameterParser()

  @Test
  fun `it should create a draft from a published rule configuration`() {
    val source = givenPublishedBatteryLevelConfiguration(
      threshold = 20,
      revision = 1,
    )

    val store = FakeRuleConfigurationStore(
      listOf(source),
    )

    val useCase = CreateRuleConfigurationDraft(
      store = store,
      ruleParameterParser = parser,
      clock = clock,
    )

    val result = useCase.create(
      sourceId = source.id.value,
      request = RuleConfigurationRequest(
        parameters = mapOf(
          "threshold" to 50,
        ),
      ),
      user = "test-user",
    )

    assertThat(result.id)
      .isNotEqualTo(source.id.value)

    assertThat(result.ruleId)
      .isEqualTo(BatteryLevelRuleV1.ruleDefinition.id.value)

    assertThat(result.ruleVersion)
      .isEqualTo(BatteryLevelRuleV1.ruleDefinition.version.value)

    assertThat(result.revision)
      .isEqualTo(2)

    assertThat(result.status)
      .isEqualTo(RuleConfigurationStatus.DRAFT)

    assertThat(result.parameters)
      .isEqualTo(
        BatteryLevelRuleParameters(
          threshold = BatteryPercentage(50),
        ),
      )

    val saved = store.findById(result.id)

    assertThat(saved).isNotNull
    assertThat(saved!!.createdAt)
      .isEqualTo(Instant.parse("2026-09-22T10:00:00Z"))
    assertThat(saved.createdBy)
      .isEqualTo("test-user")
  }

  @Test
  fun `it should not change the source configuration`() {
    val source = givenPublishedBatteryLevelConfiguration(
      threshold = 20,
      revision = 1,
    )

    val store = FakeRuleConfigurationStore(
      listOf(source),
    )

    CreateRuleConfigurationDraft(
      store = store,
      ruleParameterParser = parser,
      clock = clock,
    ).create(
      sourceId = source.id.value,
      request = RuleConfigurationRequest(
        parameters = mapOf(
          "threshold" to 50,
        ),
      ),
      user = "test-user",
    )

    val persistedSource =
      store.findById(source.id.value)

    assertThat(persistedSource!!.status)
      .isEqualTo(RuleConfigurationStatus.PUBLISHED)

    assertThat(persistedSource.revision)
      .isEqualTo(RuleConfigurationRevision(1))

    assertThat(persistedSource.parameters)
      .isEqualTo(
        BatteryLevelRuleParameters(
          threshold = BatteryPercentage(20),
        ),
      )
  }

  @Test
  fun `it should throw when source configuration does not exist`() {
    val id = UUID.randomUUID()

    val useCase = CreateRuleConfigurationDraft(
      store = FakeRuleConfigurationStore(),
      ruleParameterParser = parser,
      clock = clock,
    )

    assertThatThrownBy {
      useCase.create(
        sourceId = id,
        request = RuleConfigurationRequest(
          parameters = mapOf(
            "threshold" to 50,
          ),
        ),
        user = "test-user",
      )
    }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessage(
        "Rule configuration with id $id not found",
      )
  }

  @Test
  fun `it should fail when source configuration is a draft`() {
    val source = RuleConfiguration.createDraft(
      ruleDefinition = BatteryLevelRuleV1.ruleDefinition,
      revision = RuleConfigurationRevision(2),
      parameters = BatteryLevelRuleParameters(
        threshold = BatteryPercentage(20),
      ),
      createdAt = Instant.parse("2026-09-21T10:00:00Z"),
      createdBy = "test-user",
    )

    val useCase = CreateRuleConfigurationDraft(
      store = FakeRuleConfigurationStore(
        listOf(source),
      ),
      ruleParameterParser = parser,
      clock = clock,
    )

    assertThatThrownBy {
      useCase.create(
        sourceId = source.id.value,
        request = RuleConfigurationRequest(
          parameters = mapOf(
            "threshold" to 50,
          ),
        ),
        user = "test-user",
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage(
        "A draft can only be created from a published rule configuration",
      )
  }

  @Test
  fun `it should fail when a draft already exists for the rule`() {
    val source = givenPublishedBatteryLevelConfiguration(
      revision = 1,
    )

    val existingDraft = RuleConfiguration.createDraft(
      ruleDefinition = BatteryLevelRuleV1.ruleDefinition,
      revision = RuleConfigurationRevision(2),
      parameters = BatteryLevelRuleParameters(
        threshold = BatteryPercentage(30),
      ),
      createdAt = Instant.parse("2026-09-21T10:00:00Z"),
      createdBy = "another-user",
    )

    val useCase = CreateRuleConfigurationDraft(
      store = FakeRuleConfigurationStore(
        listOf(
          source,
          existingDraft,
        ),
      ),
      ruleParameterParser = parser,
      clock = clock,
    )

    assertThatThrownBy {
      useCase.create(
        sourceId = source.id.value,
        request = RuleConfigurationRequest(
          parameters = mapOf(
            "threshold" to 50,
          ),
        ),
        user = "test-user",
      )
    }
      .isInstanceOf(EntityExistsException::class.java)
      .hasMessage(
        "A draft already exists for BATTERY_LEVEL v1",
      )
  }

  @Test
  fun `it should fail when parameters are invalid`() {
    val source =
      givenPublishedBatteryLevelConfiguration()

    val useCase = CreateRuleConfigurationDraft(
      store = FakeRuleConfigurationStore(
        listOf(source),
      ),
      ruleParameterParser = parser,
      clock = clock,
    )

    assertThatThrownBy {
      useCase.create(
        sourceId = source.id.value,
        request = RuleConfigurationRequest(
          parameters = emptyMap(),
        ),
        user = "test-user",
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage(
        "Missing or invalid threshold parameter",
      )
  }
}
