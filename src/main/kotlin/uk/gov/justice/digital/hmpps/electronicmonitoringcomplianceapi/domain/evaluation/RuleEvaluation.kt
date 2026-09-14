package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.evaluation

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationRevision
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleVersion
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.DeviceId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.telemetry.EventId
import java.time.Instant

// Records the evaluation of a rule being applied to one telemetry event
// with a particular rule configuration
data class RuleEvaluation(
  val deviceId: DeviceId,
  val eventId: EventId,
  val recordedAt: Instant,
  val ruleId: RuleId,
  val ruleVersion: RuleVersion,
  val configurationId: RuleConfigurationId,
  val configurationRevision: RuleConfigurationRevision,
  val result: RuleEvaluationResult,
)
