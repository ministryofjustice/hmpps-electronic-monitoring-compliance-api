package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.evaluation

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.ComplianceState
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceCompliance
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceComplianceStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceComplianceSummary
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceRuleCompliance
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfiguration
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationRevision
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleDefinition
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleParameters
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleParameters
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryPercentage
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.EventId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.events.BatteryLevelReported
import java.time.Instant
import java.util.UUID

class EvaluateBatteryLevelEventTest {

  private val rule = BatteryLevelRuleV1()

  @Test
  fun `it should fail when device compliance has not been initialised`() {
    // Given a published configuration with a threshold of 20%
    val configuration =
      givenPublishedConfiguration(threshold = 20)

    // And a device compliance store that does not contain a record for the device
    val useCase =
      EvaluateBatteryLevelEvent(
        ruleConfigurationStore =
        FakeRuleConfigurationStore(configuration),
        deviceComplianceStore =
        FakeDeviceComplianceStore(),
      )

    // When a rule is evaluated, then it should throw
    assertThatThrownBy {
      useCase.evaluate(
        givenBatteryLevelReported(
          batteryPercentage = 10,
        ),
      )
    }
      .isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Device compliance not initialised")
  }

  @Test
  fun `it should update an existing compliance record`() {
    // Given a published configuration with a threshold of 20%
    val configuration = givenPublishedConfiguration(threshold = 20)

    // And an existing non-compliant record for the device
    val configurationStore = FakeRuleConfigurationStore(configuration)
    val complianceStore = FakeDeviceComplianceStore(givenExistingDeviceCompliance())
    val useCase = EvaluateBatteryLevelEvent(
      ruleConfigurationStore = configurationStore,
      deviceComplianceStore = complianceStore,
    )

    // When a battery level event with a battery percentage of 50% is evaluated
    useCase.evaluate(
      givenBatteryLevelReported(
        batteryPercentage = 50,
        recordedAt = Instant.parse("2026-01-01T10:05:00Z"),
      ),
    )

    val compliance = complianceStore.saved.single()

    // Then the device should be marked as compliant
    assertThat(compliance.state)
      .isEqualTo(ComplianceState.COMPLIANT)

    // And the rule compliance record should be updated to compliant
    assertThat(compliance.ruleCompliance.single().state)
      .isEqualTo(ComplianceState.COMPLIANT)

    // And the state changed time should be updated to the time of the evaluation
    assertThat(compliance.ruleCompliance.single().stateChangedAt)
      .isEqualTo(Instant.parse("2026-01-01T10:05:00Z"))
  }

  @Test
  fun `it should fail if there is no published configuration`() {
    // Given no published configuration for the battery level rule
    val useCase = EvaluateBatteryLevelEvent(
      ruleConfigurationStore = FakeRuleConfigurationStore(null),
      deviceComplianceStore = FakeDeviceComplianceStore(),
    )

    // When a battery level event is evaluated, it should throw an exception
    assertThatThrownBy {
      useCase.evaluate(
        givenBatteryLevelReported(
          batteryPercentage = 10,
          recordedAt = Instant.parse("2026-01-01T10:00:00Z"),
        ),
      )
    }
      .isInstanceOf(IllegalStateException::class.java)
      .hasMessage("No published configuration for BATTERY_LEVEL v1")
  }

