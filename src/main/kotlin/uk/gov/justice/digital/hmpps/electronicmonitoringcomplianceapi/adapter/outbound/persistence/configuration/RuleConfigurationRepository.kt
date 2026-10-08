package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.configuration

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStatus
import java.time.Instant
import java.util.UUID

interface RuleConfigurationRepository : JpaRepository<RuleConfigurationEntity, UUID> {

  @Query(
    """
      SELECT DISTINCT ON (rule_id, rule_version) *
      FROM rule_configuration
      WHERE status = 'PUBLISHED'
      ORDER BY
          rule_id,
          rule_version,
          effective_from DESC,
          revision DESC;
    """,
    nativeQuery = true,
  )
  fun findCurrentPublished(): List<RuleConfigurationEntity>

  @Query(
    """
      SELECT *
      FROM rule_configuration
      WHERE rule_id = :ruleId
        AND rule_version = :ruleVersion
        AND status = 'PUBLISHED'
        AND effective_from <= :at
      ORDER BY effective_from DESC, revision DESC
      LIMIT 1
    """,
    nativeQuery = true,
  )
  fun findPublishedAt(
    ruleId: String,
    ruleVersion: Int,
    at: Instant,
  ): RuleConfigurationEntity?

  fun findAllByStatus(
    status: RuleConfigurationStatus,
  ): List<RuleConfigurationEntity>

  fun findByRuleIdAndRuleVersionAndStatus(
    ruleId: String,
    ruleVersion: Int,
    status: RuleConfigurationStatus,
  ): RuleConfigurationEntity?
}
