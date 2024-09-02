package tree.ralph.mindmapmemo

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Test

import org.junit.Assert.*
import kotlin.random.Random

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun `coroutine in scope function test`() {
       runBlocking {
           println("111")
           val job = launch {
               when (op()) {
                   0 -> doSth(0)
                   1 -> doSth(1)
                   2 -> doSth(2)
                   3 -> doSth(3)
                   else -> doSth(-1)
               }
           }
           println("222")
           job.join()
           println("333")
       }
    }

    private suspend fun op(): Int {
        delay(500)
        return Random.nextInt() % 4
    }

    private suspend fun doSth(target: Int) {
        delay(500)
        println(target)
    }
}