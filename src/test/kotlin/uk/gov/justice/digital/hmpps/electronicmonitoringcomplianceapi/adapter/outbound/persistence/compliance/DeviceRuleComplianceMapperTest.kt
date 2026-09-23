package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.compliance

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.ComplianceState
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceRuleCompliance
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import java.time.Instant
import java.util.UUID

class DeviceRuleComplianceMapperTest {

  private val mapper = DeviceRuleComplianceMapper()

  @Test
  fun `it should map a device rule compliance to entity`() {
    val compliance = givenComplianceEntity()
    val ruleCompliance = givenRuleCompliance()

    val entity = mapper.toEntity(compliance, ruleCompliance)

    assertThat(entity.id).isEqualTo(ruleCompliance.id)
    assertThat(entity.deviceCompliance).isEqualTo(compliance)
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
    val compliance = givenComplianceEntity()
    val entity = DeviceRuleComplianceEntity(
      id = id,
      deviceId = 123,
      deviceCompliance = compliance,
      ruleId = "BATTERY_LEVEL",
      ruleVersion = 1,
      state = ComplianceState.NON_COMPLIANT,
      stateChangedAt = Instant.parse("2026-01-01T10:00:00Z"),
    )

    val ruleCompliance = mapper.toDomain(entity)

    assertThat(ruleCompliance.id).isEqualTo(id)
    assertThat(ruleCompliance.deviceId).isEqualTo(DeviceId(123))
    assertThat(ruleCompliance.ruleDefinition)
      .isEqualTo(BatteryLevelRuleV1.ruleDefinition)
    assertThat(ruleCompliance.state)
      .isEqualTo(ComplianceState.NON_COMPLIANT)
    assertThat(ruleCompliance.stateChangedAt)
      .isEqualTo(Instant.parse("2026-01-01T10:00:00Z"))
  }

  @Test
  fun `rejects unknown rule definition`() {
    val compliance = givenComplianceEntity()
    val entity = DeviceRuleComplianceEntity(
      id = UUID.randomUUID(),
      deviceId = 123,
      deviceCompliance = compliance,
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

  private fun givenComplianceEntity(): DeviceComplianceEntity = DeviceComplianceEntity(
    id = UUID.randomUUID(),
    deviceId = 123,
    status = DeviceStatus.ACTIVATED,
    state = ComplianceState.NON_COMPLIANT,
  )

  private fun givenRuleCompliance(): DeviceRuleCompliance = DeviceRuleCompliance.rehydrate(
    id = UUID.randomUUID(),
    deviceId = DeviceId(123),
    ruleDefinition = BatteryLevelRuleV1.ruleDefinition,
    state = ComplianceState.NON_COMPLIANT,
    stateChangedAt = Instant.parse("2026-01-01T10:00:00Z"),
  )
}
