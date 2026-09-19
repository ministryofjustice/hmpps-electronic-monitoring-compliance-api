package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.adapter.inbound.web.compliance

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.compliance.DeviceComplianceRepository
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

class DeviceComplianceControllerTest : IntegrationTestBase() {

  @Autowired
  private lateinit var deviceComplianceStore: DeviceComplianceStore

  @Autowired
  private lateinit var repository: DeviceComplianceRepository

  @BeforeEach
  fun setUp() {
    repository.deleteAll()
  }

  @Test
  fun `it should list device compliance`() {
    // Given a compliant device
    val compliant = givenDeviceCompliance(
      deviceId = 123,
      status = DeviceStatus.ACTIVATED,
    )

    compliant.apply(
      givenCompliantEvaluation(
        deviceId = 123,
      ),
    )

    deviceComplianceStore.save(compliant)

    // And a device without compliant rule data
    deviceComplianceStore.save(
      givenDeviceCompliance(
        deviceId = 456,
        status = DeviceStatus.ACTIVATED,
      ),
    )

    // And a deactivated device
    deviceComplianceStore.save(
      givenDeviceCompliance(
        deviceId = 789,
        status = DeviceStatus.DEACTIVATED,
      ),
    )

    webTestClient
      .get()
      .uri("/v1/device-compliance")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .json(
        """
          {
            "summary": {
              "compliant": 1,
              "nonCompliant": 1,
              "deactivated": 1
            },
            "devices": [
              {
                "deviceId": 123,
                "status": "ACTIVATED",
                "state": "COMPLIANT"
              },
              {
                "deviceId": 456,
                "status": "ACTIVATED",
                "state": NON_COMPLIANT
              },
              {
                "deviceId": 789,
                "status": "DEACTIVATED",
                "state": null
              }
            ]
          }
        """.trimIndent(),
      )
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

  private fun givenCompliantEvaluation(
    deviceId: Int,
  ) = RuleEvaluation(
    deviceId = DeviceId(deviceId),
    eventId = EventId(1),
    recordedAt = Instant.parse("2026-01-01T10:00:00Z"),
    ruleDefinition = BatteryLevelRuleV1.ruleDefinition,
    configurationId = RuleConfigurationId(UUID.randomUUID()),
    configurationRevision = RuleConfigurationRevision(1),
    result = RuleEvaluationResult.Compliant,
  )
}
