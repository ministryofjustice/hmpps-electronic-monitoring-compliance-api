package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleDefinition
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleParameters
import java.util.UUID

interface RuleConfigurationStore {
  fun findPublished(): List<RuleConfiguration<out RuleParameters>>

  fun <P : RuleParameters> findPublished(
    definition: RuleDefinition<P>,
  ): RuleConfiguration<P>?

  fun findById(
    id: UUID,
  ): RuleConfiguration<out RuleParameters>?
}
