package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleParameters

interface RuleConfigurationStore {
  fun findPublished(): List<RuleConfiguration<out RuleParameters>>
}
