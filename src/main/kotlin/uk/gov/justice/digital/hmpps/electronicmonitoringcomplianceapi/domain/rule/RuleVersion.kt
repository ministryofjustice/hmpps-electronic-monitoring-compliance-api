package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule

@JvmInline
value class RuleVersion(val value: Int) {
  init {
    require(value > 0) { "Rule version must be greater than zero." }
  }
}
