package com.p5art.yuan.coroutinesstudy

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlin.math.nextDown

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logt(TAG, "activity loaded")

        var jobLaunched: Job? = null

        btLaunch.setOnClickListener {
            logt(TAG, "button Launch tapped")

            /*
             * Launch
             */
            jobLaunched = GlobalScope.launch {
                logt(TAG, "launched job and takes a delay of 3s")
                delay(3_000)
                logt(TAG, "3s passed")

                try {
                    withTimeout(100) {
                        // the following task would be timed out in 100ms
                        var i = 0
                        while (i < 10000 && isActive) { // isActive checks if this job has been cancelled
                            logt(TAG, "doing maths ${i++}")
                        }
                        delay(1) // Only with this delay() function within the withTimeout{} block, can the TimeoutCancellationException be thrown.  Otherwise no exception thrown.
                    }

                    if (isActive) { // the job is not cancelled and the TimeoutCancellationException hasn't been thrown
                        logt(TAG, "maths done")
                    } else { // the job is cancelled
                        logt(TAG, "maths job cancelled")
                    }
                } catch (e: TimeoutCancellationException) { // withTimeout() throw this exception when the time is run out
                    logt(TAG, "maths job timed out")
                }
            }
        }

        btCancelLaunch.setOnClickListener {
            // check if there's job to cancel
            jobLaunched ?: logt(TAG, "no job launched yet")

            // check if the job been has completed
            if (jobLaunched?.isCompleted == true) {
                logt(TAG, "job has been completed")
            }

            // the job is still running - cancel it
            jobLaunched?.let {
                GlobalScope.launch {
                    logt(TAG, "cancelling the job launched")
                    /*
                    * Cancel
                    */
                    it.cancelAndJoin()
                    logt(TAG, "the job launched has been cancelled")
                }
            }
        }

        btAsync.setOnClickListener {
            tvAsyncResult.text = "Started"
            logt(TAG, "About to start async math work")

            val mathWork = GlobalScope.async {
                var i = 1
                repeat(10) {
                    i += i
                    delay(300)
                    logt(TAG, "i = $i")
                }
                i
            }

            var result = 0

            GlobalScope.launch {
                logt(TAG, "waiting for the math work to finish")
                result = mathWork.await()
                println("math work finished result = $result")

                runOnUiThread {
                    logt(TAG, "Got result on UI thread = $result")
                    tvAsyncResult.text = result.toString()
                }
            }
        }

        var j = 0
        btAddOne.setOnClickListener {
            tvAddOneResult.text = (++j).toString()
        }

        btJoin.setOnClickListener {
            runBlocking {
                val job = GlobalScope.launch {
                    // launch a new coroutine and keep a reference to its Job
                    delay(1000L)
                    println("2")
                }
                println("1")
                job.join() // wait until child coroutine completes
                println("3")
            }

            /**
             * runBlocking() is a coroutine builder (launch and async are also coroutine builders)
             * a coroutine builder creates a CoroutineScope, which is the "this" object inside the
             * { } brackets.
             */
            runBlocking {
                /**
                 * launch a new coroutine in the scope of runBlocking (not the GlobalScope)
                 * launch {} = this.launch{}
                 * "this" is the CoroutineScope created by runBlocking()
                 *
                 * Since launch{} is called on the CoroutineScope of the containing runBlocking{}'s,
                 * the containing runBlocking{} block would finish only when the launch{} block finishes
                 */
//                this.launch { // same as just launch {}, but different from GlobalScope.launch {}
                launch {
                    delay(2000L)
                    println("5")
                }

                println("4")
            }

            println("6")
        }

        /**
         * the following example shows how to implicitly "join" a job launched:
         * create a coroutineScope block, and launch a job inside that coroutineScope block
         * then the coroutineScope block will only finish when the job launched inside finishes.
         *
         * I feel this implicit way of joining a job is harder to read and more error prone.
         */
        btCoroutineScope.setOnClickListener {
            GlobalScope.launch {
                // this: CoroutineScope
                launch {
                    delay(900L)
                    println("5")
                }

                /**
                 * This is a scope - once entered, it has to finish everything that is launched with in the scope before
                 * the scope can be exited.  That's why it would print "2", "3", "4" in sequence - it has to wait until
                 * "3" is printed that the scope can be exited and moves on to printing "4"
                 */
                coroutineScope {
                    // Creates a coroutine scope
//              runBlocking{ // runBlocking would do the same here
                    launch {
                        delay(500L)
                        println("3")
                    }

                    delay(100L)
                    println("2") // This line will be printed before the nested launch
                }

                println("4") // This line is not printed until the nested launch completes
            }

            println("1")
        }

        btManyCoroutines.setOnClickListener {
            println("h")

            runBlocking {
                var i = 0
                repeat(100_000) {
                    // launch a lot of coroutines
                    launch {
                        val de = (Math.random() * 1000).toLong()
                        println("delay = $de")
                        delay(de)
                        println(i++)
                    }
                }
            }
        }
    }
}
