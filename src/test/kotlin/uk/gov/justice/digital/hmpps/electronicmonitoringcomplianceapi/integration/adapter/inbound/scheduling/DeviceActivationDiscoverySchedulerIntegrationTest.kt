
package uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.adapter.inbound.scheduling

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.adapter.inbound.scheduling.DeviceActivationDiscoveryScheduler
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.application.activation.DiscoverDeviceActivations
import uk.gov.justice.digital.hmpps.electronicmonitoringcomplianceapi.integration.IntegrationTestBase
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class DeviceActivationDiscoverySchedulerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var scheduler: DeviceActivationDiscoveryScheduler

  @MockitoBean
  private lateinit var discoverDeviceActivations: DiscoverDeviceActivations

  @Test
  fun `only one concurrent invocation executes discovery`() {
    val started = CountDownLatch(1)
    val release = CountDownLatch(1)
    val executions = AtomicInteger(0)

    doAnswer {
      executions.incrementAndGet()
      started.countDown()

      check(release.await(10, TimeUnit.SECONDS)) {
        "Timed out waiting to release discovery"
      }

      null
    }.whenever(discoverDeviceActivations).execute()

    val executor = Executors.newFixedThreadPool(2)

    try {
      val first = executor.submit {
        scheduler.discover()
      }

      assertThat(started.await(5, TimeUnit.SECONDS)).isTrue()

      // Attempt discovery while the first invocation holds the lock.
      val second = executor.submit {
        scheduler.discover()
      }

      // ShedLock should skip this invocation rather than block.
      second.get(5, TimeUnit.SECONDS)

      assertThat(executions.get()).isEqualTo(1)

      release.countDown()
      first.get(5, TimeUnit.SECONDS)

      // After release, a subsequent invocation should execute.
      scheduler.discover()

      verify(discoverDeviceActivations, times(2)).execute()
    } finally {
      release.countDown()
      executor.shutdownNow()
    }
  }
}
