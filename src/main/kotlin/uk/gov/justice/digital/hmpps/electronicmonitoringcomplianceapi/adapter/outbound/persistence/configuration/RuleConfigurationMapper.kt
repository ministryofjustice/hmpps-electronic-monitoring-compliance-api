package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.configuration

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfiguration
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationRevision
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleDefinition
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleParameters
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleParameters
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryPercentage

@Component
class RuleConfigurationMapper {
  fun toDomain(
    entity: RuleConfigurationEntity,
  ): RuleConfiguration<out RuleParameters> = when {
    entity.matches(BatteryLevelRuleV1.ruleDefinition) ->
      toBatteryLevelRuleConfiguration(entity)

    else ->
      throw IllegalArgumentException(
        "Unknown rule definition: ${entity.ruleId} v${entity.ruleVersion}",
      )
  }

  private fun toBatteryLevelRuleConfiguration(
    entity: RuleConfigurationEntity,
  ): RuleConfiguration<BatteryLevelRuleParameters> {
    val threshold = entity.parameters["threshold"] as? Number
      ?: throw IllegalArgumentException(
        "Battery level configuration threshold must be numeric",
      )

    return RuleConfiguration.rehydrate(
      id = RuleConfigurationId(entity.id),
      ruleDefinition = BatteryLevelRuleV1.ruleDefinition,
      revision = RuleConfigurationRevision(entity.revision),
      parameters = BatteryLevelRuleParameters(
        threshold = BatteryPercentage(
          (threshold).toInt(),
        ),
      ),
      status = entity.status,
      createdAt = entity.createdAt,
      createdBy = entity.createdBy,
      publishedAt = entity.publishedAt,
      publishedBy = entity.publishedBy,
      effectiveFrom = entity.effectiveFrom,
    )
  }

  private fun RuleConfigurationEntity.matches(
    definition: RuleDefinition<*>,
  ) = ruleId == definition.id.value &&
    ruleVersion == definition.version.value
}
