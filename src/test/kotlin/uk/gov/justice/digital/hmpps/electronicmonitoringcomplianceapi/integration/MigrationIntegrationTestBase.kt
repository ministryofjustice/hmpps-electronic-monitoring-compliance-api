package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
abstract class MigrationIntegrationTestBase {

  companion object {
    @JvmStatic
    private val postgresContainer = PostgreSQLContainer<Nothing>("postgres:18")
      .apply {
        withUsername("postgres")
        withPassword("postgres")
        withDatabaseName("testdb")
        withReuse(false)
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
