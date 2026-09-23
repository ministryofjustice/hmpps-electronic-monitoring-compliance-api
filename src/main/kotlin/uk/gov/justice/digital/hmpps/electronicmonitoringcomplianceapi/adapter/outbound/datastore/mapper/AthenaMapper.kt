package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.outbound.datastore.mapper

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

abstract class AthenaMapper {
  protected fun parseAthenaTimestamp(
    value: String,
  ): Instant = LocalDateTime
    .parse(value, ATHENA_TIMESTAMP_FORMATTER)
    .toInstant(ZoneOffset.UTC)

  protected fun Map<String, String?>.required(
    column: String,
  ): String = this[column]
    ?.takeIf { it.isNotBlank() }
    ?: throw IllegalArgumentException(
      "Athena result is missing $column",
    )

  companion object {
    private val ATHENA_TIMESTAMP_FORMATTER: DateTimeFormatter =
      DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd HH:mm:ss")
        .optionalStart()
        .appendFraction(
          ChronoField.NANO_OF_SECOND,
          0,
          9,
          true,
        )
        .optionalEnd()
        .toFormatter()
  }
}
