package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration

sealed interface RuleConfigurationParametersSummary {
  data class BatteryLevel(
    val threshold: Int,
  ) : RuleConfigurationParametersSummary
}
