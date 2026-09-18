package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
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

class DeviceComplianceTest {
  private val deviceId = DeviceId(1)

  @Test
  fun `it should initialise rule compliance with no data`() {
    val compliance = givenDeviceCompliance()
    val ruleCompliance = compliance.ruleCompliance.single()

    assertThat(ruleCompliance.ruleDefinition)
      .isEqualTo(BatteryLevelRuleV1.ruleDefinition)
    assertThat(ruleCompliance.state)
      .isEqualTo(ComplianceState.NO_DATA)
  }

  @Test
  fun `it should indicate an activated device is non compliant when a rule has no data`() {
    val compliance = givenDeviceCompliance()

    assertThat(compliance.state)
      .isEqualTo(ComplianceState.NON_COMPLIANT)
  }

  @Test
  fun `it should indicate an activated device is compliant when all rules are compliant`() {
    val compliance = givenDeviceCompliance()

    compliance.apply(
      givenCompliantRuleEvaluation(),
    )

    assertThat(compliance.state)
      .isEqualTo(ComplianceState.COMPLIANT)
  }

  @Test
  fun `it should indicate an activated device is non compliant when a rule is non compliant`() {
    val compliance = givenDeviceCompliance()

    compliance.apply(
      givenNonCompliantRuleEvaluation(),
    )

    assertThat(compliance.state)
      .isEqualTo(ComplianceState.NON_COMPLIANT)
  }

  @Test
  fun `it should indicate a deactivated device does not have a compliance state`() {
    val compliance = givenDeviceCompliance(
      status = DeviceStatus.DEACTIVATED,
    )

    assertThat(compliance.state).isNull()
  }

  @Test
  fun `it should apply an evaluation to the matching rule`() {
    val compliance = givenDeviceCompliance()
    val evaluation = givenNonCompliantRuleEvaluation()

    compliance.apply(evaluation)

    assertThat(compliance.ruleCompliance)
      .singleElement()
      .extracting(DeviceRuleCompliance::state)
      .isEqualTo(ComplianceState.NON_COMPLIANT)
  }

  private fun givenDeviceCompliance(
    status: DeviceStatus = DeviceStatus.ACTIVATED,
  ) = DeviceCompliance.create(
    deviceId = deviceId,
    status = status,
    ruleDefinitions = listOf(
      BatteryLevelRuleV1.ruleDefinition,
    ),
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
