package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule

data class RuleDefinition<P : RuleParameters>(
  val id: RuleId,
  val version: RuleVersion,
)
