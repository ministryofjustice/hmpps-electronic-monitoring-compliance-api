package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.compliance

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.ComplianceState
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceCompliance
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationRevision
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.evaluation.RuleEvaluation
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.evaluation.RuleEvaluationResult
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.EventId
import java.time.Instant
import java.util.UUID

class DeviceComplianceMapperTest {

  private val ruleComplianceMapper = DeviceRuleComplianceMapper()
  private val mapper = DeviceComplianceMapper(ruleComplianceMapper)

  @Test
  fun `it should map device compliance to entity`() {
    // Given an activated device with battery level compliance
    val compliance = givenDeviceCompliance()

    // When mapped to an entity
    val entity = mapper.toEntity(compliance)

    // Then the device fields should be mapped
    assertThat(entity.id)
      .isEqualTo(compliance.id)

    assertThat(entity.deviceId)
      .isEqualTo(123)

    assertThat(entity.status)
      .isEqualTo(DeviceStatus.ACTIVATED)

    // When a rule has not been evaluated (i.e. NO_DATA), the device is considered non-compliant
    assertThat(entity.state)
      .isEqualTo(ComplianceState.NON_COMPLIANT)

    // Children are added by updateEntity rather than toEntity
    assertThat(entity.ruleCompliance)
      .isEmpty()
  }

  @Test
  fun `it should add new rule compliance to an entity`() {
    // Given an entity without any rule compliance
    val compliance = givenDeviceCompliance()
    val entity = mapper.toEntity(compliance)

    // When the entity is updated
    mapper.updateEntity(
      entity = entity,
      compliance = compliance,
    )

    // Then the missing rule compliance should be added
    assertThat(entity.ruleCompliance.single().id)
      .isEqualTo(compliance.ruleCompliance.single().id)

    assertThat(entity.ruleCompliance.single().deviceCompliance)
      .isSameAs(entity)

    assertThat(entity.ruleCompliance.single().deviceId)
      .isEqualTo(123)

    assertThat(entity.ruleCompliance.single().ruleId)
      .isEqualTo(BatteryLevelRuleV1.ruleDefinition.id.value)

    assertThat(entity.ruleCompliance.single().ruleVersion)
      .isEqualTo(BatteryLevelRuleV1.ruleDefinition.version.value)

    assertThat(entity.ruleCompliance.single().state)
      .isEqualTo(ComplianceState.NO_DATA)

    assertThat(entity.ruleCompliance.single().stateChangedAt)
      .isNull()
  }

  @Test
  fun `it should update existing rule compliance without replacing it`() {
    // Given an entity containing the existing rule compliance
    val compliance = givenDeviceCompliance()
    val entity = mapper.toEntity(compliance)

    // When the entity is updated
    mapper.updateEntity(
      entity = entity,
      compliance = compliance,
    )

    val existingEntity = entity.ruleCompliance.single()
    val existingId = existingEntity.id

    // And the domain rule compliance changes state
    compliance.apply(
      givenCompliantRuleEvaluation(),
    )

    // When the entity is updated
    mapper.updateEntity(
      entity = entity,
      compliance = compliance,
    )

    // Then the existing child should be updated rather than replaced
    assertThat(entity.ruleCompliance.single())
      .isSameAs(existingEntity)

    assertThat(entity.ruleCompliance.single().id)
      .isEqualTo(existingId)

    assertThat(entity.ruleCompliance.single().state)
      .isEqualTo(ComplianceState.COMPLIANT)

    assertThat(entity.ruleCompliance.single().stateChangedAt)
      .isEqualTo(
        Instant.parse("2026-01-01T10:00:00Z"),
      )

    // And the device state should also be updated
    assertThat(entity.state)
      .isEqualTo(ComplianceState.COMPLIANT)
  }

