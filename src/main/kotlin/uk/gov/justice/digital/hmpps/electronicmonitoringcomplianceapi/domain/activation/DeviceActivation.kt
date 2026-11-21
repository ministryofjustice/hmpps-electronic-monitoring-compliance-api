package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.activation

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import java.time.Instant

data class DeviceActivation(
  val id: DeviceActivationId,
  val deviceId: DeviceId,
  val activationDate: Instant,
  val deactivationDate: Instant?,
)
