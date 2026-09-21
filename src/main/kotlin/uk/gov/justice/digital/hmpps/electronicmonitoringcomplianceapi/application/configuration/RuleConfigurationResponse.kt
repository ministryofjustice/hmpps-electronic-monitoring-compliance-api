package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceRuleComplianceCounts
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleParameters
import java.util.UUID

data class RuleConfigurationResponse(
  val id: UUID,
  val ruleId: String,
  val ruleVersion: Int,
  val revision: Int,
  val parameters: RuleParameters,
  val status: RuleConfigurationStatus,
  val summary: DeviceRuleComplianceCounts,
)
