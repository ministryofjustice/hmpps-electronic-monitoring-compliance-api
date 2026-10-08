package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.compliance.dto

data class DeviceComplianceCounts(
  val compliant: Int,
  val nonCompliant: Int,
  val deactivated: Int,
)
