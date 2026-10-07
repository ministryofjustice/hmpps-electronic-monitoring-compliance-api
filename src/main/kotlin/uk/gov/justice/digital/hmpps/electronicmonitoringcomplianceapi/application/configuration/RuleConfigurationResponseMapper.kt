package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceRuleComplianceCounts
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfiguration
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleParameters

fun RuleConfiguration<out RuleParameters>.toResponse(
  summary: DeviceRuleComplianceCounts? = null,
): RuleConfigurationResponse = RuleConfigurationResponse(
  id = id.value,
  ruleId = ruleDefinition.id.value,
  ruleVersion = ruleDefinition.version.value,
  revision = revision.value,
  parameters = parameters,
  status = status,
  summary = summary,
)
