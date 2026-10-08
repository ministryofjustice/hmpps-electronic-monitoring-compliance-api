package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.usecase

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.dto.RuleConfigurationResponse
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.configuration.mapper.toResponse
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.configuration.RuleConfigurationStore
import java.time.Clock
import java.util.UUID

@Service
class PublishRuleConfiguration(
  private val store: RuleConfigurationStore,
  private val clock: Clock,
) {
  @Transactional
  fun publish(
    id: UUID,
    user: String,
  ): RuleConfigurationResponse {
    val configuration = store.findById(id)
      ?: throw EntityNotFoundException(
        "Rule configuration with id $id not found",
      )
    val currentTime = clock.instant()

    configuration.publish(
      publishedAt = currentTime,
      publishedBy = user,
      effectiveFrom = currentTime,
    )

    return store.save(configuration).toResponse()
  }
}
