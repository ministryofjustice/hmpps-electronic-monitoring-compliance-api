package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance.DeviceComplianceStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStore
import java.util.UUID

@Service
class GetRuleConfiguration(
  private val store: RuleConfigurationStore,
  private val complianceStore: DeviceComplianceStore,
) {
  fun get(id: UUID): RuleConfigurationResponse {
    val configuration = store.findById(id) ?: throw EntityNotFoundException(
      "Rule configuration not found: $id",
    )

    val complianceSummary = complianceStore.getRuleComplianceSummary(
      ruleId = configuration.ruleDefinition.id,
      ruleVersion = configuration.ruleDefinition.version,
    )

    return RuleConfigurationResponse(
      id = configuration.id.value,
      ruleId = configuration.ruleDefinition.id.value,
      ruleVersion = configuration.ruleDefinition.version.value,
      revision = configuration.revision.value,
      parameters = configuration.parameters,
      status = configuration.status,
      summary = complianceSummary,
    )
  }
}
