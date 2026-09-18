package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.compliance

enum class ComplianceState {
  COMPLIANT,
  NON_COMPLIANT,
  NO_DATA, // Initial state, no evaluations have been processed
}
