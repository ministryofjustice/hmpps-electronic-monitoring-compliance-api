package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.evaluation

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.ComplianceState
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceRuleCompliance
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceRuleComplianceStore
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
  fun `it should create a compliance record from first evaluation`() {
    // Given a published configuration with a threshold of 20%
    val configuration = givenPublishedConfiguration(threshold = 20)
    val configurationStore = FakeRuleConfigurationStore(configuration)
    val complianceStore = FakeDeviceRuleComplianceStore()
    val useCase = EvaluateBatteryLevelEvent(
      ruleConfigurationStore = configurationStore,
      deviceRuleComplianceStore = complianceStore,
    )

    // When a battery level event with a battery percentage of 10% is evaluated
    useCase.evaluate(
      givenBatteryLevelReported(
        batteryPercentage = 10,
        recordedAt = Instant.parse("2026-01-01T10:00:00Z"),
      ),
    )

    val compliance = complianceStore.saved.single()

    // Then a new non-compliance record is created
    assertThat(compliance.deviceId).isEqualTo(DeviceId(123))
    assertThat(compliance.ruleDefinition)
      .isEqualTo(BatteryLevelRuleV1.ruleDefinition)
    assertThat(compliance.state)
      .isEqualTo(ComplianceState.NON_COMPLIANT)
    assertThat(compliance.stateChangedAt)
      .isEqualTo(Instant.parse("2026-01-01T10:00:00Z"))
  }

  @Test
  fun `it should update an existing compliance record`() {
    // Given a published configuration with a threshold of 20%
    val configuration = givenPublishedConfiguration(threshold = 20)

    // And an existing non-compliance record for the device
    val existing = DeviceRuleCompliance.from(
      rule.evaluate(
        givenBatteryLevelReported(
          batteryPercentage = 10,
          recordedAt = Instant.parse("2026-01-01T10:00:00Z"),
        ),
        configuration,
      ),
    )
    val configurationStore = FakeRuleConfigurationStore(configuration)
    val complianceStore = FakeDeviceRuleComplianceStore(existing)
    val useCase = EvaluateBatteryLevelEvent(
      ruleConfigurationStore = configurationStore,
      deviceRuleComplianceStore = complianceStore,
    )

    // When a battery level event with a battery percentage of 50% is evaluated
    useCase.evaluate(
      givenBatteryLevelReported(
        batteryPercentage = 50,
        recordedAt = Instant.parse("2026-01-01T10:05:00Z"),
      ),
    )

    val compliance = complianceStore.saved.single()

    // Then the existing compliance record is updated to compliant
    assertThat(compliance.state)
      .isEqualTo(ComplianceState.COMPLIANT)
    assertThat(compliance.stateChangedAt)
      .isEqualTo(Instant.parse("2026-01-01T10:05:00Z"))
  }

  @Test
  fun `it should fail if there is no published configuration`() {
    // Given no published configuration for the battery level rule
    val useCase = EvaluateBatteryLevelEvent(
      ruleConfigurationStore = FakeRuleConfigurationStore(null),
      deviceRuleComplianceStore = FakeDeviceRuleComplianceStore(),
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
    val existing = DeviceRuleCompliance.from(
      rule.evaluate(
        givenBatteryLevelReported(
          batteryPercentage = 10,
          recordedAt = Instant.parse("2026-01-01T10:00:00Z"),
        ),
        configuration,
      ),
    )

    val complianceStore = FakeDeviceRuleComplianceStore(existing)
    val useCase = EvaluateBatteryLevelEvent(
      ruleConfigurationStore = FakeRuleConfigurationStore(configuration),
      deviceRuleComplianceStore = complianceStore,
    )

    // When a battery level event with a battery percentage of 5% is evaluated
    useCase.evaluate(
      givenBatteryLevelReported(
        batteryPercentage = 5,
        recordedAt = Instant.parse("2026-01-01T10:05:00Z"),
      ),
    )

    val compliance = complianceStore.saved.single()

    // Then the existing compliance record remains non-compliant
    assertThat(compliance.state).isEqualTo(ComplianceState.NON_COMPLIANT)

    // And the state changed time does not change
    assertThat(compliance.stateChangedAt)
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

  private class FakeDeviceRuleComplianceStore(
    private var compliance: DeviceRuleCompliance? = null,
  ) : DeviceRuleComplianceStore {

    val saved = mutableListOf<DeviceRuleCompliance>()

    override fun find(
      deviceId: DeviceId,
      ruleDefinition: RuleDefinition<*>,
    ): DeviceRuleCompliance? = compliance?.takeIf {
      it.deviceId == deviceId &&
        it.ruleDefinition == ruleDefinition
    }

    override fun save(
      compliance: DeviceRuleCompliance,
    ): DeviceRuleCompliance {
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
