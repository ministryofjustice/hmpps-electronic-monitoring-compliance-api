package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery

@JvmInline
value class BatteryPercentage(val value: Int) {
  init {
    require(value in 0..100) { "Battery percentage must be between 0 and 100." }
  }
}
