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
//import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth
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

//    private lateinit var databaseScores: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.go_no_go)

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

        // Test will be between 8 seconds to 17.5 seconds, as outlined in the project proposal
        // Remember that nextInt() is inclusive to the lower bound and exclusive to the upper,
        // So we add 8000 milliseconds to start at 8 seconds and end at 17.5
        //val testDuration = r.nextInt(10500) + 8000
        //Log.i(TAG, "Test will be $testDuration milliseconds long")

        val testDuration = 20000                    // trial for fixed duration for 10 tests, 2 seconds long per test



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

    private fun addScore() {

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