package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.configuration

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleParameters
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryPercentage
import java.time.Instant
import java.util.UUID

class RuleConfigurationMapperTest {
  private val mapper = RuleConfigurationMapper()

  @Test
  fun `it should map a battery level rule configuration entity to domain`() {
    // Given a battery level rule configuration entity
    val id = UUID.randomUUID()
    val entity = RuleConfigurationEntity(
      id = id,
      ruleId = BatteryLevelRuleV1.ruleDefinition.id.value,
      ruleVersion = BatteryLevelRuleV1.ruleDefinition.version.value,
      revision = 1,
      status = RuleConfigurationStatus.PUBLISHED,
      parameters = mapOf(
        "threshold" to 20,
      ),
      createdAt = Instant.parse("2026-01-01T10:00:00Z"),
      createdBy = "system",
      publishedAt = Instant.parse("2026-01-01T10:00:00Z"),
      publishedBy = "system",
      effectiveFrom = Instant.parse("2026-01-01T10:00:00Z"),
    )

    // When it is mapped to domain
    val configuration = mapper.toDomain(entity)

    // Then the domain object should be a RuleConfiguration with battery level rule parameters
    assertThat(configuration.id.value).isEqualTo(id)
    assertThat(configuration.ruleDefinition).isEqualTo(BatteryLevelRuleV1.ruleDefinition)
    assertThat(configuration.revision.value).isEqualTo(1)
    assertThat(configuration.status).isEqualTo(RuleConfigurationStatus.PUBLISHED)
    assertThat(configuration.parameters).isEqualTo(
      BatteryLevelRuleParameters(
        threshold = BatteryPercentage(20),
      ),
    )
    assertThat(configuration.createdAt)
      .isEqualTo(Instant.parse("2026-01-01T10:00:00Z"))
    assertThat(configuration.createdBy).isEqualTo("system")
    assertThat(configuration.publishedAt)
      .isEqualTo(Instant.parse("2026-01-01T10:00:00Z"))
    assertThat(configuration.publishedBy).isEqualTo("system")
    assertThat(configuration.effectiveFrom)
      .isEqualTo(Instant.parse("2026-01-01T10:00:00Z"))
  }

  @Test
  fun `it should reject an unknown rule definition`() {
    // Given a rule configuration with id UNKNOWN
    val entity = RuleConfigurationEntity(
      id = UUID.randomUUID(),
      ruleId = "UNKNOWN",
      ruleVersion = 1,
      revision = 1,
      status = RuleConfigurationStatus.PUBLISHED,
      parameters = mapOf(
        "threshold" to 20,
      ),
      createdAt = Instant.parse("2026-01-01T10:00:00Z"),
      createdBy = "system",
      publishedAt = Instant.parse("2026-01-01T10:00:00Z"),
      publishedBy = "system",
      effectiveFrom = Instant.parse("2026-01-01T10:00:00Z"),
    )

    // When it is mapped to domain, then it should throw an exception
    assertThatThrownBy {
      mapper.toDomain(entity)
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Unknown rule definition: UNKNOWN v1")
  }

  @Test
  fun `it should reject an unknown version of a known rule`() {
    // Given a battery level rule configuration with an unknown version
    val entity = RuleConfigurationEntity(
      id = UUID.randomUUID(),
      ruleId = BatteryLevelRuleV1.ruleDefinition.id.value,
      ruleVersion = 999,
      revision = 1,
      status = RuleConfigurationStatus.PUBLISHED,
      parameters = mapOf(
        "threshold" to 20,
      ),
      createdAt = Instant.parse("2026-01-01T10:00:00Z"),
      createdBy = "system",
      publishedAt = Instant.parse("2026-01-01T10:00:00Z"),
      publishedBy = "system",
      effectiveFrom = Instant.parse("2026-01-01T10:00:00Z"),
    )

    // When it is mapped to domain, then it should throw an exception
    assertThatThrownBy {
      mapper.toDomain(entity)
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Unknown rule definition: BATTERY_LEVEL v999")
  }
}
