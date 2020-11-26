package com.example.group17_gonogo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.*

class GoNoGoActivity: AppCompatActivity() {

    private lateinit var startButton: Button
    private lateinit var reactionTestView: TextView

    private lateinit var colorGreen: Color
    private lateinit var colorRed: Color
    private lateinit var colorGray: Color

    private val maxWaitTime = 2000          // moved up from startReactionTest

    // May not need this var once thread/other solution implemented
    private var wasClicked = false          // moved up from startReactionTest

    // new variables
    private var startTime: Long = 0
    private var reactTime: Long = 0

    private var goProbability = 0.8f
    private val noGoProbability = 1 - goProbability

    private var mode: GNGMode = GNGMode.NONE
    private var testStatus: TestStatus = TestStatus.TBD

    private var resultList = ArrayList<GNGResult>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.go_no_go)

        // TODO -- Create a button on block three or in the middle of block four
        // If on block three, change text to stop and implement functionality for stopping test
        // If on block three, make it invisible after it is pressed
        startButton = findViewById(R.id.start_button)
        reactionTestView = findViewById(R.id.block_four)

        // May add accessibility option for colorblind users -- if so, will need to change var names
        // because these colors will not be green and red
        colorGreen = Color.valueOf(ContextCompat.getColor(applicationContext, R.color.green))
        colorRed = Color.valueOf(ContextCompat.getColor(applicationContext, R.color.red))
        colorGray = Color.valueOf(ContextCompat.getColor(applicationContext, R.color.gray))

        startButton.setOnClickListener {
            startReactionTest(reactionTestView)
            startButton.isEnabled = false

        }

        reactionTestView.setOnClickListener() {
            getStatus()
        }

    }

    private fun changeColor() {
        var rand = (0..100).random()                                    // generate random number between 0 and 100

        var ngBound = 100 * noGoProbability

        if (rand <= ngBound) {
            mode = GNGMode.NO_GO                                        // record the test is currently showing a No-Go
            reactionTestView.setBackgroundColor(colorRed.toArgb())
            reactionTestView.setText("No Go")
        } else {
            mode = GNGMode.GO                                           // record the test is currently showing a Go
            reactionTestView.setBackgroundColor(colorGreen.toArgb())
            reactionTestView.setText("Go")
        }
    }

    private fun getStatus() {
        if (!wasClicked) {
            reactTime = getElapsedTime(startTime, System.currentTimeMillis())

            if (reactTime < maxWaitTime) {                      // check if the reaction time is less than the time limit

                if (mode == GNGMode.GO) {                       // if the mode is GO
                    testStatus = TestStatus.SUCCESS                 // record the result as SUCCESS
                    reactionTestView.setText("SUCCESS")
                } else if (mode == GNGMode.NO_GO){              // if the mode is NO GO
                    testStatus = TestStatus.FAILED                  // record the result as failed
                    reactionTestView.setText("FAILED")
                } else {
                    // do nothing
                }

            } else {                                            // case when reaction time is >= time limit

                if (mode == GNGMode.GO) {                       // if the mode is GO
                    testStatus = TestStatus.FAILED                  // record the result as failed
                } else if (mode == GNGMode.NO_GO){              // if the mode is NO GO
                    testStatus = TestStatus.SUCCESS                 // record the result as SUCCESS
                } else {
                    // do nothing
                }
            }

            wasClicked = true                                   // changed to true so the user cannot clicked on the same test more than once
        }
    }


    private fun startReactionTest(view: View) {
        val r = Random()

        // Test will be between 8 seconds to 17.5 seconds, as outlined in the project proposal
        // Remember that nextInt() is inclusive to the lower bound and exclusive to the upper,
        // So we add 8000 milliseconds to start at 8 seconds and end at 17.5
        //val testDuration = r.nextInt(10500) + 8000
        //Log.i(TAG, "Test will be $testDuration milliseconds long")

        val testDuration = 20000                    // trial for fixed duration for 10 tests, 2 seconds long per test


        // We set the color to red as the test begins
        // view.setBackgroundColor(colorRed.toArgb())

        // The view will not have an OnClickListener until we begin our subtests
        // view.setOnClickListener(null)          // commented out for testing if we want user to be able to click the view when the button is red

        // NEW CODE: get random color with probability 8:2
        //changeColor()


        // the OnClickListener for the view -- this is enabled during subtests,
        // and currently only uses a boolean to track whether or not the user clicked the view
        val reactionClick = View.OnClickListener {
            wasClicked = true
        }

        // Test is comprised of subtests -- these tests will change the color of the background
        // to the "go" color after a randomly selected interval of 1 to 3 seconds
        // These subtests will be run for testDuration -- this is accomplished with a CountDownTimer

        var timeUntilNextSubtest = generateDuration(r, 3, 1)
        val subTestStart = System.currentTimeMillis()
        var colorChangeStart = Long.MAX_VALUE

        //Log.i(TAG, "First subTestDuration: $timeUntilNextSubtest")
        var currInterval = 0

        // The subtestTimer is nested within testTimer, and is responsible for gauging user reaction
        // times and updating the view accordingly.
        // If the user reacts within this time, the view is set to the "no" color and the timer
        // is interrupted via a cancel() to indicate the user is ready for additional subtests
        // Because the OnClickListener modifies the boolean wasClicked to be true, we need
        // to set wasClicked to false so that future tests can use this variable
        // If the user is not fast enough, the onFinish() will be called: it is here that
        // the view color is changed to "no" automatically as the subtest concluded without any input
        // TODO -- Issue a toast and determine logic for handling subtests that the user failed to react to
        val subtestTimer = object: CountDownTimer(maxWaitTime.toLong(), 2000) {     // interval changed from 1000 to 2000
            override fun onTick(millisUntilFinished: Long) {
                // Log.i(TAG, "Now in subtestTimer")
                //if (wasClicked) {
                //    Log.i(TAG, "User reacted in time!")
                //    view.setBackgroundColor(colorRed.toArgb())
                //    wasClicked = false
                //    view.setOnClickListener(null)
                //    cancel()
                //}

            }

            override fun onFinish() {
                // Log.i(TAG, "Not fast enough!")
                // view.setBackgroundColor(colorRed.toArgb())
                // view.setOnClickListener(null)
            }
        }

        // The overarching CountDownTimer that is the main functionality of this activity.
        // The variable timeUntilNextSubtest dictates how many seconds will pass until the next
        // subtest -- that is, when a number of seconds equal to this variable have passed since
        // the last subtest, the subtestTimer will be started and timeUntilNextSubtest will be
        // regenerated randomly in anticipation of the next subtest.
        // We use the currInterval variable to keep track of the number of seconds elapsed between
        // each subtest; for instance, if timeUntilNextSubtest is 3, that means 3 seconds must
        // elapse until subtestTimer can begin.
        // currInterval starts at 0 and is incremented every 1000 milliseconds in onTick.
        // When currInterval is equal to timeUntilNextSubtest, the required amount of time
        // has passed and the next subtest can begin

        val testTimer = object: CountDownTimer(testDuration.toLong(), 2000) {       // interval changed from 1000 to 2000
            override fun onTick(millisUntilFinished: Long) {
//                Log.i(TAG, "Seconds remaining: " + millisUntilFinished/1000)
                // Log.i(TAG, "Timing for current interval: $currInterval")
                //if (timeUntilNextSubtest.toLong() == (currInterval++).toLong()) {
                    // Log.i(TAG, "Changing to \"go\" color")
                    // This is where the subtest actually begins.
                    // Change the view color to "go" color, and wait for user input.
                    // Create a nested timer that will countdown using maxWaitTime.
                    // If the user reacts in time, cancel() the nested timer
                    // TODO -- record reaction speed for each subtest
                    // If the user is too slow, handle changing view color back to "no" color in onFinish
                //    view.setBackgroundColor(colorGreen.toArgb())
                //    view.setOnClickListener(reactionClick)
                //    subtestTimer.start()                               // starting subtestTimer here, timer will be called every countDownInterval
//                    view.setOnClickListener(null)
                    // Regenerate subTestDuration with a function
                 //   timeUntilNextSubtest = generateDuration(r, 3, 1)
                 //   Log.i(TAG, "New subTestDuration: $timeUntilNextSubtest")
                 //   currInterval = 0
                //}

                if (mode != GNGMode.NONE) {
                    if (testStatus != TestStatus.TBD) {
                        recordScore()                       // record score
                    } else {
                        getStatus()
                        recordScore()                       // record score
                    }
                }
                startTime = System.currentTimeMillis()      // get new startTime
                changeColor()

                // since subtestTimer will finish every countdownInterval, start new subtestTimer
                // might not need this
                // subtestTimer.start()


            }

            override fun onFinish() {
                Log.i(TAG, "Testing Done!")
                view.setBackgroundColor(colorGray.toArgb())
                startButton.isEnabled = true
                startButton.text = "Retry?"
                getStatus()                                 // check if the user pass or fail the last test
                recordScore()                               // record the last test result
                showResult()                                // display the result
            }
        }
        testTimer.start()

        // Below is my initial proof of concept for calculating elapsed time -- this has since been
        // drastically changed but I will leave it here in case anyone (i.e. George) wants to see it

//        var timeElapsed = System.currentTimeMillis()
//        var isGreen = true
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

    private fun recordScore() {
        // Log.i(TAG, "Mode: $mode, Test status: $testStatus")
        var result = GNGResult(reactTime, mode, testStatus)     // Create a GNGResult object with the values obtained from last test
        resultList.add(result)                                  // add result to the arraylist
        testStatus = TestStatus.TBD                             // reset the status for the next test
        wasClicked = false                                      // reset the value for the next test
        goProbability -= 0.02f                                  // increase the chance of No-Go appear as the test goes on, might need to adjust the number later
    }

    private fun showResult() {
        // create intent to pass the result list to popup activity
        val intent = Intent(applicationContext, TestResultPopUp::class.java)
        intent.putExtra("result", resultList)
        startActivity(intent)
    }


    // Simple function to reduce clutter in important computations -- probably do not need this anymore
    private fun getElapsedTime(start: Long, end: Long): Long {
        return (end - start)
    }

    // Another simple function to reduce clutter
    private fun generateDuration(r: Random, bound: Int, offset: Int): Int {
        return r.nextInt(bound) + offset
    }

    companion object {
        const val TAG = "GoNoGo"
    }
}

enum class GNGMode {
    NONE, GO, NO_GO
}

enum class TestStatus {
    TBD, SUCCESS, FAILED
}