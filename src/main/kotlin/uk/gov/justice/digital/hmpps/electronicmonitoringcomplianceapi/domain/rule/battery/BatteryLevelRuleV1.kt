package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfiguration
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.evaluation.RuleEvaluation
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.evaluation.RuleEvaluationResult
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.Rule
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleDefinition
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleVersion
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.events.BatteryLevelReported

class BatteryLevelRuleV1 : Rule<BatteryLevelReported, BatteryLevelRuleParameters> {
  override val definition = ruleDefinition

  override fun evaluate(
    event: BatteryLevelReported,
    configuration: RuleConfiguration<BatteryLevelRuleParameters>,
  ): RuleEvaluation {
    require(configuration.ruleDefinition == definition) {
      "Configuration rule definition does not match rule definition"
    }

    val result = if (event.batteryPercentage.value <= configuration.parameters.threshold.value) {
      RuleEvaluationResult.NonCompliant(
        reason = BatteryAtOrBelowThreshold(
          actual = event.batteryPercentage,
          threshold = configuration.parameters.threshold,
        ),
      )
    } else {
      RuleEvaluationResult.Compliant
    }

    return RuleEvaluation(
      deviceId = event.deviceId,
      eventId = event.eventId,
      recordedAt = event.recordedAt,
      ruleDefinition = definition,
      configurationId = configuration.id,
      configurationRevision = configuration.revision,
      result = result,
    )
  }

  companion object {
    val ruleDefinition = RuleDefinition<BatteryLevelRuleParameters>(
      id = BatteryLevelRule.id,
      version = RuleVersion(1),
    )
  }
}
