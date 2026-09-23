package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration

import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleDefinition
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.RuleParameters
import java.time.Instant
import java.util.UUID

class RuleConfiguration<P : RuleParameters>
private constructor(
  val ruleDefinition: RuleDefinition<P>,
  val id: RuleConfigurationId,
  val revision: RuleConfigurationRevision,
  parameters: P,
  status: RuleConfigurationStatus,
  val createdAt: Instant,
  val createdBy: String,
  publishedAt: Instant? = null,
  publishedBy: String? = null,
  effectiveFrom: Instant? = null,
) {

  var parameters: P = parameters
    private set

  var status: RuleConfigurationStatus = status
    private set

  var publishedAt: Instant? = publishedAt
    private set

  var publishedBy: String? = publishedBy
    private set

  var effectiveFrom: Instant? = effectiveFrom
    private set

  fun updateParameters(parameters: P) {
    check(status == RuleConfigurationStatus.DRAFT) {
      "Only draft rule configurations can be changed"
    }

    this.parameters = parameters
  }

  fun publish(publishedAt: Instant, publishedBy: String, effectiveFrom: Instant) {
    check(status == RuleConfigurationStatus.DRAFT) {
      "Only draft rule configurations can be published"
    }

    require(publishedBy.isNotBlank()) {
      "Published by cannot be blank"
    }

    status = RuleConfigurationStatus.PUBLISHED

    this.publishedAt = publishedAt
    this.publishedBy = publishedBy
    this.effectiveFrom = effectiveFrom
  }

  companion object {
    fun <P : RuleParameters> createDraft(
      ruleDefinition: RuleDefinition<P>,
      revision: RuleConfigurationRevision,
      parameters: P,
      createdAt: Instant,
      createdBy: String,
    ): RuleConfiguration<P> {
      require(createdBy.isNotBlank()) {
        "Created by must not be blank"
      }

      return RuleConfiguration(
        ruleDefinition = ruleDefinition,
        id = RuleConfigurationId(UUID.randomUUID()),
        revision = revision,
        parameters = parameters,
        status = RuleConfigurationStatus.DRAFT,
        createdAt = createdAt,
        createdBy = createdBy,
      )
    }

    internal fun <P : RuleParameters> rehydrate(
      id: RuleConfigurationId,
      ruleDefinition: RuleDefinition<P>,
      revision: RuleConfigurationRevision,
      parameters: P,
      status: RuleConfigurationStatus,
      createdAt: Instant,
      createdBy: String,
      publishedAt: Instant?,
      publishedBy: String?,
      effectiveFrom: Instant?,
    ): RuleConfiguration<P> = RuleConfiguration(
      ruleDefinition = ruleDefinition,
      id = id,
      revision = revision,
      parameters = parameters,
      status = status,
      createdAt = createdAt,
      createdBy = createdBy,
      publishedAt = publishedAt,
      publishedBy = publishedBy,
      effectiveFrom = effectiveFrom,
    )
  }
}
