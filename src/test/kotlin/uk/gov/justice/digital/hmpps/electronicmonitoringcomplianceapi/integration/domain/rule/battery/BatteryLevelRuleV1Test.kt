package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.domain.rule.battery

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.BatteryLevelRuleConfiguration
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationRevision
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.evaluation.RuleEvaluationResult
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryAtOrBelowThreshold
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleParameters
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryPercentage
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.EventId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.events.BatteryLevelReported
import java.time.Instant

class BatteryLevelRuleV1Test {
  private val rule = BatteryLevelRuleV1()

  @Test
  fun `it should evaluate a battery level above the threshold as compliant`() {
    // Given a battery level of 21 and threshold of 20
    val event = givenBatteryLevelReportedEvent(21)
    val config = givenConfiguration(20)

    // When the rule is evaluated
    val evaluation = rule.evaluate(
      event,
      config,
    )

    // Then it should evaluate as compliant
    assertThat(evaluation.result).isEqualTo(RuleEvaluationResult.Compliant)
  }

  @Test
  fun `it should evaluate a battery level at the threshold as non compliant`() {
    // Given a battery level of 20 and threshold of 20
    val event = givenBatteryLevelReportedEvent(20)
    val config = givenConfiguration(20)

    // When the rule is evaluated
    val evaluation = rule.evaluate(
      event,
      config,
    )

    // Then it should evaluate as non-compliant
    assertThat(evaluation.result).isEqualTo(
      RuleEvaluationResult.NonCompliant(
        BatteryAtOrBelowThreshold(
          actual = BatteryPercentage(20),
          threshold = BatteryPercentage(20),
        ),
      ),
    )
  }

  @Test
  fun `it should evaluate a battery level below the threshold as non compliant`() {
    // Given a battery level of 19 and threshold of 20
    val event = givenBatteryLevelReportedEvent(19)
    val config = givenConfiguration(20)

    // When the rule is evaluated
    val evaluation = rule.evaluate(
      event,
      config,
    )

    // Then it should evaluate as non-compliant
    assertThat(evaluation.result).isEqualTo(
      RuleEvaluationResult.NonCompliant(
        BatteryAtOrBelowThreshold(
          actual = BatteryPercentage(19),
          threshold = BatteryPercentage(20),
        ),
      ),
    )
  }

  private fun givenBatteryLevelReportedEvent(
    level: Int,
  ) = BatteryLevelReported(
    eventId = EventId(1),
    deviceId = DeviceId(1),
    recordedAt = Instant.parse("2026-01-01T00:00:00Z"),
    batteryPercentage = BatteryPercentage(level),
  )

  private fun givenConfiguration(
    threshold: Int,
  ) = BatteryLevelRuleConfiguration.createDraft(
    revision = RuleConfigurationRevision(1),
    parameters = BatteryLevelRuleParameters(
      threshold = BatteryPercentage(threshold),
    ),
    createdAt = Instant.parse("2026-01-01T00:00:00Z"),
    createdBy = "user",
  )
}
