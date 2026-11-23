package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.activation

@JvmInline
value class DeviceActivationId(
  val value: Long,
) {
  init {
    require(value > 0) { "Device Activation ID must be valid" }
  }
}
