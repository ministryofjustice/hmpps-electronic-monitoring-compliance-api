package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.inbound.web.configuration

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.ListRuleConfigurations
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.RuleConfigurationSummary

@RestController
@RequestMapping(value = ["/v1/rule-configurations"])
class RuleConfigurationController(
  private val listRuleConfigurations: ListRuleConfigurations,
) {
  @GetMapping
  fun listRuleConfigurations(): List<RuleConfigurationSummary> = listRuleConfigurations.list()
}
