package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfiguration
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleDefinition
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleParameters
import java.time.Instant
import java.util.UUID

class FakeRuleConfigurationStore(
  configurations: List<RuleConfiguration<out RuleParameters>> =
    emptyList(),
) : RuleConfigurationStore {

  private val configurations =
    configurations
      .associateBy { it.id.value }
      .toMutableMap()

  override fun findCurrentPublished(): List<RuleConfiguration<out RuleParameters>> = configurations.values
    .filter {
      it.status == RuleConfigurationStatus.PUBLISHED &&
        it.effectiveFrom != null
    }
    .groupBy {
      it.ruleDefinition.id.value to it.ruleDefinition.version.value
    }
    .mapNotNull { (_, revisions) ->
      revisions.maxWithOrNull(
        compareBy<RuleConfiguration<out RuleParameters>> {
          it.effectiveFrom
        }.thenBy {
          it.revision.value
        },
      )
    }

  @Suppress("UNCHECKED_CAST")
  override fun <P : RuleParameters> findPublishedAt(
    definition: RuleDefinition<P>,
    at: Instant,
  ): RuleConfiguration<P>? = configurations.values
    .filter {
      it.status == RuleConfigurationStatus.PUBLISHED &&
        it.ruleDefinition == definition &&
        it.effectiveFrom != null &&
        !it.effectiveFrom!!.isAfter(at)
    }
    .maxWithOrNull(
      compareBy<RuleConfiguration<out RuleParameters>> {
        it.effectiveFrom
      }.thenBy {
        it.revision.value
      },
    ) as RuleConfiguration<P>?

  @Suppress("UNCHECKED_CAST")
  override fun <P : RuleParameters> findDraft(
    definition: RuleDefinition<P>,
  ): RuleConfiguration<P>? = configurations.values
    .firstOrNull {
      it.status == RuleConfigurationStatus.DRAFT &&
        it.ruleDefinition == definition
    } as RuleConfiguration<P>?

  override fun findById(
    id: UUID,
  ): RuleConfiguration<out RuleParameters>? = configurations[id]

  override fun save(
    configuration: RuleConfiguration<out RuleParameters>,
  ): RuleConfiguration<out RuleParameters> {
    configurations[configuration.id.value] =
      configuration
    return configuration
  }
}
