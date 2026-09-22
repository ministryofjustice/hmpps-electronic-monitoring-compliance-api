package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration

import jakarta.persistence.EntityExistsException
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceRuleComplianceCounts
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfiguration
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationRevision
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleParameterParser
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleParameters
import java.time.Clock
import java.util.UUID

@Service
class CreateRuleConfigurationDraft(
  private val store: RuleConfigurationStore,
  private val ruleParameterParser: RuleParameterParser,
  private val clock: Clock,
) {
  fun create(
    sourceId: UUID,
    request: RuleConfigurationRequest,
    user: String,
  ): RuleConfigurationResponse {
    val source = store.findById(sourceId)
      ?: throw EntityNotFoundException(
        "Rule configuration with id $sourceId not found",
      )

    require(source.status == RuleConfigurationStatus.PUBLISHED) {
      "A draft can only be created from a published rule configuration"
    }

    if (store.findDraft(source.ruleDefinition) != null) {
      throw EntityExistsException(
        "A draft already exists for ${source.ruleDefinition.id.value} v${source.ruleDefinition.version.value}",
      )
    }

    val draft = createDraft(
      source = source,
      request = request,
      user = user,
    )

    val saved = store.save(draft)

    return RuleConfigurationResponse(
      id = saved.id.value,
      ruleId = saved.ruleDefinition.id.value,
      ruleVersion = saved.ruleDefinition.version.value,
      revision = saved.revision.value,
      parameters = saved.parameters,
      status = saved.status,
      summary = DeviceRuleComplianceCounts(
        compliant = 0,
        nonCompliant = 0,
        noData = 0,
        deactivated = 0,
      ),
    )
  }

  private fun <P : RuleParameters> createDraft(
    source: RuleConfiguration<P>,
    request: RuleConfigurationRequest,
    user: String,
  ): RuleConfiguration<P> = RuleConfiguration.createDraft(
    ruleDefinition = source.ruleDefinition,
    revision = RuleConfigurationRevision(
      source.revision.value + 1,
    ),
    parameters = ruleParameterParser.parse(
      source.ruleDefinition,
      request.parameters,
    ),
    createdAt = clock.instant(),
    createdBy = user,
  )
}