  @Test
  fun `it should not change state changed time when compliance state does not change`() {
    // Given a published configuration with a threshold of 20%
    val configuration = givenPublishedConfiguration(threshold = 20)

    // And an existing non-compliance record for the device
    val complianceStore = FakeDeviceComplianceStore(givenExistingDeviceCompliance())
    val useCase = EvaluateBatteryLevelEvent(
      ruleConfigurationStore = FakeRuleConfigurationStore(configuration),
      deviceComplianceStore = complianceStore,
    )

    // When a battery level event with a battery percentage of 5% is evaluated
    useCase.evaluate(
      givenBatteryLevelReported(
        batteryPercentage = 5,
        recordedAt = Instant.parse("2026-01-01T10:05:00Z"),
      ),
    )

    val compliance = complianceStore.saved.single()

    // Then the device record should remain non-compliant
    assertThat(compliance.state)
      .isEqualTo(ComplianceState.NON_COMPLIANT)

    // And the rule compliance record should remain non-compliant
    assertThat(compliance.ruleCompliance.single().state)
      .isEqualTo(ComplianceState.NON_COMPLIANT)

    // And the state changed time does not change
    assertThat(compliance.ruleCompliance.single().stateChangedAt)
      .isEqualTo(Instant.parse("2026-01-01T10:00:00Z"))
  }

  private fun givenBatteryLevelReported(
    batteryPercentage: Int,
    recordedAt: Instant = Instant.parse("2026-01-01T10:00:00Z"),
  ): BatteryLevelReported = BatteryLevelReported(
    eventId = EventId(1),
    deviceId = DeviceId(123),
    recordedAt = recordedAt,
    batteryPercentage = BatteryPercentage(batteryPercentage),
  )

  private fun givenPublishedConfiguration(
    threshold: Int,
  ): RuleConfiguration<BatteryLevelRuleParameters> = RuleConfiguration.rehydrate(
    id = RuleConfigurationId(UUID.randomUUID()),
    ruleDefinition = BatteryLevelRuleV1.ruleDefinition,
    revision = RuleConfigurationRevision(1),
    parameters = BatteryLevelRuleParameters(
      threshold = BatteryPercentage(threshold),
    ),
    status = RuleConfigurationStatus.PUBLISHED,
    createdAt = Instant.parse("2026-01-01T09:00:00Z"),
    createdBy = "system",
    publishedAt = Instant.parse("2026-01-01T09:00:00Z"),
    publishedBy = "system",
    effectiveFrom = Instant.parse("2026-01-01T09:00:00Z"),
  )

  private fun givenExistingDeviceCompliance(
    state: ComplianceState = ComplianceState.NON_COMPLIANT,
  ): DeviceCompliance = DeviceCompliance.rehydrate(
    id = UUID.randomUUID(),
    deviceId = DeviceId(123),
    status = DeviceStatus.ACTIVATED,
    ruleCompliance = listOf(
      DeviceRuleCompliance.rehydrate(
        id = UUID.randomUUID(),
        deviceId = DeviceId(123),
        ruleDefinition = BatteryLevelRuleV1.ruleDefinition,
        state = state,
        stateChangedAt = Instant.parse("2026-01-01T10:00:00Z"),
      ),
    ),
  )

  private class FakeDeviceComplianceStore(
    private var compliance: DeviceCompliance? = null,
  ) : DeviceComplianceStore {

    val saved = mutableListOf<DeviceCompliance>()

    override fun find(
      deviceId: DeviceId,
    ): DeviceCompliance? = compliance?.takeIf {
      it.deviceId == deviceId
    }

    override fun findById(id: UUID): DeviceCompliance? = null

    override fun findAll(): List<DeviceCompliance> = listOf()

    override fun findAllSummaries(): List<DeviceComplianceSummary> = listOf()

    override fun save(
      compliance: DeviceCompliance,
    ): DeviceCompliance {
      this.compliance = compliance
      saved += compliance
      return compliance
    }
  }

  private class FakeRuleConfigurationStore(
    private val configuration: RuleConfiguration<BatteryLevelRuleParameters>? = null,
  ) : RuleConfigurationStore {

    override fun findPublished(): List<RuleConfiguration<out RuleParameters>> = listOfNotNull(configuration)

    @Suppress("UNCHECKED_CAST")
    override fun <P : RuleParameters> findPublished(
      definition: RuleDefinition<P>,
    ): RuleConfiguration<P>? = configuration
      ?.takeIf { it.ruleDefinition == definition }
      as RuleConfiguration<P>?
  }
}
