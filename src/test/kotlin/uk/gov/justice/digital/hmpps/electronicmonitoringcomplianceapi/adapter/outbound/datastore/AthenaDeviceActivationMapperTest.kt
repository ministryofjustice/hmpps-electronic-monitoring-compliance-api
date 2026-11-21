package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.datastore

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.datastore.mapper.AthenaDeviceActivationMapper
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.activation.DeviceActivationId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import java.time.Instant

class AthenaDeviceActivationMapperTest {

  private val mapper = AthenaDeviceActivationMapper()

  @Test
  fun `it should map an active device activation`() {
    val result = mapper.map(
      createAthenaQueryResultRow(),
    )

    assertThat(result.id).isEqualTo(DeviceActivationId(1L))
    assertThat(result.deviceId).isEqualTo(DeviceId(123))
    assertThat(result.activationDate).isEqualTo(
      Instant.parse("2026-01-01T10:00:00Z"),
    )
    assertThat(result.deactivationDate).isNull()
  }

  @Test
  fun `it should map a completed device activation`() {
    val result = mapper.map(
      createAthenaQueryResultRow(
        deactivationDate = "2026-01-02 11:30:00.000000",
      ),
    )

    assertThat(result.deactivationDate).isEqualTo(
      Instant.parse("2026-01-02T11:30:00Z"),
    )
  }

  @Test
  fun `it should reject an activation without a device id`() {
    val row = createAthenaQueryResultRow().minus("device_id")

    assertThatThrownBy { mapper.map(row) }
      .isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `it should reject an invalid activation timestamp`() {
    val row = createAthenaQueryResultRow() + (
      "device_activation_date" to "invalid"
      )

    assertThatThrownBy { mapper.map(row) }
      .isInstanceOf(RuntimeException::class.java)
  }

  private fun createAthenaQueryResultRow(
    deactivationDate: String = "",
  ): Map<String, String?> = mapOf(
    "device_activation_id" to "1",
    "device_id" to "123",
    "device_activation_date" to "2026-01-01 10:00:00.000000",
    "device_deactivation_date" to deactivationDate,
  )
}
