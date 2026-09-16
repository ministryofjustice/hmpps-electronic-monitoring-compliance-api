package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.ComplianceState
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceRuleCompliance
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceRuleComplianceEvent
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationRevision
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.evaluation.RuleEvaluation
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.evaluation.RuleEvaluationResult
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryAtOrBelowThreshold
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryPercentage
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.EventId
import java.time.Instant
import java.util.UUID

class DeviceRuleComplianceTest {
  private val deviceId = DeviceId(1)

  @Test
  fun `it moves from compliant to non compliant when evaluation is non compliant`() {
    // Given a device that is compliant
    val ruleCompliance = givenNewRuleCompliance(givenCompliantRuleEvaluation())
    // And a later non-compliant evaluation
    val ruleEvaluation = givenNonCompliantRuleEvaluation(
      recordedAt = Instant.parse("2026-01-01T00:05:00.00Z"),
    )

    // When the evaluation is applied
    val events = ruleCompliance.apply(
      evaluation = ruleEvaluation,
    )

    // Then the state should be non-compliant
    Assertions.assertThat(ruleCompliance.state).isEqualTo(ComplianceState.NON_COMPLIANT)

    // And a state changed event was emitted
    Assertions.assertThat(events).containsExactly(
      DeviceRuleComplianceEvent.StateChanged(
        deviceId = deviceId,
        ruleId = BatteryLevelRuleV1.ruleDefinition.id,
        from = ComplianceState.COMPLIANT,
        to = ComplianceState.NON_COMPLIANT,
        occurredAt = Instant.parse("2026-01-01T00:05:00.00Z"),
        evaluation = ruleEvaluation,
      ),
    )
  }

  @Test
  fun `it remains non compliant when evaluation is non compliant`() {
    // Given a device that is non-compliant
    val ruleCompliance = givenNewRuleCompliance(givenNonCompliantRuleEvaluation())
    // And a later non-compliant evaluation
    val ruleEvaluation = givenNonCompliantRuleEvaluation(
      recordedAt = Instant.parse("2026-01-01T00:05:00.00Z"),
    )

    // When the evaluation is applied
    val events = ruleCompliance.apply(
      ruleEvaluation,
    )

    // Then the state should remain non-compliant
    Assertions.assertThat(ruleCompliance.state).isEqualTo(ComplianceState.NON_COMPLIANT)

    // And a state changed event should not be emitted
    Assertions.assertThat(events).isEmpty()
  }

  @Test
  fun `it moves from non-compliant to compliant when evaluation is compliant`() {
    // Given a device that is non-compliant
    val ruleCompliance = givenNewRuleCompliance(givenNonCompliantRuleEvaluation())
    // And a later compliant evaluation
    val ruleEvaluation = givenCompliantRuleEvaluation(
      recordedAt = Instant.parse("2026-01-01T00:05:00.00Z"),
    )

    // When the evaluation is applied
    val events = ruleCompliance.apply(
      ruleEvaluation,
    )

    // Then the state should be compliant
    Assertions.assertThat(ruleCompliance.state).isEqualTo(ComplianceState.COMPLIANT)

    // And a state changed event should be emitted
    Assertions.assertThat(events).containsExactly(
      DeviceRuleComplianceEvent.StateChanged(
        deviceId = deviceId,
        ruleId = BatteryLevelRuleV1.ruleDefinition.id,
        from = ComplianceState.NON_COMPLIANT,
        to = ComplianceState.COMPLIANT,
        occurredAt = Instant.parse("2026-01-01T00:05:00.00Z"),
        evaluation = ruleEvaluation,
      ),
    )
  }

  private fun givenNewRuleCompliance(evaluation: RuleEvaluation) = DeviceRuleCompliance.from(
    evaluation = evaluation,
  )

  private fun givenRuleEvaluation(
    result: RuleEvaluationResult,
    recordedAt: Instant,
  ) = RuleEvaluation(
    deviceId = deviceId,
    eventId = EventId(1),
    recordedAt = recordedAt,
    ruleDefinition = BatteryLevelRuleV1.ruleDefinition,
    configurationId = RuleConfigurationId(UUID.randomUUID()),
    configurationRevision = RuleConfigurationRevision(1),
    result = result,
  )

  private fun givenNonCompliantRuleEvaluationResult() = RuleEvaluationResult.NonCompliant(
    BatteryAtOrBelowThreshold(
      actual = BatteryPercentage(10),
      threshold = BatteryPercentage(20),
    ),
  )

  private fun givenCompliantRuleEvaluation(
    recordedAt: Instant = Instant.parse("2026-01-01T00:00:00.00Z"),
  ) = givenRuleEvaluation(
    result = RuleEvaluationResult.Compliant,
    recordedAt = recordedAt,
  )

  private fun givenNonCompliantRuleEvaluation(
    recordedAt: Instant = Instant.parse("2026-01-01T00:00:00.00Z"),
  ) = givenRuleEvaluation(
    result = givenNonCompliantRuleEvaluationResult(),
    recordedAt = recordedAt,
  )
}
