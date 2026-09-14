package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry

@JvmInline
value class DeviceId(val value: Int) {
  init {
    require(value > 0) { "Device ID must be valid" }
  }
}
