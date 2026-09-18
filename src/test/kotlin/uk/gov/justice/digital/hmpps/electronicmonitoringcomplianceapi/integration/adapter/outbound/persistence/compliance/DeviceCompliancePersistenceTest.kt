package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.adapter.outbound.persistence.compliance

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.compliance.DeviceComplianceRepository
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.ComplianceState
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceCompliance
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceComplianceStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationRevision
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.evaluation.RuleEvaluation
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.evaluation.RuleEvaluationResult
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.EventId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.IntegrationTestBase
import java.time.Instant
import java.util.UUID

class DeviceCompliancePersistenceTest : IntegrationTestBase() {

  @Autowired
  private lateinit var store: DeviceComplianceStore

  @Autowired
  private lateinit var repository: DeviceComplianceRepository

  @BeforeEach
  fun setUp() {
    repository.deleteAll()
  }

  @Test
  fun `it should save and retrieve device compliance`() {
    // Given an activated device with an unevaluated battery level rule
    val compliance = givenDeviceCompliance()

    // When the compliance is saved
    store.save(compliance)

    // Then it should be retrievable with its rule compliance
    val saved = store.find(DeviceId(123))

    assertThat(saved).isNotNull

    assertThat(saved!!.id)
      .isEqualTo(compliance.id)

    assertThat(saved.deviceId)
      .isEqualTo(DeviceId(123))

    assertThat(saved.status)
      .isEqualTo(DeviceStatus.ACTIVATED)

    assertThat(saved.state)
      .isEqualTo(ComplianceState.NON_COMPLIANT)

    assertThat(saved.ruleCompliance.single().ruleDefinition)
      .isEqualTo(BatteryLevelRuleV1.ruleDefinition)

    assertThat(saved.ruleCompliance.single().state)
      .isEqualTo(ComplianceState.NO_DATA)

    assertThat(saved.ruleCompliance.single().stateChangedAt)
      .isNull()
  }

  @Test
  fun `it should update existing rule compliance without replacing it`() {
    // Given a persisted device compliance
    val compliance = givenDeviceCompliance()

    store.save(compliance)

    val originalRuleComplianceId =
      compliance.ruleCompliance.single().id

    // And the battery level rule becomes compliant
    compliance.apply(
      givenCompliantRuleEvaluation(),
    )

    // When the aggregate is saved again
    store.save(compliance)

    // Then the existing rule compliance should be updated
    val saved = store.find(DeviceId(123))!!

    assertThat(saved.state)
      .isEqualTo(ComplianceState.COMPLIANT)

    assertThat(saved.ruleCompliance.single().id)
      .isEqualTo(originalRuleComplianceId)

    assertThat(saved.ruleCompliance.single().state)
      .isEqualTo(ComplianceState.COMPLIANT)

    assertThat(saved.ruleCompliance.single().stateChangedAt)
      .isEqualTo(
        Instant.parse("2026-01-01T10:00:00Z"),
      )
  }

  @Test
  fun `it should persist subsequent rule compliance state changes`() {
    // Given a compliant device
    val compliance = givenDeviceCompliance()

    compliance.apply(
      givenCompliantRuleEvaluation(
        recordedAt =
        Instant.parse("2026-01-01T10:00:00Z"),
      ),
    )

    store.save(compliance)

    val originalRuleComplianceId =
      compliance.ruleCompliance.single().id

    // When a later evaluation makes the device non-compliant
    compliance.apply(
      givenNonCompliantRuleEvaluation(
        recordedAt =
        Instant.parse("2026-01-01T10:05:00Z"),
      ),
    )

    store.save(compliance)

    // Then the same child row should contain the new state
    val saved = store.find(DeviceId(123))!!

    assertThat(saved.state)
      .isEqualTo(ComplianceState.NON_COMPLIANT)

    assertThat(saved.ruleCompliance.single().id)
      .isEqualTo(originalRuleComplianceId)

    assertThat(saved.ruleCompliance.single().state)
      .isEqualTo(ComplianceState.NON_COMPLIANT)

    assertThat(saved.ruleCompliance.single().stateChangedAt)
      .isEqualTo(
        Instant.parse("2026-01-01T10:05:00Z"),
      )
  }

  @Test
  fun `it should preserve state changed time when rule compliance does not change`() {
    // Given a non-compliant device
    val compliance = givenDeviceCompliance()

    compliance.apply(
      givenNonCompliantRuleEvaluation(
        recordedAt =
        Instant.parse("2026-01-01T10:00:00Z"),
      ),
    )

    store.save(compliance)

    // When another non-compliant evaluation is applied
    compliance.apply(
      givenNonCompliantRuleEvaluation(
        recordedAt =
        Instant.parse("2026-01-01T10:05:00Z"),
      ),
    )

    store.save(compliance)

    // Then the original state change time should be retained
    val saved = store.find(DeviceId(123))!!

    assertThat(saved.ruleCompliance.single().state)
      .isEqualTo(ComplianceState.NON_COMPLIANT)

    assertThat(saved.ruleCompliance.single().stateChangedAt)
      .isEqualTo(
        Instant.parse("2026-01-01T10:00:00Z"),
      )
  }

  @Test
  fun `it should return null when device compliance does not exist`() {
    val compliance = store.find(DeviceId(999))

    assertThat(compliance).isNull()
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

  private fun givenNonCompliantRuleEvaluation(
    recordedAt: Instant = Instant.parse("2026-01-01T10:00:00Z"),
  ): RuleEvaluation = RuleEvaluation(
    deviceId = DeviceId(123),
    eventId = EventId(2),
    recordedAt = recordedAt,
    ruleDefinition = BatteryLevelRuleV1.ruleDefinition,
    configurationId = RuleConfigurationId(UUID.randomUUID()),
    configurationRevision = RuleConfigurationRevision(1),
    result = RuleEvaluationResult.NonCompliant(
      reason = uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryAtOrBelowThreshold(
        actual = uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryPercentage(10),
        threshold = uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryPercentage(20),
      ),
    ),
  )
}
