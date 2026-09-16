package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.adapter.inbound.web.configuration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.configuration.RuleConfigurationEntity
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.configuration.RuleConfigurationRepository
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.IntegrationTestBase
import java.time.Instant
import java.util.UUID

class RuleConfigurationControllerTest : IntegrationTestBase() {

  @Autowired
  private lateinit var repository: RuleConfigurationRepository

  @BeforeEach
  fun setUp() {
    repository.deleteAll()
  }

  @Nested
  @DisplayName("GET /v1/rule-configurations")
  inner class GetRuleConfigurations {

    @Test
    fun `it should only return published configurations`() {
      // Given a published and a draft rule configuration
      repository.saveAll(
        listOf(
          ruleConfigurationEntity(
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
  }

  private fun ruleConfigurationEntity(
    revision: Int,
    status: RuleConfigurationStatus,
    threshold: Int,
  ) = RuleConfigurationEntity(
    id = UUID.randomUUID(),
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
}
