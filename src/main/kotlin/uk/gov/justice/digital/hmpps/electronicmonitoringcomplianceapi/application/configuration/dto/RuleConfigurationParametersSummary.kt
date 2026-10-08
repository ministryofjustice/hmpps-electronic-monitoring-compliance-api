package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.dto

sealed interface RuleConfigurationParametersSummary {
  data class BatteryLevel(
    val threshold: Int,
  ) : RuleConfigurationParametersSummary
}