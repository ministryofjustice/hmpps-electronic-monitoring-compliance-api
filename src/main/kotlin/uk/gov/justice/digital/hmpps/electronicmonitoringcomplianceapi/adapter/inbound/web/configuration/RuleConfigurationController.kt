package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.inbound.web.configuration

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.CreateRuleConfigurationDraft
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.GetRuleConfiguration
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.ListRuleConfigurations
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.RuleConfigurationRequest
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.RuleConfigurationResponse
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.RuleConfigurationSummary
import java.util.UUID

@RestController
@RequestMapping(value = ["/v1/rule-configurations"])
class RuleConfigurationController(
  private val createRuleConfigurationDraft: CreateRuleConfigurationDraft,
  private val getRuleConfiguration: GetRuleConfiguration,
  private val listRuleConfigurations: ListRuleConfigurations,
) {
  @GetMapping("/{ruleConfigurationId}")
  fun getRuleConfiguration(
    @PathVariable("ruleConfigurationId")
    ruleConfigurationId: UUID,
  ): RuleConfigurationResponse = getRuleConfiguration.get(ruleConfigurationId)

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
