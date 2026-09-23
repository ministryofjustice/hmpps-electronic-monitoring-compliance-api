package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.persistence.configuration

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStatus
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "rule_configuration")
class RuleConfigurationEntity(
  @Id
  val id: UUID,

  @Column(name = "rule_id", nullable = false)
  val ruleId: String,

  @Column(name = "rule_version", nullable = false)
  val ruleVersion: Int,

  @Column(name = "revision", nullable = false)
  val revision: Int,

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  val status: RuleConfigurationStatus,

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "parameters", columnDefinition = "jsonb", nullable = false)
  val parameters: Map<String, Any>,

  @Column(name = "created_at", nullable = false)
  val createdAt: Instant,

  @Column(name = "created_by", nullable = false)
  val createdBy: String,

  @Column(name = "published_at")
  val publishedAt: Instant?,

  @Column(name = "published_by")
  val publishedBy: String?,

  @Column(name = "effective_from")
  val effectiveFrom: Instant?,
)
