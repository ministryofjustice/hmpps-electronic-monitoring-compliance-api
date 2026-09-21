package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.adapter.inbound.web.configuration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.compliance.DeviceComplianceEntity
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.compliance.DeviceComplianceRepository
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.compliance.DeviceRuleComplianceEntity
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.configuration.RuleConfigurationEntity
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.configuration.RuleConfigurationRepository
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.ComplianceState
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.IntegrationTestBase
import java.time.Instant
import java.util.UUID

class RuleConfigurationControllerTest : IntegrationTestBase() {

  @Autowired
  private lateinit var repository: RuleConfigurationRepository

  @Autowired
  private lateinit var deviceComplianceRepository: DeviceComplianceRepository

  @BeforeEach
  fun setUp() {
    repository.deleteAll()
    deviceComplianceRepository.deleteAll()
  }

  @Nested
  @DisplayName("GET /v1/rule-configurations")
  inner class GetRuleConfigurations {
    @Test
    fun `it should only return published configurations`() {
      val id = UUID.randomUUID()
      // Given a published and a draft rule configuration
      repository.saveAll(
        listOf(
          ruleConfigurationEntity(
            id = id,
            revision = 1,
            status = RuleConfigurationStatus.PUBLISHED,
            threshold = 20,
          ),
          ruleConfigurationEntity(
            revision = 2,
            status = RuleConfigurationStatus.DRAFT,
            threshold = 15,
          ),
        ),
      )

      // When we call the endpoint, only the published configuration should be returned
      webTestClient
        .get()
        .uri("/v1/rule-configurations")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .json(
          """
        [
          {
            "id": "$id",
            "ruleId": "BATTERY_LEVEL",
            "ruleVersion": 1,
            "revision": 1,
            "parameters": {
              "threshold": 20
            }
          }
        ]
          """.trimIndent(),
        )
    }

    @Test
    fun `it should return an empty list when there are no published configurations`() {
      webTestClient
        .get()
        .uri("/v1/rule-configurations")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .json("[]")
    }

    @Test
    fun `it should return not found when rule configuration does not exist`() {
      webTestClient
        .get()
        .uri(
          "/v1/rule-configurations/${UUID.randomUUID()}",
        )
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @Test
    fun `it should return bad request when rule configuration id is not a uuid`() {
      webTestClient
        .get()
        .uri(
          "/v1/rule-configurations/not-a-uuid",
        )
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isBadRequest
    }
  }

  @Nested
  @DisplayName("GET /v1/rule-configurations/{id}")
  inner class GetRuleConfiguration {
    @Test
    fun `it should get rule configuration`() {
      // Given a published rule config
      val configuration =
        repository.save(
          ruleConfigurationEntity(
            revision = 1,
            status = RuleConfigurationStatus.PUBLISHED,
            threshold = 20,
          ),
        )

      // And some device compliance data for that rule
      deviceComplianceRepository.save(
        deviceComplianceEntity(
          deviceId = 1,
          status = DeviceStatus.ACTIVATED,
          state = ComplianceState.COMPLIANT,
        ),
      )
      deviceComplianceRepository.save(
        deviceComplianceEntity(
          deviceId = 2,
          status = DeviceStatus.ACTIVATED,
          state = ComplianceState.COMPLIANT,
        ),
      )
      deviceComplianceRepository.save(
        deviceComplianceEntity(
          deviceId = 3,
          status = DeviceStatus.ACTIVATED,
          state = ComplianceState.NON_COMPLIANT,
        ),
      )
      deviceComplianceRepository.save(
        deviceComplianceEntity(
          deviceId = 4,
          status = DeviceStatus.ACTIVATED,
          state = ComplianceState.NO_DATA,
        ),
      )
      deviceComplianceRepository.save(
        deviceComplianceEntity(
          deviceId = 5,
          status = DeviceStatus.DEACTIVATED,
          state = ComplianceState.COMPLIANT,
        ),
      )

      // When we call the endpoint, it should return the configuration and a summary of device compliance
      webTestClient
        .get()
        .uri(
          "/v1/rule-configurations/${configuration.id}",
        )
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .json(
          """
      {
        "id": "${configuration.id}",
        "ruleId": "BATTERY_LEVEL",
        "ruleVersion": 1,
        "revision": 1,
        "parameters": {
          "threshold": 20
        },
        "status": "PUBLISHED",
        "summary": {
          "compliant": 2,
          "nonCompliant": 1,
          "noData": 1,
          "deactivated": 1
        }
      }
          """.trimIndent(),
        )
    }
  }

  private fun ruleConfigurationEntity(
    id: UUID = UUID.randomUUID(),
    revision: Int,
    status: RuleConfigurationStatus,
    threshold: Int,
  ) = RuleConfigurationEntity(
    id = id,
    ruleId = BatteryLevelRuleV1.ruleDefinition.id.value,
    ruleVersion = BatteryLevelRuleV1.ruleDefinition.version.value,
    revision = revision,
    status = status,
    parameters = mapOf(
      "threshold" to threshold,
    ),
    createdAt = Instant.parse("2026-01-01T10:00:00Z"),
    createdBy = "migration",
    publishedAt =
    if (status == RuleConfigurationStatus.PUBLISHED) {
      Instant.parse("2026-01-01T10:00:00Z")
    } else {
      null
    },
    publishedBy =
    if (status == RuleConfigurationStatus.PUBLISHED) {
      "migration"
    } else {
      null
    },
    effectiveFrom =
    if (status == RuleConfigurationStatus.PUBLISHED) {
      Instant.parse("2026-01-01T10:00:00Z")
    } else {
      null
    },
  )

  private fun deviceComplianceEntity(
    id: UUID = UUID.randomUUID(),
    deviceId: Int = 123,
    status: DeviceStatus = DeviceStatus.ACTIVATED,
    state: ComplianceState = ComplianceState.COMPLIANT,
  ): DeviceComplianceEntity {
    val deviceCompliance = DeviceComplianceEntity(
      id = id,
      deviceId = deviceId,
      status = status,
      state = state,
    )

    deviceCompliance.ruleCompliance.add(
      DeviceRuleComplianceEntity(
        id = UUID.randomUUID(),
        ruleId = BatteryLevelRuleV1.ruleDefinition.id.value,
        ruleVersion = BatteryLevelRuleV1.ruleDefinition.version.value,
        state = state,
        stateChangedAt = Instant.parse("2026-01-01T10:00:00Z"),
        deviceCompliance = deviceCompliance,
        deviceId = deviceId,
      ),
    )

    return deviceCompliance
  }
}
