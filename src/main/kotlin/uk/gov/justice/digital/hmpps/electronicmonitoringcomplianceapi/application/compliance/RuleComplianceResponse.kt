package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.compliance

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.ComplianceState
import java.time.Instant

data class RuleComplianceResponse(
  val ruleId: String,
  val ruleVersion: Int,
  val state: ComplianceState,
  val stateChangedAt: Instant?,
)
