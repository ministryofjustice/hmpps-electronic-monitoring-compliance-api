package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.compliance

import org.assertj.core.api.Assertions.assertThat
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
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryAtOrBelowThreshold
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryPercentage
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.EventId
import java.time.Instant
import java.util.UUID

class ListDeviceComplianceTest {

  @Test
  fun `it should list device compliance with summary counts`() {
    // Given a compliant, non-compliant and deactivated device
    val compliant = givenDeviceCompliance(
      deviceId = 1,
      status = DeviceStatus.ACTIVATED,
    ).also {
      it.apply(
        givenEvaluation(
          deviceId = 1,
          result = RuleEvaluationResult.Compliant,
        ),
      )
    }

    val nonCompliant = givenDeviceCompliance(
      deviceId = 2,
      status = DeviceStatus.ACTIVATED,
    ).also {
      it.apply(
        givenEvaluation(
          deviceId = 2,
          result = RuleEvaluationResult.NonCompliant(
            reason = BatteryAtOrBelowThreshold(
              actual = BatteryPercentage(10),
              threshold = BatteryPercentage(20),
            ),
          ),
        ),
      )
    }

    val deactivated = givenDeviceCompliance(
      deviceId = 3,
      status = DeviceStatus.DEACTIVATED,
    )

    val useCase = ListDeviceCompliance(
      store = FakeDeviceComplianceStore(
        listOf(
          compliant,
          nonCompliant,
          deactivated,
        ),
      ),
    )

    // When device compliance is listed
    val result = useCase.list()

    // Then the summary counts should be calculated
    assertThat(result.summary)
      .isEqualTo(
        DeviceComplianceCounts(
          compliant = 1,
          nonCompliant = 1,
          deactivated = 1,
        ),
      )

    // And all devices should be returned
    assertThat(result.devices)
      .containsExactly(
        DeviceComplianceSummary(
          id = compliant.id,
          deviceId = DeviceId(1),
          status = DeviceStatus.ACTIVATED,
          state = ComplianceState.COMPLIANT,
        ),
        DeviceComplianceSummary(
          id = nonCompliant.id,
          deviceId = DeviceId(2),
          status = DeviceStatus.ACTIVATED,
          state = ComplianceState.NON_COMPLIANT,
        ),
        DeviceComplianceSummary(
          id = deactivated.id,
          deviceId = DeviceId(3),
          status = DeviceStatus.DEACTIVATED,
          state = null,
        ),
      )
  }

  @Test
  fun `it should return empty summary when there are no devices`() {
    // Given no device compliance records in the store
    val useCase = ListDeviceCompliance(
      store = FakeDeviceComplianceStore(),
    )

    // When device compliance is listed
    val result = useCase.list()

    // Then the summary counts should be zero
    assertThat(result.summary)
      .isEqualTo(
        DeviceComplianceCounts(
          compliant = 0,
          nonCompliant = 0,
          deactivated = 0,
        ),
      )

    // And no devices should be returned
    assertThat(result.devices)
      .isEmpty()
  }

  private fun givenDeviceCompliance(
    deviceId: Int,
    status: DeviceStatus,
  ) = DeviceCompliance.create(
    deviceId = DeviceId(deviceId),
    status = status,
    ruleDefinitions = listOf(
      BatteryLevelRuleV1.ruleDefinition,
    ),
  )

  private fun givenEvaluation(
    deviceId: Int,
    result: RuleEvaluationResult,
  ) = RuleEvaluation(
    deviceId = DeviceId(deviceId),
    eventId = EventId(1),
    recordedAt = Instant.parse("2026-01-01T10:00:00Z"),
    ruleDefinition = BatteryLevelRuleV1.ruleDefinition,
    configurationId = RuleConfigurationId(UUID.randomUUID()),
    configurationRevision = RuleConfigurationRevision(1),
    result = result,
  )

  private class FakeDeviceComplianceStore(
    private val compliance: List<DeviceCompliance> = emptyList(),
  ) : DeviceComplianceStore {

    override fun find(
      deviceId: DeviceId,
    ): DeviceCompliance? = compliance.firstOrNull {
      it.deviceId == deviceId
    }

    override fun findById(id: UUID): DeviceCompliance? = null

    override fun findAll(): List<DeviceCompliance> = compliance

    override fun findAllSummaries(): List<DeviceComplianceSummary> = compliance.map {
      DeviceComplianceSummary(
        id = it.id,
        deviceId = it.deviceId,
        status = it.status,
        state = it.state,
      )
    }

    override fun save(
      compliance: DeviceCompliance,
    ): DeviceCompliance = compliance
  }
}
