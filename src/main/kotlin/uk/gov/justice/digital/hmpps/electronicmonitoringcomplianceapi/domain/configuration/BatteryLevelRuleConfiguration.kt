package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleVersion
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRule
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleParameters
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import java.time.Instant
import java.util.UUID

class BatteryLevelRuleConfiguration
private constructor(
  val id: RuleConfigurationId,
  val ruleId: RuleId,
  val ruleVersion: RuleVersion,
  val revision: RuleConfigurationRevision,
  parameters: BatteryLevelRuleParameters,
  status: RuleConfigurationStatus,
  val createdAt: Instant,
  val createdBy: String,
  publishedAt: Instant? = null,
  publishedBy: String? = null,
  effectiveFrom: Instant? = null,
) {

  var parameters: BatteryLevelRuleParameters = parameters
    private set

  var status: RuleConfigurationStatus = status
    private set

  var publishedAt: Instant? = publishedAt
    private set

  var publishedBy: String? = publishedBy
    private set

  var effectiveFrom: Instant? = effectiveFrom
    private set

  fun updateParameters(parameters: BatteryLevelRuleParameters) {
    check(status == RuleConfigurationStatus.DRAFT) {
      "Only draft rule configurations can be changed"
    }

    this.parameters = parameters
  }

  fun publish(publishedAt: Instant, publishedBy: String, effectiveFrom: Instant) {
    check(status == RuleConfigurationStatus.DRAFT) {
      "Only draft rule configurations can be published"
    }

    require(publishedBy.isNotBlank()) {
      "Published by cannot be blank"
    }

    status = RuleConfigurationStatus.PUBLISHED

    this.publishedAt = publishedAt
    this.publishedBy = publishedBy
    this.effectiveFrom = effectiveFrom
  }

  companion object {
    fun createDraft(
      revision: RuleConfigurationRevision,
      parameters: BatteryLevelRuleParameters,
      createdAt: Instant,
      createdBy: String,
    ): BatteryLevelRuleConfiguration {
      require(createdBy.isNotBlank()) {
        "Created by must not be blank"
      }

      return BatteryLevelRuleConfiguration(
        id = RuleConfigurationId(UUID.randomUUID()),
        ruleId = BatteryLevelRule.id,
        ruleVersion = BatteryLevelRuleV1.version,
        revision = revision,
        parameters = parameters,
        status = RuleConfigurationStatus.DRAFT,
        createdAt = createdAt,
        createdBy = createdBy,
      )
    }
  }
}
