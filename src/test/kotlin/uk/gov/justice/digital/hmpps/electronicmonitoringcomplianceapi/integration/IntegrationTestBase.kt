package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration

import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.PostgreSQLContainer
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.config.TestAwsConfiguration
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.wiremock.AthenaColumn
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.wiremock.AwsApiExtension
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.wiremock.AwsApiExtension.Companion.awsMockServer
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper

@ExtendWith(HmppsAuthApiExtension::class, AwsApiExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(TestAwsConfiguration::class)
abstract class IntegrationTestBase {

  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  @BeforeEach
  fun setupBase() {
    awsMockServer.stubStsAssumeRole()
  }

  internal fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  protected fun stubPingWithResponse(status: Int) {
    hmppsAuth.stubHealthPing(status)
  }

  protected fun stubQueryExecution(
    queryExecutionId: String = "query-1",
    retryCount: Int = 1,
    finalQueryExecutionStatus: String = "SUCCEEDED",
    columns: List<AthenaColumn> = emptyList(),
    rows: List<List<String>> = emptyList(),
  ) {
    awsMockServer.stubAthenaStartQueryExecution(queryExecutionId)
    awsMockServer.stubAthenaGetQueryExecution(
      retryCount,
      finalQueryExecutionStatus,
    )
    awsMockServer.stubAthenaGetQueryResults(columns, rows)
  }

  protected fun verifyAthenaStartQueryExecutionCount(
    count: Int,
  ) {
    awsMockServer.verify(
      count,
      postRequestedFor(urlPathEqualTo("/"))
        .withHeader("X-Amz-Target", equalTo("AmazonAthena.StartQueryExecution")),
    )
  }

  protected fun verifyAthenaGetQueryExecutionCount(
    count: Int,
  ) {
    awsMockServer.verify(
      count,
      postRequestedFor(urlPathEqualTo("/"))
        .withHeader("X-Amz-Target", equalTo("AmazonAthena.GetQueryExecution")),
    )
  }

  protected fun verifyAthenaGetQueryResultsCount(
    count: Int,
  ) {
    awsMockServer.verify(
      count,
      postRequestedFor(urlPathEqualTo("/"))
        .withHeader("X-Amz-Target", equalTo("AmazonAthena.GetQueryResults")),
    )
  }

  protected fun verifyAthenaStartQueryExecutionWithQuery(
    query: String,
    executionParameters: List<String>,
  ) {
    val requestPattern = postRequestedFor(urlPathEqualTo("/"))
      .withHeader("X-Amz-Target", equalTo("AmazonAthena.StartQueryExecution"))
      .withRequestBody(
        matchingJsonPath("QueryString", equalTo(query)),
      )

    for (executionParameter in executionParameters) {
      requestPattern.withRequestBody(
        matchingJsonPath("$.ExecutionParameters[?(@ == \"$executionParameter\")]"),
      )
    }

    awsMockServer.verify(
      1,
      requestPattern,
    )
  }

  companion object {
    @JvmStatic
    private val postgresContainer = PostgreSQLContainer<Nothing>("postgres:18")
      .apply {
        withUsername("postgres")
        withPassword("postgres")
        withDatabaseName("testdb")
        withReuse(true)
        start()
      }

    @JvmStatic
    @DynamicPropertySource
    fun properties(registry: DynamicPropertyRegistry) {
      registry.add("spring.datasource.url") { postgresContainer.jdbcUrl }
      registry.add("spring.datasource.username") { postgresContainer.username }
      registry.add("spring.datasource.password") { postgresContainer.password }
    }
  }
}
