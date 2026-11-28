package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry

@JvmInline
value class EventId(val value: Long) {
  init {
    require(value > 0) { "Event ID must be greater than zero." }
  }
}