  @Test
  fun `it should remove rule compliance that is no longer in the aggregate`() {
    // Given an entity containing rule compliance
    val original = givenDeviceCompliance()
    val entity = mapper.toEntity(original)

    mapper.updateEntity(
      entity = entity,
      compliance = original,
    )

    assertThat(entity.ruleCompliance)
      .hasSize(1)

    // And the rehydrated aggregate no longer contains that rule compliance
    val updated = DeviceCompliance.rehydrate(
      id = original.id,
      deviceId = original.deviceId,
      status = original.status,
      ruleCompliance = emptyList(),
    )

    // When the entity is updated
    mapper.updateEntity(
      entity = entity,
      compliance = updated,
    )

    // Then the orphaned child should be removed
    assertThat(entity.ruleCompliance)
      .isEmpty()
  }

  @Test
  fun `it should map entity to device compliance`() {
    // Given an entity containing a battery level rule compliance
    val id = UUID.randomUUID()
    val ruleComplianceId = UUID.randomUUID()

    val entity = DeviceComplianceEntity(
      id = id,
      deviceId = 123,
      status = DeviceStatus.ACTIVATED,
      state = ComplianceState.NON_COMPLIANT,
    )

    entity.ruleCompliance.add(
      DeviceRuleComplianceEntity(
        id = ruleComplianceId,
        deviceCompliance = entity,
        deviceId = 123,
        ruleId = BatteryLevelRuleV1.ruleDefinition.id.value,
        ruleVersion = BatteryLevelRuleV1.ruleDefinition.version.value,
        state = ComplianceState.NO_DATA,
        stateChangedAt = null,
      ),
    )

    // When mapped to the domain
    val compliance = mapper.toDomain(entity)

    // Then the complete aggregate should be rehydrated
    assertThat(compliance.id)
      .isEqualTo(id)

    assertThat(compliance.deviceId)
      .isEqualTo(DeviceId(123))

    assertThat(compliance.status)
      .isEqualTo(DeviceStatus.ACTIVATED)

    assertThat(compliance.state)
      .isEqualTo(ComplianceState.NON_COMPLIANT)

    assertThat(compliance.ruleCompliance.single().id)
      .isEqualTo(ruleComplianceId)

    assertThat(compliance.ruleCompliance.single().deviceId)
      .isEqualTo(DeviceId(123))

    assertThat(compliance.ruleCompliance.single().ruleDefinition)
      .isEqualTo(BatteryLevelRuleV1.ruleDefinition)

    assertThat(compliance.ruleCompliance.single().state)
      .isEqualTo(ComplianceState.NO_DATA)

    assertThat(compliance.ruleCompliance.single().stateChangedAt)
      .isNull()
  }

  @Test
  fun `it should update device status and state`() {
    // Given an activated device
    val original = givenDeviceCompliance()
    val entity = mapper.toEntity(original)

    mapper.updateEntity(
      entity = entity,
      compliance = original,
    )

    // And the same device is rehydrated as deactivated
    val updated = DeviceCompliance.rehydrate(
      id = original.id,
      deviceId = original.deviceId,
      status = DeviceStatus.DEACTIVATED,
      ruleCompliance = original.ruleCompliance,
    )

    // When the entity is updated
    mapper.updateEntity(
      entity = entity,
      compliance = updated,
    )

    // Then its status and device state should be updated
    assertThat(entity.status)
      .isEqualTo(DeviceStatus.DEACTIVATED)

    assertThat(entity.state)
      .isNull()
  }

  private fun givenDeviceCompliance(): DeviceCompliance = DeviceCompliance.create(
    deviceId = DeviceId(123),
    status = DeviceStatus.ACTIVATED,
    ruleDefinitions = listOf(
      BatteryLevelRuleV1.ruleDefinition,
    ),
  )

  private fun givenCompliantRuleEvaluation(
    recordedAt: Instant = Instant.parse("2026-01-01T10:00:00Z"),
  ): RuleEvaluation = RuleEvaluation(
    deviceId = DeviceId(123),
    eventId = EventId(1),
    recordedAt = recordedAt,
    ruleDefinition = BatteryLevelRuleV1.ruleDefinition,
    configurationId = RuleConfigurationId(UUID.randomUUID()),
    configurationRevision = RuleConfigurationRevision(1),
    result = RuleEvaluationResult.Compliant,
  )
}
