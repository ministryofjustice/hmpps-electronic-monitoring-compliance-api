package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.evaluation.RuleEvaluation
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import java.time.Instant

sealed interface DeviceRuleComplianceEvent {
  data class StateChanged(
    val deviceId: DeviceId,
    val ruleId: RuleId,
    val from: ComplianceState,
    val to: ComplianceState,
    val occurredAt: Instant,
    val evaluation: RuleEvaluation,
  ) : DeviceRuleComplianceEvent
}
