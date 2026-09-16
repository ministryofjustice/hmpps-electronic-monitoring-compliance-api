package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.configuration

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStatus
import java.util.UUID

interface RuleConfigurationRepository : JpaRepository<RuleConfigurationEntity, UUID> {

  fun findAllByStatus(
    status: RuleConfigurationStatus,
  ): List<RuleConfigurationEntity>
}
