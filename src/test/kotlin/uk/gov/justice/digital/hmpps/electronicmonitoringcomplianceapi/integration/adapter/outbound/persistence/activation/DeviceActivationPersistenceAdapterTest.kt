package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.adapter.outbound.persistence.activation

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.activation.DeviceActivationRepository
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.activation.DeviceActivation
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.activation.DeviceActivationId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.activation.DeviceActivationStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.IntegrationTestBase
import java.time.Instant

class DeviceActivationPersistenceAdapterTest : IntegrationTestBase() {

  @Autowired
  private lateinit var store: DeviceActivationStore

  @Autowired
  private lateinit var repository: DeviceActivationRepository

  @BeforeEach
  fun setUp() {
    repository.deleteAll()
    repository.flush()
  }

  @Test
  fun `it should persist a new activation`() {
    val activation = createActivation()

    store.upsert(activation)

    val saved = repository.findAll().single()

    assertThat(saved.deviceId).isEqualTo(123L)
    assertThat(saved.activationDate).isEqualTo(activation.activationDate)
    assertThat(saved.deactivationDate).isNull()
  }

  @Test
  fun `it should not duplicate an existing activation`() {
    val activation = createActivation()

    store.upsert(activation)
    store.upsert(activation)

    assertThat(repository.count()).isEqualTo(1)
  }

  @Test
  fun `it should update the deactivation timestamp`() {
    val activation = createActivation()

    store.upsert(activation)
    store.upsert(
      activation.copy(
        deactivationDate =
        Instant.parse("2026-01-02T10:00:00Z"),
      ),
    )

    val saved = repository.findAll().single()

    assertThat(saved.deactivationDate).isEqualTo(
      Instant.parse("2026-01-02T10:00:00Z"),
    )
  }

  @Test
  fun `it should preserve activation identity when updating`() {
    val activation = createActivation()

    store.upsert(activation)

    val originalId = repository.findAll().single().id

    store.upsert(
      activation.copy(
        deactivationDate =
        Instant.parse("2026-01-02T10:00:00Z"),
      ),
    )

    assertThat(repository.findAll().single().id)
      .isEqualTo(originalId)
  }

  @Test
  fun `it should support multiple activations for the same device`() {
    store.upsert(
      createActivation(
        id = 1L,
        activationDate =
        Instant.parse("2026-01-01T10:00:00Z"),
      ),
    )

    store.upsert(
      createActivation(
        id = 2L,
        activationDate =
        Instant.parse("2026-02-01T10:00:00Z"),
      ),
    )

    assertThat(repository.count()).isEqualTo(2)
  }

  @Test
  fun `it should reject a deactivation date before activation date`() {
    val invalid = createActivation(
      deactivationDate =
      Instant.parse("2025-12-31T10:00:00Z"),
    )

    assertThatThrownBy {
      store.upsert(invalid)
      repository.flush()
    }.isInstanceOf(DataIntegrityViolationException::class.java)
  }

  private fun createActivation(
    id: Long = 1L,
    activationDate: Instant =
      Instant.parse("2026-01-01T10:00:00Z"),
    deactivationDate: Instant? = null,
  ) = DeviceActivation(
    id = DeviceActivationId(id),
    deviceId = DeviceId(123),
    activationDate = activationDate,
    deactivationDate = deactivationDate,
  )
}
