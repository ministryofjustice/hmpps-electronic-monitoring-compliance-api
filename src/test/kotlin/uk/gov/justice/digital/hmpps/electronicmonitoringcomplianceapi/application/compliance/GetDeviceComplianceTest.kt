package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.compliance

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.ComplianceState
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceCompliance
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceComplianceStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceComplianceSummary
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

class GetDeviceComplianceTest {

  @Test
  fun `it should get device compliance`() {
    // Given a device compliance with a rule evaluation
    val compliance = DeviceCompliance.create(
      deviceId = DeviceId(123),
      status = DeviceStatus.ACTIVATED,
      ruleDefinitions = listOf(
        BatteryLevelRuleV1.ruleDefinition,
      ),
    )

    compliance.apply(
      RuleEvaluation(
        deviceId = DeviceId(123),
        eventId = EventId(1),
        recordedAt = Instant.parse("2026-01-01T10:00:00Z"),
        ruleDefinition = BatteryLevelRuleV1.ruleDefinition,
        configurationId = RuleConfigurationId(UUID.randomUUID()),
        configurationRevision = RuleConfigurationRevision(1),
        result = RuleEvaluationResult.Compliant,
      ),
    )

    val useCase = GetDeviceCompliance(
      store = FakeDeviceComplianceStore(
        compliance,
      ),
    )

    // When we get the device compliance

    val result = useCase.get(compliance.id)

    // Then the result should be correct
    assertThat(result.deviceId)
      .isEqualTo(123)
    assertThat(result.status)
      .isEqualTo(DeviceStatus.ACTIVATED)
    assertThat(result.state)
      .isEqualTo(ComplianceState.COMPLIANT)
    assertThat(result.stateChangedAt)
      .isEqualTo(
        Instant.parse("2026-01-01T10:00:00Z"),
      )
    assertThat(result.rules.single().ruleId).isEqualTo(
      BatteryLevelRuleV1.ruleDefinition.id.value,
    )
    assertThat(result.rules.single().ruleVersion).isEqualTo(
      BatteryLevelRuleV1.ruleDefinition.version.value,
    )
    assertThat(result.rules.single().state)
      .isEqualTo(ComplianceState.COMPLIANT)
    assertThat(result.rules.single().stateChangedAt)
      .isEqualTo(
        Instant.parse("2026-01-01T10:00:00Z"),
      )
  }

  @Test
  fun `it should throw when device compliance does not exist`() {
    // Given a device compliance id that does not exist
    val id = UUID.randomUUID()

    val useCase = GetDeviceCompliance(
      store = FakeDeviceComplianceStore(),
    )

    // When we get the device compliance, then it should throw an exception
    assertThatThrownBy {
      useCase.get(id)
    }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessage(
        "Device compliance with id $id not found",
      )
  }

  private class FakeDeviceComplianceStore(
    private val compliance: DeviceCompliance? = null,
  ) : DeviceComplianceStore {

    override fun findById(
      id: UUID,
    ): DeviceCompliance? = compliance?.takeIf {
      it.id == id
    }

    override fun find(
      deviceId: DeviceId,
    ): DeviceCompliance? = compliance?.takeIf {
      it.deviceId == deviceId
    }

    override fun findAll(): List<DeviceCompliance> = listOfNotNull(compliance)

    override fun findAllSummaries(): List<DeviceComplianceSummary> = listOf()

    override fun save(
      compliance: DeviceCompliance,
    ): DeviceCompliance = compliance
  }
}
