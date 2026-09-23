package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleParameters
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryLevelRuleV1
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.domain.rule.battery.BatteryPercentage

class RuleParameterParserTest {

  private val parser = RuleParameterParser()

  @Test
  fun `it should parse battery level parameters`() {
    val result =
      parser.parse(
        ruleDefinition =
        BatteryLevelRuleV1.ruleDefinition,
        parameters = mapOf(
          "threshold" to 20,
        ),
      )

    assertThat(result)
      .isEqualTo(
        BatteryLevelRuleParameters(
          threshold = BatteryPercentage(20),
        ),
      )
  }

  @Test
  fun `it should accept numeric threshold values`() {
    val result =
      parser.parse(
        ruleDefinition =
        BatteryLevelRuleV1.ruleDefinition,
        parameters = mapOf(
          "threshold" to 20L,
        ),
      )

    assertThat(result.threshold)
      .isEqualTo(BatteryPercentage(20))
  }

  @Test
  fun `it should reject missing threshold`() {
    assertThatThrownBy {
      parser.parse(
        ruleDefinition =
        BatteryLevelRuleV1.ruleDefinition,
        parameters = emptyMap(),
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage(
        "Missing or invalid threshold parameter",
      )
  }

  @Test
  fun `it should reject non numeric threshold`() {
    assertThatThrownBy {
      parser.parse(
        ruleDefinition =
        BatteryLevelRuleV1.ruleDefinition,
        parameters = mapOf(
          "threshold" to "twenty",
        ),
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage(
        "Missing or invalid threshold parameter",
      )
  }
}
