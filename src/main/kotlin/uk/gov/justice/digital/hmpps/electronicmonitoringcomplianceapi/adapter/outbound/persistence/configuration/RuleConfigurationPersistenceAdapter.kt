package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.configuration

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfiguration
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleDefinition
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleParameters
import java.util.UUID

@Component
class RuleConfigurationPersistenceAdapter(
  private val repository: RuleConfigurationRepository,
  private val mapper: RuleConfigurationMapper,
) : RuleConfigurationStore {

  override fun findPublished(): List<RuleConfiguration<out RuleParameters>> = repository
    .findAllByStatus(RuleConfigurationStatus.PUBLISHED)
    .map(mapper::toDomain)

  @Suppress("UNCHECKED_CAST")
  override fun <P : RuleParameters> findPublished(
    definition: RuleDefinition<P>,
  ): RuleConfiguration<P>? = repository
    .findByRuleIdAndRuleVersionAndStatus(
      definition.id.value,
      definition.version.value,
      RuleConfigurationStatus.PUBLISHED,
    )
    ?.let(mapper::toDomain)
    as RuleConfiguration<P>?

  override fun findById(id: UUID): RuleConfiguration<out RuleParameters>? = repository
    .findById(id)
    .map(mapper::toDomain)
    .orElse(null)
}
