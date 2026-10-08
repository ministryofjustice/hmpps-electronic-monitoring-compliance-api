package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfiguration
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationRevision
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleParameters
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryPercentage
import java.time.Instant
import java.util.UUID

object RuleConfigurationFixtures {
  fun givenDraftBatteryLevelConfiguration(
    threshold: Int = 20,
  ): RuleConfiguration<BatteryLevelRuleParameters> = RuleConfiguration.createDraft(
    ruleDefinition = BatteryLevelRuleV1.ruleDefinition,
    revision = RuleConfigurationRevision(2),
    parameters = BatteryLevelRuleParameters(
      threshold = BatteryPercentage(threshold),
    ),
    createdAt = Instant.parse("2026-10-08T09:00:00Z"),
    createdBy = "test-user",
  )

  fun givenPublishedBatteryLevelConfiguration(
    threshold: Int = 20,
    revision: Int = 1,
    id: UUID = UUID.randomUUID(),
    effectiveFrom: Instant = Instant.parse("2026-01-01T00:00:00Z"),
  ): RuleConfiguration<BatteryLevelRuleParameters> = RuleConfiguration.rehydrate(
    id = RuleConfigurationId(id),
    ruleDefinition = BatteryLevelRuleV1.ruleDefinition,
    revision = RuleConfigurationRevision(revision),
    parameters = BatteryLevelRuleParameters(
      threshold = BatteryPercentage(threshold),
    ),
    status = RuleConfigurationStatus.PUBLISHED,
    createdAt = Instant.parse("2026-01-01T00:00:00Z"),
    createdBy = "system",
    publishedAt = Instant.parse("2026-01-01T00:00:00Z"),
    publishedBy = "system",
    effectiveFrom = effectiveFrom,
  )
}
