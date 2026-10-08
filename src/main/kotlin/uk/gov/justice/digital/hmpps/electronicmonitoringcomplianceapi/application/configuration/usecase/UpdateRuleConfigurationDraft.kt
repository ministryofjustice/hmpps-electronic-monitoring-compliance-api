package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.usecase

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.dto.RuleConfigurationRequest
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.dto.RuleConfigurationResponse
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.mapper.toResponse
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfiguration
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStatus
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStore
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleParameterParser
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleParameters
import java.util.UUID

@Service
class UpdateRuleConfigurationDraft(
  private val store: RuleConfigurationStore,
  private val ruleParameterParser: RuleParameterParser,
) {
  @Transactional
  fun update(
    id: UUID,
    request: RuleConfigurationRequest,
  ): RuleConfigurationResponse {
    val configuration = store.findById(id)
      ?: throw EntityNotFoundException(
        "Rule configuration with id $id not found",
      )

    check(configuration.status == RuleConfigurationStatus.DRAFT) {
      "Only draft rule configurations can be changed"
    }

    return updateConfiguration(configuration, request).toResponse()
  }

  private fun <P : RuleParameters> updateConfiguration(
    configuration: RuleConfiguration<P>,
    request: RuleConfigurationRequest,
  ): RuleConfiguration<out RuleParameters> {
    val parameters = ruleParameterParser.parse(
      configuration.ruleDefinition,
      request.parameters,
    )

    configuration.updateParameters(parameters)

    return store.save(configuration)
  }
}
