package com.example.group17gonogo

import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.*

class ReactionActivity: AppCompatActivity() {

    private lateinit var startButton: Button
    private lateinit var reactionTestView: TextView

    private lateinit var colorGreen: Color
    private lateinit var colorRed: Color
    private lateinit var colorGray: Color
    private lateinit var colorBlack: Color

    private var mSoundPool: SoundPool? = null
    private var mSoundId: Int = 0
    private lateinit var mAudioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reaction)

        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // If on block three, make it invisible after it is pressed
        startButton = findViewById(R.id.goNoGoStart_button)
        reactionTestView = findViewById(R.id.block_four)

        // May add accessibility option for colorblind users -- if so, will need to change var names
        // because these colors will not be green and red
        colorGreen = Color.valueOf(ContextCompat.getColor(applicationContext, R.color.green))
        colorRed = Color.valueOf(ContextCompat.getColor(applicationContext, R.color.red))
        colorGray = Color.valueOf(ContextCompat.getColor(applicationContext, R.color.gray))
        colorBlack = Color.valueOf(ContextCompat.getColor(applicationContext, R.color.black))

        startButton.setOnClickListener {
            startReactionTest(reactionTestView)
            startButton.isEnabled = false
        }

    }

    private fun startReactionTest(view: View) {
        mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)

        val r = Random()
        val maxWaitTime = 2000

        // Test will be between 8 seconds to 17.5 seconds, as outlined in the project proposal
        // Remember that nextInt() is inclusive to the lower bound and exclusive to the upper,
        // So we add 8000 milliseconds to start at 8 seconds and end at 17.5
        val testDuration = r.nextInt(10500) + 8000
        Log.i(TAG, "Test will be $testDuration milliseconds long")

        // May not need this var once thread/other solution implemented
        var wasClicked = false

        // We set the color to red as the test begins
        view.setBackgroundColor(colorRed.toArgb())
        // The view will not have an OnClickListener until we begin our subtests
        view.setOnClickListener(null)

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

        Log.i(TAG, "First subTestDuration: $timeUntilNextSubtest")
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
        val subtestTimer = object: CountDownTimer(maxWaitTime.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                //Log.i(TAG, "Now in subtestTimer")
                if (wasClicked) {
                    mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
                    Log.i(TAG, "User reacted in time!")
                    view.setBackgroundColor(colorRed.toArgb())
                    wasClicked = false
                    view.setOnClickListener(null)
                    cancel()
                }
            }

            override fun onFinish() {
                Log.i(TAG, "Not fast enough!")
                view.setBackgroundColor(colorRed.toArgb())
                view.setOnClickListener(null)
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
        val testTimer = object: CountDownTimer(testDuration.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
//                Log.i(TAG, "Seconds remaining: " + millisUntilFinished/1000)
//                Log.i(TAG, "Timing for current interval: $currInterval")
                if (timeUntilNextSubtest.toLong() == (currInterval++).toLong()) {
                    Log.i(TAG, "Changing to \"go\" color")
                    // This is where the subtest actually begins.
                    // Change the view color to "go" color, and wait for user input.
                    // Create a nested timer that will countdown using maxWaitTime.
                    // If the user reacts in time, cancel() the nested timer
                    // TODO -- record reaction speed for each subtest
                    // If the user is too slow, handle changing view color back to "no" color in onFinish
                    view.setBackgroundColor(colorGreen.toArgb())
                    view.setOnClickListener(reactionClick)
                    subtestTimer.start()
//                    view.setOnClickListener(null)
                    // Regenerate subTestDuration with a function
                    timeUntilNextSubtest = generateDuration(r, 3, 1)
                    Log.i(TAG, "New subTestDuration: $timeUntilNextSubtest")
                    currInterval = 0
                }

            }
            override fun onFinish() {
                Log.i(TAG, "Testing Done!")
                view.setBackgroundColor(colorGray.toArgb())
                startButton.isEnabled = true
                startButton.text = "Retry?"
            }
        }
        testTimer.start()

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