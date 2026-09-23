package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.evaluation.RuleEvaluationReason

data class BatteryAtOrBelowThreshold(val actual: BatteryPercentage, val threshold: BatteryPercentage) : RuleEvaluationReason
