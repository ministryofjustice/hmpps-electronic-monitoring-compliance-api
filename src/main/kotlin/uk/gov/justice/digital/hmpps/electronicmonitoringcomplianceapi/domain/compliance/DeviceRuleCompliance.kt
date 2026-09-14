package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.evaluation.RuleEvaluation
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.evaluation.RuleEvaluationResult
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import java.time.Instant

// Represents the current state of whether a device is compliant with a rule
class DeviceRuleCompliance private constructor(
  val deviceId: DeviceId,
  val ruleId: RuleId,
  state: ComplianceState,
  stateChangedAt: Instant?,
) {

  var state: ComplianceState = state
    private set

  var stateChangedAt: Instant? = stateChangedAt
    private set

  fun apply(
    evaluation: RuleEvaluation,
  ): List<DeviceRuleComplianceEvent> {
    require(evaluation.deviceId == deviceId) {
      "Evaluation belongs to a different device"
    }

    require(evaluation.ruleId == ruleId) {
      "Evaluation belongs to a different rule"
    }

    return when (evaluation.result) {
      RuleEvaluationResult.Compliant -> applyCompliantEvaluation(evaluation)
      is RuleEvaluationResult.NonCompliant -> applyNonCompliantEvaluation(evaluation)
    }
  }

  private fun applyCompliantEvaluation(
    evaluation: RuleEvaluation,
  ): List<DeviceRuleComplianceEvent> = when (state) {
    ComplianceState.COMPLIANT -> emptyList()

    ComplianceState.NON_COMPLIANT,
    -> {
      val previousState = state

      state = ComplianceState.COMPLIANT
      stateChangedAt = evaluation.recordedAt

      listOf(
        DeviceRuleComplianceEvent.StateChanged(
          deviceId = deviceId,
          ruleId = ruleId,
          from = previousState,
          to = ComplianceState.COMPLIANT,
          occurredAt = evaluation.recordedAt,
          evaluation = evaluation,
        ),
      )
    }
  }

  private fun applyNonCompliantEvaluation(
    evaluation: RuleEvaluation,
  ): List<DeviceRuleComplianceEvent> = when (state) {
    ComplianceState.NON_COMPLIANT -> emptyList()

    ComplianceState.COMPLIANT,
    -> {
      val previousState = state

      state = ComplianceState.NON_COMPLIANT
      stateChangedAt = evaluation.recordedAt

      listOf(
        DeviceRuleComplianceEvent.StateChanged(
          deviceId = deviceId,
          ruleId = ruleId,
          from = previousState,
          to = ComplianceState.NON_COMPLIANT,
          occurredAt = evaluation.recordedAt,
          evaluation = evaluation,
        ),
      )
    }
  }

  companion object {
    fun create(
      deviceId: DeviceId,
      ruleId: RuleId,
    ): DeviceRuleCompliance = DeviceRuleCompliance(
      deviceId = deviceId,
      ruleId = ruleId,
      state = ComplianceState.COMPLIANT,
      stateChangedAt = null,
    )
  }
}
