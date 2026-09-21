package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration

import java.util.UUID

data class RuleConfigurationSummary(
  val id: UUID,
  val ruleId: String,
  val ruleVersion: Int,
  val revision: Int,
  val parameters: RuleConfigurationParametersSummary,
)
