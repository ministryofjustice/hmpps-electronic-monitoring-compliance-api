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
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleVersion
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryAtOrBelowThreshold
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryPercentage
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.EventId
import java.time.Instant
import java.util.UUID

class DeviceRuleComplianceTest {
  private val deviceId = DeviceId(1)

  @Test
  fun `it should start in the compliant state`() {
    // Given a new rule compliance
    val ruleCompliance = givenNewRuleCompliance()

    // Then the state should be compliant
    Assertions.assertThat(ruleCompliance.state).isEqualTo(ComplianceState.COMPLIANT)
  }

  @Test
  fun `it moves from compliant to non compliant when evaluation is non compliant`() {
    // Given a new evaluation and a non-compliant rule evaluation
    val ruleCompliance = givenNewRuleCompliance()
    val ruleEvaluationResult = givenNonCompliantRuleEvaluationResult()
    val ruleEvaluation = givenRuleEvaluation(result = ruleEvaluationResult, recordedAt = Instant.parse("2026-01-01T00:00:00.00Z"))

    // When a non-compliant evaluation is applied
    val events = ruleCompliance.apply(
      evaluation = ruleEvaluation,
    )

    // Then the state should be non-compliant
    Assertions.assertThat(ruleCompliance.state).isEqualTo(ComplianceState.NON_COMPLIANT)

    // And a state changed event was emitted
    Assertions.assertThat(events).containsExactly(
      DeviceRuleComplianceEvent.StateChanged(
        deviceId = deviceId,
        ruleId = RuleId("BATTERY_LEVEL"),
        from = ComplianceState.COMPLIANT,
        to = ComplianceState.NON_COMPLIANT,
        occurredAt = Instant.parse("2026-01-01T00:00:00.00Z"),
        evaluation = ruleEvaluation,
      ),
    )
  }

  @Test
  fun `it remains non compliant when evaluation is non compliant`() {
    // Given a new rule compliance and a non-compliant rule evaluation
    val ruleCompliance = givenNewRuleCompliance()
    val ruleEvaluationResult = givenNonCompliantRuleEvaluationResult()
    val firstRuleEvaluation = givenRuleEvaluation(
      result = ruleEvaluationResult,
      recordedAt = Instant.parse("2026-01-01T00:00:00.00Z"),
    )
    val secondRuleEvaluation = givenRuleEvaluation(
      result = ruleEvaluationResult,
      recordedAt = Instant.parse("2026-01-01T00:05:00.00Z"),
    )

    // And the device is already non-compliant
    ruleCompliance.apply(firstRuleEvaluation)

    // When a second non-compliant evaluation is applied
    val events = ruleCompliance.apply(
      secondRuleEvaluation,
    )

    // Then the state should be non-compliant
    Assertions.assertThat(ruleCompliance.state).isEqualTo(ComplianceState.NON_COMPLIANT)

    // And a state changed event should not be emitted
    Assertions.assertThat(events).isEmpty()
  }

  @Test
  fun `it moves from non-compliant to compliant when evaluation is compliant`() {
    // Given a new rule compliance and a compliant rule evaluation
    val ruleCompliance = givenNewRuleCompliance()
    val ruleEvaluationResult = givenNonCompliantRuleEvaluationResult()
    val firstRuleEvaluation = givenRuleEvaluation(result = ruleEvaluationResult, recordedAt = Instant.parse("2026-01-01T00:00:00.00Z"))
    val secondRuleEvaluation = givenRuleEvaluation(result = RuleEvaluationResult.Compliant, recordedAt = Instant.parse("2026-01-01T00:05:00.00Z"))

    // And the device is already non-compliant
    ruleCompliance.apply(firstRuleEvaluation)

    // When a compliant evaluation is applied
    val events = ruleCompliance.apply(
      secondRuleEvaluation,
    )

    // Then the state should be compliant
    Assertions.assertThat(ruleCompliance.state).isEqualTo(ComplianceState.COMPLIANT)

    // And a state changed event should be emitted
    Assertions.assertThat(events).containsExactly(
      DeviceRuleComplianceEvent.StateChanged(
        deviceId = deviceId,
        ruleId = RuleId("BATTERY_LEVEL"),
        from = ComplianceState.NON_COMPLIANT,
        to = ComplianceState.COMPLIANT,
        occurredAt = Instant.parse("2026-01-01T00:05:00.00Z"),
        evaluation = secondRuleEvaluation,
      ),
    )
  }

  private fun givenNewRuleCompliance() = DeviceRuleCompliance.create(
    deviceId = deviceId,
    ruleId = RuleId("BATTERY_LEVEL"),
  )

  private fun givenRuleEvaluation(
    result: RuleEvaluationResult,
    recordedAt: Instant,
  ) = RuleEvaluation(
    deviceId = deviceId,
    eventId = EventId(1),
    recordedAt = recordedAt,
    ruleId = RuleId("BATTERY_LEVEL"),
    ruleVersion = RuleVersion(1),
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
}
