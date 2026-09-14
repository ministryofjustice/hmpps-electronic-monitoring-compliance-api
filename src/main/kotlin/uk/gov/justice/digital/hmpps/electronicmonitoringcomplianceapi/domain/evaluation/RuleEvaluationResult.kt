package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.evaluation

sealed interface RuleEvaluationResult {
  data object Compliant : RuleEvaluationResult

  data class NonCompliant(val reason: RuleEvaluationReason) : RuleEvaluationResult
}
