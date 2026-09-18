package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.evaluation.RuleEvaluation
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleDefinition
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import java.util.UUID

class DeviceCompliance private constructor(
  val id: UUID,
  val deviceId: DeviceId,
  status: DeviceStatus,
  ruleCompliance: List<DeviceRuleCompliance>,
) {
  var status: DeviceStatus = status
    private set

  val state: ComplianceState?
    get() = if (status == DeviceStatus.ACTIVATED) {
      calculateState()
    } else {
      null
    }

  private val _ruleCompliance = ruleCompliance.toMutableList()

  val ruleCompliance: List<DeviceRuleCompliance>
    get() = this._ruleCompliance.toList()

  private fun calculateState(): ComplianceState = if (_ruleCompliance.all {
      it.state == ComplianceState.COMPLIANT
    }
  ) {
    ComplianceState.COMPLIANT
  } else {
    ComplianceState.NON_COMPLIANT
  }

  fun apply(evaluation: RuleEvaluation): List<DeviceRuleComplianceEvent> {
    require(status == DeviceStatus.ACTIVATED) {
      "Cannot evaluate compliance for a deactivated device"
    }

    val ruleCompliance = _ruleCompliance.single {
      it.ruleDefinition == evaluation.ruleDefinition
    }

    return ruleCompliance.apply(evaluation)
  }

  companion object {
    fun create(
      deviceId: DeviceId,
      status: DeviceStatus,
      ruleDefinitions: List<RuleDefinition<*>>,
    ): DeviceCompliance = DeviceCompliance(
      id = UUID.randomUUID(),
      deviceId = deviceId,
      status = status,
      ruleCompliance = ruleDefinitions.map { ruleDefinition ->
        DeviceRuleCompliance.create(
          deviceId = deviceId,
          ruleDefinition = ruleDefinition,
        )
      },
    )

    internal fun rehydrate(
      id: UUID,
      deviceId: DeviceId,
      status: DeviceStatus,
      ruleCompliance: List<DeviceRuleCompliance>,
    ): DeviceCompliance = DeviceCompliance(
      id = id,
      deviceId = deviceId,
      status = status,
      ruleCompliance = ruleCompliance,
    )
  }
}
