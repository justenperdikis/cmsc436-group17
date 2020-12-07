package com.example.group17gonogo

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class ReactionActivity: AppCompatActivity() {

    // views
    private lateinit var startButton: Button
    private lateinit var reactionTestView: TextView
    private lateinit var instructionView: TextView
    private lateinit var recentScoreView: TextView

    // colors
    private lateinit var colorGreen: Color
    private lateinit var colorRed: Color
    private lateinit var colorGray: Color
    private lateinit var colorBlack: Color

    // test var
    var wasClicked = false
    private var startTime: Long = 0
    private var resultList = ArrayList<GNGResult>()

    // sounds var
    private lateinit var mAudioManager: AudioManager

    // database
    private var mAuth: FirebaseAuth? = null
    private val mRootRef = FirebaseDatabase.getInstance().reference
    private val mReactionScoreRef = mRootRef.child("reactionScores")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reaction)

        // obtain audio manager
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // obtain views
        startButton = findViewById(R.id.goNoGoStart_button)
        reactionTestView = findViewById(R.id.block_four)
        instructionView = findViewById(R.id.block_two)
        recentScoreView = findViewById(R.id.block_three)

        // obtain instance of FirebaseAuth
        mAuth = FirebaseAuth.getInstance()

        // set the instructionView text to the proper instruction string
        instructionView.setText(R.string.ReactionTest_instructions)

        // display the player's latest score in highScoreView
        displayScore()

        //Set colors depending on light or dark mode
        setColors()

        // add an onClickListener to startButton that calls startReactionTest
        startButton.setOnClickListener {
            startReactionTest(reactionTestView)
            // disable the startButton so it is no longer visible
            startButton.isEnabled = false
        }

        // change the label in the action bar
        supportActionBar!!.title = "Reaction Test"
    }

    private fun displayScore() {
        if (mAuth!!.currentUser != null) {
            mReactionScoreRef.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.i(TAG, "onDataChanged in onStart called")

                    val score: Any? = snapshot.child(mAuth!!.uid.toString()).child("score").value

                    if (score == null) {
                        recentScoreView.text = getString(R.string.highscore_text, "0")
                    } else {
                        recentScoreView.text = getString(R.string.highscore_text, score.toString())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // do nothing
                }
            })
        } else {
            val text = "Login here to see your recent score!"

            val ss = SpannableString(text)
            val clickable = object: ClickableSpan() {
                override fun onClick(view: View) {
                    val intent = Intent(applicationContext, LoginActivity::class.java)
                    startActivity(intent)
                }
            }

            ss.setSpan(clickable, 6, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            recentScoreView.text = ss
            recentScoreView.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun startReactionTest(view: TextView) {
        mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
        view.text = "Get ready..."

        val r = Random()
        val maxWaitTime = 2000

        // Test will be between 8 seconds to 17.5 seconds, as outlined in the project proposal
        // Remember that nextInt() is inclusive to the lower bound and exclusive to the upper,
        // So we add 8000 milliseconds to start at 8 seconds and end at 17.5
        val testDuration = r.nextInt(10500) + 8000
        Log.i(TAG, "Test will be $testDuration milliseconds long")

        // We set the color to red as the test begins
        view.setBackgroundColor(colorGray.toArgb())
        // The view will not have an OnClickListener until we begin our subtests
        view.setOnClickListener(null)

        // the OnClickListener for the view -- this is enabled during subtests,
        // and currently only uses a boolean to track whether or not the user clicked the view
        val reactionClick = View.OnClickListener {
            mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
            wasClicked = true
        }



        // Test is comprised of subtests -- these tests will change the color of the background
        // to the "go" color after a randomly selected interval of 1 to 3 seconds
        // These subtests will be run for testDuration -- this is accomplished with a CountDownTimer

        var timeUntilNextSubtest = generateDuration(r, 3, 1)

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
        val subtestTimer = object: CountDownTimer(maxWaitTime.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                //Log.i(TAG, "Now in subtestTimer")
                if (wasClicked) {
                    Log.i(TAG, "User reacted in time!")
                    view.setBackgroundColor(colorGray.toArgb())
                    view.text = "Good!"
                    wasClicked = false
                    view.setOnClickListener(null)
                    // The user reacted in time, so we add a successful test to the resultlist
                    addResult(true)
                    cancel()
                }
            }

            override fun onFinish() {
                Log.i(TAG, "Not fast enough!")
                view.setBackgroundColor(colorRed.toArgb())
                view.text = "Not fast enough!"
                view.setOnClickListener(null)
                // The user did not react in time, so we add an unsuccessful test to the resultlist
                addResult(false)
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
                if (timeUntilNextSubtest.toLong() == (currInterval++).toLong()) {
                    Log.i(TAG, "Changing to \"go\" color")
                    // This is where the subtest actually begins.
                    // Change the view color to "go" color, and wait for user input.
                    // Create a nested timer that will countdown using maxWaitTime.
                    // If the user reacts in time, cancel() the nested timer
                    // If the user is too slow, handle changing view color back to "no" color in onFinish
                    view.setBackgroundColor(colorGreen.toArgb())
                    view.text = "Click!"
                    view.setOnClickListener(reactionClick)
                    startTime = System.currentTimeMillis()
                    subtestTimer.start()
                    // Regenerate subTestDuration with a function
                    timeUntilNextSubtest = generateDuration(r, 3, 1)
                    Log.i(TAG, "New subTestDuration: $timeUntilNextSubtest")
                    currInterval = 0
                }

            }
            override fun onFinish() {
                Log.i(TAG, "Testing Done!")
                subtestTimer.cancel()
                view.setBackgroundColor(colorGray.toArgb())
                view.text = ""
                Log.i(TAG, "$resultList")
                startButton.isEnabled = true
                startButton.text = "Retry?"

                showResult()
            }
        }
        testTimer.start()

    }

    // -------------- new addition -----------------------------

    private fun showResult() {
        reactionTestView.setBackgroundColor(colorGray.toArgb())
        reactionTestView.text = ""

        // create intent to pass the result list to popup activity
        val intent = Intent(applicationContext, TestResultPopUp::class.java)
        intent.putExtra("result", resultList)
        intent.putExtra("testType", TestType.React)
        startActivity(intent)
        resultList.clear()
    }

    // ----------------------------------------------------------

    // Simple function to reduce clutter in important computations -- probably do not need this anymore
    private fun getElapsedTime(start: Long, end: Long): Long {
        return (end - start)
    }

    // Another simple function to reduce clutter
    private fun generateDuration(r: Random, bound: Int, offset: Int): Int {
        return r.nextInt(bound) + offset
    }

    // The addResult function creates a GNGResult whose fields are based on whether the user passed
    // or failed a subtest -- if the user clicked in time, they passed, and the GNGResult is
    // populated with TestStatus.SUCCESS, else if the user fails to react in time, the GNGResult
    // is populated with the TestStatus.FAILED
    // The GNGResult object is used because it is an integral unit of the firebase realtime database
    // This is a simple reaction test, and we do not need as rich detail for these tests as
    // is needed for the GoNoGoActivity, so GNGResult is well suited to recording results of these
    // two types of tests
    private fun addResult(passed: Boolean) {
        val time = getElapsedTime(startTime, System.currentTimeMillis())
        val result : GNGResult
        result = if (passed) {
            GNGResult(time, GNGMode.GO, TestStatus.SUCCESS)
        } else {
            GNGResult(time, GNGMode.GO, TestStatus.FAILED)
        }
        resultList.add(result)
    }

    private fun setColors(){
        colorGray = Color.valueOf(ContextCompat.getColor(applicationContext, R.color.gray))
        colorBlack = Color.valueOf(ContextCompat.getColor(applicationContext, R.color.black))

        when (applicationContext?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                colorGreen = Color.valueOf(ContextCompat.getColor(applicationContext, R.color.dark_green))
                colorRed = Color.valueOf(ContextCompat.getColor(applicationContext, R.color.dark_red))
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                colorGreen = Color.valueOf(ContextCompat.getColor(applicationContext, R.color.green))
                colorRed = Color.valueOf(ContextCompat.getColor(applicationContext, R.color.red))
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                colorGreen = Color.valueOf(ContextCompat.getColor(applicationContext, R.color.green))
                colorRed = Color.valueOf(ContextCompat.getColor(applicationContext, R.color.red))
            }
        }
    }

    companion object {
        const val TAG = "GoNoGo"
    }
}

enum class TestType {
    React, GNG
}