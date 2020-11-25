package com.example.group17_gonogo

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.*

class GoNoGoActivity: AppCompatActivity() {

    private lateinit var startButton: TextView
    private lateinit var viewToClick: TextView

    private lateinit var colorGreen: Color
    private lateinit var colorRed: Color

    private var isGreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.go_no_go)

        // TODO -- Create a button on block three or in the middle of block four
        // If on block three, change text to stop and implement functionality for stopping test
        // If on block three, make it invisible after it is pressed
        startButton = findViewById(R.id.block_three)
        viewToClick = findViewById(R.id.block_four)

        // May add accessibility option for colorblind users -- if so, will need to change var names
        // because these colors will not be green and red
        colorGreen = Color.valueOf(ContextCompat.getColor(applicationContext, R.color.green))
        colorRed = Color.valueOf(ContextCompat.getColor(applicationContext, R.color.red))




        startButton.setOnClickListener {
            startReactionTest(viewToClick)
        }
    }

    private fun startReactionTest(view: View) {
        val r = Random()
        val maxWaitTime = 2000

        val testDuration = r.nextInt(10500) + 8000
        Log.i(TAG, "Test will be $testDuration milliseconds long")

        var subTestDuration = 0
        // May not need this var once thread/other solution implemented
        var wasClicked = false

        view.setBackgroundColor(colorRed.toArgb())
        view.setOnClickListener(null)

        val myClick = View.OnClickListener {
            wasClicked = true
        }



        // Test is comprised of subtests -- these tests will change the color of the background
        // to the "go" color between a randomly selected interval of 1 to 3 seconds
        // These subtests will be run until the time between testStart and the current time
        // is greater than or equal to testDuration
            val subTestStart = System.currentTimeMillis()
            var colorChangeStart = Long.MAX_VALUE
            subTestDuration = generateDuration(r, 3, 1)
            Log.i(TAG, "First subTestDuration: $subTestDuration")
            var currInterval = 0

        val subtestTimer = object: CountDownTimer(maxWaitTime.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.i(TAG, "Now in subtestTimer")
                if (wasClicked) {
                    Log.i(TAG, "User reacted in time!")
                    view.setBackgroundColor(colorRed.toArgb())
                    wasClicked = false
                    cancel()
                }
            }

            override fun onFinish() {
                Log.i(TAG, "Not fast enough!")
                view.setBackgroundColor(colorRed.toArgb())
            }
        }

        val testTimer = object: CountDownTimer(testDuration.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
//                Log.i(TAG, "Seconds remaining: " + millisUntilFinished/1000)
//                Log.i(TAG, "Timing for current interval: $currInterval")
                if (subTestDuration.toLong() == (currInterval++).toLong()) {
                    Log.i(TAG, "Changing to \"go\" color")
                    // TODO -- This is where the subtest actually begins
                    // Change the view color to "go" color, and wait for user input
                    // Create a nested timer that will countdown using maxWaitTime
                    // If the user reacts in time, cancel the nested timer and record reaction speed
                    // If not, handle changing view color back to "no" color in onFinish
                    view.setBackgroundColor(colorGreen.toArgb())
                    subtestTimer.start()
                    view.setOnClickListener(myClick)
                    // Regenerate subTestDuration with a function
                    subTestDuration = generateDuration(r, 3, 1)
                    Log.i(TAG, "New subTestDuration: $subTestDuration")
                    currInterval = 0
                }

            }
            override fun onFinish() {
                Log.i(TAG, "Testing Done!")
            }
        }
        testTimer.start()

        // Below is my initial proof of concept for calculating elapsed time -- this has since been
        // drastically changed but I will leave it here in case anyone (i.e. George) wants to see it
        
//        var timeElapsed = System.currentTimeMillis()
//        var isGreen = false
//        Log.i(TAG, "Initial time is $timeElapsed ms")
//        isGreen = if (isGreen) {
//            view.setBackgroundColor(colorRed.toArgb())
//            Log.i(TAG, "Setting color to red/\"no\"")
//            Log.i(TAG, "Time at press was ${System.currentTimeMillis()}")
//            timeElapsed = System.currentTimeMillis() - timeElapsed
//            Log.i(TAG, "Time elapsed from green to red was $timeElapsed ms")
////                Toast.makeText(applicationContext, "User took $timeElapsed to click screen", Toast.LENGTH_LONG).show()
//            false
//        } else {
//            view.setBackgroundColor(colorGreen.toArgb())
//            Log.i(TAG, "Setting color to green/\"go\"")
//            Log.i(TAG, "Time at press was ${System.currentTimeMillis()} ms")
//            timeElapsed = System.currentTimeMillis()
//            true
//        }

    }

    private fun getElapsedTime(start: Long, end: Long): Long {
        return (end - start)
    }

    private fun generateDuration(r: Random, bound: Int, offset: Int): Int {
        return r.nextInt(bound) + offset
    }

    companion object {
        const val TAG = "GoNoGo"
    }
}