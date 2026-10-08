package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.adapter.outbound.persistence.configuration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.configuration.RuleConfigurationRepository
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.testutils.RuleConfigurationFixtures.givenPublishedBatteryLevelConfiguration
import java.time.Instant

class RuleConfigurationPersistenceTest : IntegrationTestBase() {

  @Autowired
  private lateinit var store: RuleConfigurationStore

  @Autowired
  private lateinit var repository: RuleConfigurationRepository

  @BeforeEach
  fun setUp() {
    repository.deleteAll()
  }

  @Test
  fun `it should list the latest published configurations`() {
    // Given two published revisions of the same configuration
    val old = givenPublishedBatteryLevelConfiguration()
    val new = givenPublishedBatteryLevelConfiguration(revision = 2)

    store.save(old)
    store.save(new)

    // When we query for the current published configurations
    val result = repository.findCurrentPublished()

    // Then we should get only the latest revision
    assertThat(result).hasSize(1)
    assertThat(result.first().id).isEqualTo(new.id.value)
  }

  @Test
  fun `it should return the configuration effective at a given timestamp`() {
    // Given two published revisions with different effective dates
    val revision1 = givenPublishedBatteryLevelConfiguration(
      effectiveFrom = Instant.parse("2026-01-01T00:00:00Z"),
    )
    val revision2 = givenPublishedBatteryLevelConfiguration(
      revision = 2,
      effectiveFrom = Instant.parse("2026-02-01T00:00:00Z"),
    )

    store.save(revision1)
    store.save(revision2)

    // When we query for the configuration effective at a timestamp between the two effective dates
    val result = repository.findPublishedAt(
      ruleId = "BATTERY_LEVEL",
      ruleVersion = 1,
      at = Instant.parse("2026-01-02T00:00:00Z"),
    )

    // Then we should get the first revision
    assertThat(result?.id).isEqualTo(revision1.id.value)
  }
}
