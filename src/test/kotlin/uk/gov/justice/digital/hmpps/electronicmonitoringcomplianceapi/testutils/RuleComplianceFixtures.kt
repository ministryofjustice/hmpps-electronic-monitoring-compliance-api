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

object RuleComplianceFixtures {
  fun givenPublishedBatteryLevelConfiguration(
    threshold: Int = 20,
    revision: Int = 1,
    id: UUID = UUID.randomUUID(),
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
    effectiveFrom = Instant.parse("2026-01-01T00:00:00Z"),
  )
}
