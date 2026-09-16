package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration

data class RuleConfigurationSummary(
  val ruleId: String,
  val ruleVersion: Int,
  val revision: Int,
  val parameters: RuleConfigurationParametersSummary,
)
