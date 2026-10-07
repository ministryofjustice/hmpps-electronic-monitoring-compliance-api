package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStore
import java.util.UUID

@Service
class GetRuleConfigurationDraft(
  private val store: RuleConfigurationStore,
) {
  fun get(
    sourceId: UUID,
  ): RuleConfigurationResponse {
    val source = store.findById(sourceId)
      ?: throw EntityNotFoundException(
        "Rule configuration with id $sourceId not found",
      )

    require(source.status == RuleConfigurationStatus.PUBLISHED) {
      "A draft can only be retrieved for a published rule configuration"
    }

    val draft = store.findDraft(
      source.ruleDefinition,
    ) ?: throw EntityNotFoundException(
      "No draft rule configuration exists for ${source.ruleDefinition.id.value} v${source.ruleDefinition.version.value}",
    )

    return draft.toResponse()
  }
}
