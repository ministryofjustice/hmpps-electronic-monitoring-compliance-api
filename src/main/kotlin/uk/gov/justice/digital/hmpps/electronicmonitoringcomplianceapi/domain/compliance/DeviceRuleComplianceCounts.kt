package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance

data class DeviceRuleComplianceCounts(
  val compliant: Long,
  val nonCompliant: Long,
  val noData: Long,
  val deactivated: Long,
)
