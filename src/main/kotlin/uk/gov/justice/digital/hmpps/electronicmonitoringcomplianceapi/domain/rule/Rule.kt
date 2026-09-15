package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfiguration
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.evaluation.RuleEvaluation

interface Rule<E, P : RuleParameters> {
  val definition: RuleDefinition<P>

  fun evaluate(event: E, configuration: RuleConfiguration<P>): RuleEvaluation
}
