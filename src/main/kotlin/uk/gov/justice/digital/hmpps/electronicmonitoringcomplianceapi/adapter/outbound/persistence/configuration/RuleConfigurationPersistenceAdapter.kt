package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.configuration

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfiguration
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleDefinition
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleParameters
import java.time.Instant
import java.util.UUID

@Component
class RuleConfigurationPersistenceAdapter(
  private val repository: RuleConfigurationRepository,
  private val mapper: RuleConfigurationMapper,
) : RuleConfigurationStore {

  override fun findCurrentPublished(): List<RuleConfiguration<out RuleParameters>> = repository
    .findCurrentPublished()
    .map(mapper::toDomain)

  @Suppress("UNCHECKED_CAST")
  override fun <P : RuleParameters> findPublishedAt(definition: RuleDefinition<P>, at: Instant): RuleConfiguration<P>? = repository
    .findPublishedAt(
      definition.id.value,
      definition.version.value,
      at,
    )
    ?.let(mapper::toDomain)
    as RuleConfiguration<P>?

  @Suppress("UNCHECKED_CAST")
  override fun <P : RuleParameters> findDraft(
    definition: RuleDefinition<P>,
  ): RuleConfiguration<P>? = repository
    .findByRuleIdAndRuleVersionAndStatus(
      definition.id.value,
      definition.version.value,
      RuleConfigurationStatus.DRAFT,
    )
    ?.let(mapper::toDomain)
    as RuleConfiguration<P>?

  override fun findById(id: UUID): RuleConfiguration<out RuleParameters>? = repository
    .findById(id)
    .map(mapper::toDomain)
    .orElse(null)

  override fun save(
    configuration: RuleConfiguration<out RuleParameters>,
  ): RuleConfiguration<out RuleParameters> = mapper.toDomain(
    repository.save(
      mapper.toEntity(configuration),
    ),
  )
}
