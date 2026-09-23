package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration

@JvmInline
value class RuleConfigurationRevision(val value: Int) {
  init {
    require(value > 0) { "Rule configuration revision must be greater than zero." }
  }
}
