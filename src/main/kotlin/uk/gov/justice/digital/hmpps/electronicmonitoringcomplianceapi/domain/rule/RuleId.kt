package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule

@JvmInline
value class RuleId(val value: String) {
  init {
    require(value.isNotBlank()) { "Rule ID must not be blank" }
  }
}
