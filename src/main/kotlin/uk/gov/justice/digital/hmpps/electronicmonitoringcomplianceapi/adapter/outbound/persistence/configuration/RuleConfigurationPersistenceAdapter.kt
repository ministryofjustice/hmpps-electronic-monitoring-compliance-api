package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.configuration

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfiguration
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleParameters

@Component
class RuleConfigurationPersistenceAdapter(
  private val repository: RuleConfigurationRepository,
  private val mapper: RuleConfigurationMapper,
) : RuleConfigurationStore {

  override fun findPublished(): List<RuleConfiguration<out RuleParameters>> = repository
    .findAllByStatus(RuleConfigurationStatus.PUBLISHED)
    .map(mapper::toDomain)
}
