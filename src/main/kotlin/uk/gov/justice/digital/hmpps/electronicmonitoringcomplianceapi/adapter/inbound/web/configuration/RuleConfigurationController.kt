package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.inbound.web.configuration

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.usecase.CreateRuleConfigurationDraft
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.usecase.GetRuleConfiguration
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.usecase.GetRuleConfigurationDraft
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.usecase.ListRuleConfigurations
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.dto.RuleConfigurationRequest
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.dto.RuleConfigurationResponse
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.dto.RuleConfigurationSummary
import java.util.UUID

@RestController
@RequestMapping(value = ["/v1/rule-configurations"])
class RuleConfigurationController(
  private val createRuleConfigurationDraft: CreateRuleConfigurationDraft,
  private val getRuleConfiguration: GetRuleConfiguration,
  private val getRuleConfigurationDraft: GetRuleConfigurationDraft,
  private val listRuleConfigurations: ListRuleConfigurations,
) {
  @GetMapping("/{ruleConfigurationId}")
  fun getRuleConfiguration(
    @PathVariable("ruleConfigurationId")
    ruleConfigurationId: UUID,
  ): RuleConfigurationResponse = getRuleConfiguration.get(ruleConfigurationId)

  @GetMapping("/{ruleConfigurationId}/draft")
  fun getRuleConfigurationDraft(
    @PathVariable("ruleConfigurationId")
    ruleConfigurationId: UUID,
  ): RuleConfigurationResponse = getRuleConfigurationDraft.get(ruleConfigurationId)

  @PostMapping("/{ruleConfigurationId}/draft")
  fun createRuleConfigurationDraft(
    @PathVariable("ruleConfigurationId")
    ruleConfigurationId: UUID,
    @RequestBody request: RuleConfigurationRequest,
    authentication: Authentication,
  ): ResponseEntity<RuleConfigurationResponse> {
    val configuration = createRuleConfigurationDraft.create(ruleConfigurationId, request, authentication.name)

    return ResponseEntity.status(201).body(
      configuration,
    )
  }

  @GetMapping
  fun listRuleConfigurations(): List<RuleConfigurationSummary> = listRuleConfigurations.list()
}
