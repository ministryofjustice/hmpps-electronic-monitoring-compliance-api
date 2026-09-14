package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery

data class BatteryLevelRuleParameters(
  val threshold: BatteryPercentage,
)
