package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.compliance

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.ComplianceState
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceRuleCompliance
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

class DeviceRuleComplianceMapperTest {

  private val mapper = DeviceRuleComplianceMapper()

  @Test
  fun `it should map a device rule compliance to entity`() {
    val compliance = givenCompliance()

    val entity = mapper.toEntity(compliance)

    assertThat(entity.id).isEqualTo(compliance.id)
    assertThat(entity.deviceId).isEqualTo(123)
    assertThat(entity.ruleId).isEqualTo("BATTERY_LEVEL")
    assertThat(entity.ruleVersion).isEqualTo(1)
    assertThat(entity.state).isEqualTo(ComplianceState.NON_COMPLIANT)
    assertThat(entity.stateChangedAt)
      .isEqualTo(Instant.parse("2026-01-01T10:00:00Z"))
  }

  @Test
  fun `it should map an entity to device rule compliance`() {
    val id = UUID.randomUUID()

    val entity = DeviceRuleComplianceEntity(
      id = id,
      deviceId = 123,
      ruleId = "BATTERY_LEVEL",
      ruleVersion = 1,
      state = ComplianceState.NON_COMPLIANT,
      stateChangedAt = Instant.parse("2026-01-01T10:00:00Z"),
    )

    val compliance = mapper.toDomain(entity)

    assertThat(compliance.id).isEqualTo(id)
    assertThat(compliance.deviceId).isEqualTo(DeviceId(123))
    assertThat(compliance.ruleDefinition)
      .isEqualTo(BatteryLevelRuleV1.ruleDefinition)
    assertThat(compliance.state)
      .isEqualTo(ComplianceState.NON_COMPLIANT)
    assertThat(compliance.stateChangedAt)
      .isEqualTo(Instant.parse("2026-01-01T10:00:00Z"))
  }

  @Test
  fun `rejects unknown rule definition`() {
    val entity = DeviceRuleComplianceEntity(
      id = UUID.randomUUID(),
      deviceId = 123,
      ruleId = "UNKNOWN",
      ruleVersion = 1,
      state = ComplianceState.COMPLIANT,
      stateChangedAt = Instant.parse("2026-01-01T10:00:00Z"),
    )

    assertThatThrownBy {
      mapper.toDomain(entity)
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Unknown rule definition: UNKNOWN v1")
  }

  private fun givenCompliance(): DeviceRuleCompliance = DeviceRuleCompliance.from(
    RuleEvaluation(
      deviceId = DeviceId(123),
      eventId = EventId(1),
      recordedAt = Instant.parse("2026-01-01T10:00:00Z"),
      ruleDefinition = BatteryLevelRuleV1.ruleDefinition,
      configurationId = RuleConfigurationId(UUID.randomUUID()),
      configurationRevision = RuleConfigurationRevision(1),
      result = RuleEvaluationResult.NonCompliant(
        reason = BatteryAtOrBelowThreshold(
          actual = BatteryPercentage(10),
          threshold = BatteryPercentage(20),
        ),
      ),
    ),
  )
}
