package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleParameters

data class BatteryLevelRuleParameters(
  val threshold: BatteryPercentage,
) : RuleParameters
