package reagent.source

import org.junit.Assert.assertTrue
import org.junit.Test
import reagent.runTest
import reagent.tester.testOne
import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicBoolean

class OnePlatformSourceTest {
  @Test
  fun fromCallable() = runTest {
    val called = AtomicBoolean()
    Callable { called.getAndSet(true) }
        .asOne()
        .testOne {
          item(false)
        }
    assertTrue(called.get())
  }

  @Test
  fun fromCallableThrowing() = runTest {
    val exception = RuntimeException("Oops!")
    Callable { throw exception }
        .asOne()
        .testOne {
          error(exception)
        }
  }
}