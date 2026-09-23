package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.compliance

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.ComplianceState
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceCompliance
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceComplianceStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceComplianceSummary
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceRuleComplianceCounts
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleVersion
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.Device
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.ElectronicMonitoringDataStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.events.BatteryLevelReported
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils.FakeRuleConfigurationStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils.RuleComplianceFixtures.givenPublishedBatteryLevelConfiguration
import java.util.UUID

class SynchroniseDeviceComplianceTest {

  @Test
  fun `it should create compliance for a new device`() {
    val datastore = FakeElectronicMonitoringDataStore(
      devices = listOf(
        Device(
          id = DeviceId(123),
          status = DeviceStatus.ACTIVATED,
        ),
      ),
    )

    val complianceStore = FakeDeviceComplianceStore()

    val useCase = SynchroniseDeviceCompliance(
      datastore = datastore,
      ruleConfigurationStore = FakeRuleConfigurationStore(
        listOf(givenPublishedBatteryLevelConfiguration()),
      ),
      deviceComplianceStore = complianceStore,
    )

    val count = useCase.synchronise()

    assertThat(count).isEqualTo(1)

    val saved = complianceStore.saved.single()

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
  }

  @Test
  fun `it should synchronise status for an existing device`() {
    val existing = DeviceCompliance.create(
      deviceId = DeviceId(123),
      status = DeviceStatus.ACTIVATED,
      ruleDefinitions = listOf(
        BatteryLevelRuleV1.ruleDefinition,
      ),
    )

    val datastore = FakeElectronicMonitoringDataStore(
      devices = listOf(
        Device(
          id = DeviceId(123),
          status = DeviceStatus.DEACTIVATED,
        ),
      ),
    )

    val complianceStore =
      FakeDeviceComplianceStore(existing = listOf(existing))

    val useCase = SynchroniseDeviceCompliance(
      datastore = datastore,
      ruleConfigurationStore = FakeRuleConfigurationStore(
        listOf(givenPublishedBatteryLevelConfiguration()),
      ),
      deviceComplianceStore = complianceStore,
    )

    useCase.synchronise()

    val saved = complianceStore.saved.single()

    assertThat(saved.id)
      .isEqualTo(existing.id)

    assertThat(saved.status)
      .isEqualTo(DeviceStatus.DEACTIVATED)

    assertThat(saved.state)
      .isNull()
  }

  @Test
  fun `it should add newly published rules to an existing device`() {
    val existing = DeviceCompliance.create(
      deviceId = DeviceId(123),
      status = DeviceStatus.ACTIVATED,
      ruleDefinitions = emptyList(),
    )

    val complianceStore =
      FakeDeviceComplianceStore(existing = listOf(existing))

    val useCase = SynchroniseDeviceCompliance(
      datastore = FakeElectronicMonitoringDataStore(
        devices = listOf(
          Device(
            id = DeviceId(123),
            status = DeviceStatus.ACTIVATED,
          ),
        ),
      ),
      ruleConfigurationStore = FakeRuleConfigurationStore(
        listOf(givenPublishedBatteryLevelConfiguration()),
      ),
      deviceComplianceStore = complianceStore,
    )

    useCase.synchronise()

    val saved = complianceStore.saved.single()

    assertThat(saved.ruleCompliance.single().ruleDefinition)
      .isEqualTo(BatteryLevelRuleV1.ruleDefinition)

    assertThat(saved.ruleCompliance.single().state)
      .isEqualTo(ComplianceState.NO_DATA)
  }

  @Test
  fun `it should remove rules that are no longer published`() {
    val existing = DeviceCompliance.create(
      deviceId = DeviceId(123),
      status = DeviceStatus.ACTIVATED,
      ruleDefinitions = listOf(
        BatteryLevelRuleV1.ruleDefinition,
      ),
    )

    val complianceStore =
      FakeDeviceComplianceStore(existing = listOf(existing))

    val useCase = SynchroniseDeviceCompliance(
      datastore = FakeElectronicMonitoringDataStore(
        devices = listOf(
          Device(
            id = DeviceId(123),
            status = DeviceStatus.ACTIVATED,
          ),
        ),
      ),
      ruleConfigurationStore =
      FakeRuleConfigurationStore(),
      deviceComplianceStore = complianceStore,
    )

    useCase.synchronise()

    val saved = complianceStore.saved.single()

    assertThat(saved.ruleCompliance)
      .isEmpty()
  }

  @Test
  fun `it should not change devices missing from datastore`() {
    val existing = DeviceCompliance.create(
      deviceId = DeviceId(123),
      status = DeviceStatus.ACTIVATED,
      ruleDefinitions = listOf(
        BatteryLevelRuleV1.ruleDefinition,
      ),
    )

    val complianceStore =
      FakeDeviceComplianceStore(existing = listOf(existing))

    val useCase = SynchroniseDeviceCompliance(
      datastore = FakeElectronicMonitoringDataStore(
        devices = emptyList(),
      ),
      ruleConfigurationStore = FakeRuleConfigurationStore(
        listOf(givenPublishedBatteryLevelConfiguration()),
      ),
      deviceComplianceStore = complianceStore,
    )

    val count = useCase.synchronise()

    assertThat(count).isZero()
    assertThat(complianceStore.saved).isEmpty()
  }

  @Test
  fun `it should synchronise every device returned by datastore`() {
    val useCase = SynchroniseDeviceCompliance(
      datastore = FakeElectronicMonitoringDataStore(
        devices = listOf(
          Device(
            id = DeviceId(123),
            status = DeviceStatus.ACTIVATED,
          ),
          Device(
            id = DeviceId(456),
            status = DeviceStatus.DEACTIVATED,
          ),
        ),
      ),
      ruleConfigurationStore = FakeRuleConfigurationStore(
        listOf(givenPublishedBatteryLevelConfiguration()),
      ),
      deviceComplianceStore =
      FakeDeviceComplianceStore(),
    )

    val count = useCase.synchronise()

    assertThat(count).isEqualTo(2)
  }

  private class FakeElectronicMonitoringDataStore(
    private val devices: List<Device>,
  ) : ElectronicMonitoringDataStore {

    override fun getDevices(): Sequence<Device> = devices.asSequence()

    override fun getBatteryLevelReportedEvents(): Sequence<BatteryLevelReported> = emptySequence()
  }

  private class FakeDeviceComplianceStore(
    existing: List<DeviceCompliance> = emptyList(),
  ) : DeviceComplianceStore {

    private val compliance =
      existing.associateBy { it.deviceId }
        .toMutableMap()

    val saved =
      mutableListOf<DeviceCompliance>()

    override fun find(
      deviceId: DeviceId,
    ): DeviceCompliance? = compliance[deviceId]

    override fun findById(id: UUID): DeviceCompliance? = null

    override fun findAll(): List<DeviceCompliance> = compliance.values.toList()

    override fun findAllSummaries(): List<DeviceComplianceSummary> = listOf()

    override fun save(
      compliance: DeviceCompliance,
    ): DeviceCompliance {
      this.compliance[compliance.deviceId] =
        compliance

      saved += compliance

      return compliance
    }

    override fun getRuleComplianceSummary(ruleId: RuleId, ruleVersion: RuleVersion): DeviceRuleComplianceCounts = DeviceRuleComplianceCounts(
      compliant = 0,
      nonCompliant = 0,
      noData = 0,
      deactivated = 0,
    )
  }
}
