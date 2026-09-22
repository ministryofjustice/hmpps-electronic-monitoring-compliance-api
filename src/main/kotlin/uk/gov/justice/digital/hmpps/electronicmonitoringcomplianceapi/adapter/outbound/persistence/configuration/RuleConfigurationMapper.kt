package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.configuration

import org.springframework.stereotype.Component
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfiguration
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationId
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationRevision
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleDefinition
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleParameters
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleParameters
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryPercentage

@Component
class RuleConfigurationMapper(
  private val jsonMapper: JsonMapper,
) {
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

  fun toEntity(
    configuration: RuleConfiguration<out RuleParameters>,
  ): RuleConfigurationEntity = RuleConfigurationEntity(
    id = configuration.id.value,
    ruleId = configuration.ruleDefinition.id.value,
    ruleVersion = configuration.ruleDefinition.version.value,
    revision = configuration.revision.value,
    status = configuration.status,
    parameters = jsonMapper.convertValue(
      configuration.parameters,
      object : TypeReference<Map<String, Any>>() {},
    ),
    createdAt = configuration.createdAt,
    createdBy = configuration.createdBy,
    publishedAt = configuration.publishedAt,
    publishedBy = configuration.publishedBy,
    effectiveFrom = configuration.effectiveFrom,
  )

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
