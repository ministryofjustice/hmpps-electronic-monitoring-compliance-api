package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.BatteryLevelRuleConfiguration
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.evaluation.RuleEvaluation
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.evaluation.RuleEvaluationResult
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleVersion
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.events.BatteryLevelReported

class BatteryLevelRuleV1 {
  fun evaluate(
    event: BatteryLevelReported,
    configuration: BatteryLevelRuleConfiguration,
  ): RuleEvaluation {
    require(configuration.ruleId == BatteryLevelRule.id)
    require(configuration.ruleVersion == version)

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
      ruleId = configuration.ruleId,
      ruleVersion = configuration.ruleVersion,
      configurationId = configuration.id,
      configurationRevision = configuration.revision,
      result = result,
    )
  }

  companion object {
    val version = RuleVersion(1)
  }
}
