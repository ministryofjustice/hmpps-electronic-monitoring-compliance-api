package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.evaluation.RuleEvaluation
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.evaluation.RuleEvaluationResult
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleDefinition
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import java.time.Instant
import java.util.UUID

// Represents the current state of whether a device is compliant with a rule
class DeviceRuleCompliance private constructor(
  val id: UUID,
  val deviceId: DeviceId,
  val ruleDefinition: RuleDefinition<*>,
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

    require(evaluation.ruleDefinition == ruleDefinition) {
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
    ComplianceState.NO_DATA,
    -> {
      val previousState = state

      state = ComplianceState.COMPLIANT
      stateChangedAt = evaluation.recordedAt

      listOf(
        DeviceRuleComplianceEvent.StateChanged(
          deviceId = deviceId,
          ruleId = ruleDefinition.id,
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
    ComplianceState.NO_DATA,
    -> {
      val previousState = state

      state = ComplianceState.NON_COMPLIANT
      stateChangedAt = evaluation.recordedAt

      listOf(
        DeviceRuleComplianceEvent.StateChanged(
          deviceId = deviceId,
          ruleId = ruleDefinition.id,
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
      ruleDefinition: RuleDefinition<*>,
    ): DeviceRuleCompliance = DeviceRuleCompliance(
      id = UUID.randomUUID(),
      deviceId = deviceId,
      ruleDefinition = ruleDefinition,
      state = ComplianceState.NO_DATA,
      stateChangedAt = null,
    )

    internal fun rehydrate(
      id: UUID,
      deviceId: DeviceId,
      ruleDefinition: RuleDefinition<*>,
      state: ComplianceState,
      stateChangedAt: Instant?,
    ): DeviceRuleCompliance = DeviceRuleCompliance(
      id = id,
      deviceId = deviceId,
      ruleDefinition = ruleDefinition,
      state = state,
      stateChangedAt = stateChangedAt,
    )
  }
}
