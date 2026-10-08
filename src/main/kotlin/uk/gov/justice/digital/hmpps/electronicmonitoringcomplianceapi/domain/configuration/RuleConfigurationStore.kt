package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleDefinition
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleParameters
import java.time.Instant
import java.util.UUID

interface RuleConfigurationStore {
  fun findCurrentPublished(): List<RuleConfiguration<out RuleParameters>>

  fun <P : RuleParameters> findPublishedAt(
    definition: RuleDefinition<P>,
    at: Instant,
  ): RuleConfiguration<P>?

  fun <P : RuleParameters> findDraft(
    definition: RuleDefinition<P>,
  ): RuleConfiguration<P>?

  fun findById(
    id: UUID,
  ): RuleConfiguration<out RuleParameters>?

  fun save(
    configuration: RuleConfiguration<out RuleParameters>,
  ): RuleConfiguration<out RuleParameters>
}
