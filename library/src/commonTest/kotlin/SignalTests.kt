package xyz.malefic.emkt

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SignalTests {
    @Test
    fun testEmitAndCollect() =
        runTest {
            val s = Signal<Int>()
            var received: Int? = null
            val started = CompletableDeferred<Unit>()

            val job =
                launch {
                    started.complete(Unit)
                    s.flow.collect { received = it }
                }

            // Wait for collector to start
            started.await()

            s.emit(42)
            // allow collector to run
            delay(50)
            assertEquals(42, received)

            job.cancel()
        }

    @Test
    fun testEmitWithParamsAndWithParamsHelper() =
        runTest {
            val s = Signal<Any?>()
            var received: Any? = null
            val started = CompletableDeferred<Unit>()

            val job =
                launch {
                    started.complete(Unit)
                    s.flow.collect { received = it }
                }

            // Wait for collector to start
            started.await()

            s.emitWithParams("click", mapOf("x" to 1))
            delay(50)

            assertTrue(received is SignalData<*>)
            val sd = received as SignalData<*>
            assertEquals("click", sd.value)
            assertTrue(sd.hasParam("x"))
            assertEquals(1, sd.getParam<Int>("x"))

            job.cancel()
        }

    @Test
    fun testConnectAndCancel() =
        runTest {
            val s = Signal<String>()
            var cnt = 0

            // connect() creates its own scope; wait a short time to ensure collection has started
            val conn = s.connect { _ -> cnt++ }
            delay(20)

            s.emit("a")
            delay(50)
            assertEquals(1, cnt)

            conn.cancel()

            s.emit("b")
            delay(50)
            assertEquals(1, cnt, "Connection should be cancelled and not receive further emissions")
        }
}
