package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfiguration
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleDefinition
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleParameters
import java.util.UUID

class FakeRuleConfigurationStore(
  configurations: List<RuleConfiguration<out RuleParameters>> =
    emptyList(),
) : RuleConfigurationStore {

  private val configurations =
    configurations
      .associateBy { it.id.value }
      .toMutableMap()

  override fun findPublished(): List<RuleConfiguration<out RuleParameters>> = configurations.values.filter {
    it.status == RuleConfigurationStatus.PUBLISHED
  }

  @Suppress("UNCHECKED_CAST")
  override fun <P : RuleParameters> findPublished(
    definition: RuleDefinition<P>,
  ): RuleConfiguration<P>? = configurations.values
    .firstOrNull {
      it.status == RuleConfigurationStatus.PUBLISHED &&
        it.ruleDefinition == definition
    } as RuleConfiguration<P>?

  override fun findById(
    id: UUID,
  ): RuleConfiguration<out RuleParameters>? = configurations[id]

  fun save(
    configuration: RuleConfiguration<out RuleParameters>,
  ) {
    configurations[configuration.id.value] =
      configuration
  }
}
