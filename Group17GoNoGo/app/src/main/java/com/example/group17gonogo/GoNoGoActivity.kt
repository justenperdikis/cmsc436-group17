package com.example.group17gonogo

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
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
//import com.google.firebase.database.*
//import com.google.firebase.auth.FirebaseAuth
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class GoNoGoActivity: AppCompatActivity() {

    // views
    private lateinit var startButton: Button
    private lateinit var reactionTestView: TextView
    private lateinit var instructionView: TextView
    private lateinit var recentScoreView: TextView

    // color variables
    private lateinit var colorGreen: Color
    private lateinit var colorRed: Color
    private lateinit var colorGray: Color
    private lateinit var colorBlack: Color

    // var for GoNoGo test
    private val maxWaitTime = 2000
    private var startTime: Long = 0
    private var goProbability = 0.8f
    private var numOfTest: Int = 20
    private var currNumOfTest: Int = 0
    private var transitionLength: Long =300
    private val noGoProbability = 1 - goProbability
    private var mode: GNGMode = GNGMode.NONE
    private var testStatus: TestStatus = TestStatus.TBD
    private var resultList = ArrayList<GNGResult>()
    private var hasStarted: Boolean = false
    private var timer: CountDownTimer? = null

    // variables for sounds
    private var mSoundPool: SoundPool? = null
    private var mSoundId: Int = 0
    private lateinit var mAudioManager: AudioManager

    // database
    private var mAuth: FirebaseAuth? = null
    private val mRootRef = FirebaseDatabase.getInstance().reference
    private val mGNGScoreRef = mRootRef.child("gngScores")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.go_no_go)

        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        startButton = findViewById(R.id.goNoGoStart_button)
        reactionTestView = findViewById(R.id.block_four)
        instructionView = findViewById(R.id.block_two)
        recentScoreView = findViewById(R.id.block_three)

        mAuth = FirebaseAuth.getInstance()
        instructionView.setText(R.string.GoNoGo_instructions)
        // display the player latest score in block3
        displayScore()

        //set to right depending on light or dark mode
        setColors()

        startButton.setOnClickListener {

            //Plays ding
            Log.i(TAG, "Play start sound")
            startPlayback()

            //counts down from 3
            var count = 3
            val countdownTimer = object: CountDownTimer(3000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    reactionTestView.text = count.toString()
                    Log.i(TAG, "Count$count")
                    count--
                }

                // executed when timer is finished
                override fun onFinish() {
                    //starts test
                    buttonPressed()
                }
            }
            countdownTimer.start()

            startButton.isEnabled = false
        }

        reactionTestView.setOnClickListener {
            Log.i(TAG, "reactionTestView clicked")
            buttonPressed()
        }
        // Disable clicking for reactionTestView until the start button is pressed
        reactionTestView.isClickable = false

        // change the label in the actionbar
        supportActionBar!!.title = "Go-No-Go Test"
    }

    private fun displayScore() {
        if (mAuth!!.currentUser != null) {
            mGNGScoreRef.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.i(ReactionActivity.TAG, "onDataChanged in onStart called")

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

            recentScoreView.setText(ss)
            recentScoreView.movementMethod = LinkMovementMethod.getInstance()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (timer != null) {
            timer!!.cancel()
        }
        finish()
    }

    override fun onResume() {
        super.onResume()
        displayScore()
        //load sound
        loadSoundPool()
        startButton.isEnabled = true
    }

    override fun onPause() {
        super.onPause()

        //Unload sound
        mSoundPool?.apply {
            unload(mSoundId)
            release()
            mSoundPool = null
        }
        mAudioManager.unloadSoundEffects()
    }

    private fun buttonPressed() {
        mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)

        if (!hasStarted) {
            Log.i(TAG, "Start game")
            // start the game
            // reset the probability to original
            goProbability = 0.8f
            // make the TextView clickable
            reactionTestView.isClickable = true
            hasStarted = true
            startTime = System.currentTimeMillis()
            startGame()
        } else {
            Log.i(TAG, "Ongoing game, change color")
            if (currNumOfTest < numOfTest) {
                // end current timer
                timer!!.cancel()
                // increase the probability of no-go appearing as the test goes
                goProbability -= 0.01f

                // get the score value
                // get the score if the user pressed the screen before the timer ends
                val result = getScore()
                // record the current test result
                resultList.add(result)
                testTransition()

                // increase the number of test done
                currNumOfTest += 1
                // start new timer for the next test
                timer!!.start()
            } else {
                reactionTestView.isClickable = false
                gameComplete()
            }
        }
    }

    private fun startGame() {
        // get the first color
        changeColor()

        // create timer that run for 2 second
        timer = object: CountDownTimer(maxWaitTime.toLong(), maxWaitTime.toLong()) {
            override fun onTick(millisUntilFinished: Long) {
                // Do nothing
            }

            // executed when timer is finished
            override fun onFinish() {
                // get the score value
                val result = getScore()
                // record the current test result
                resultList.add(result)
                // increase the number of test done
                currNumOfTest += 1
                // check if game is complete or not
                if (currNumOfTest < numOfTest) {
                    // increase the probability of no-go appearing as the test goes
                    goProbability -= 0.01f
                    testTransition()
                    // start the timer again if the user did not click at all
                    timer!!.start()
                } else {
                    reactionTestView.isClickable = false
                    // reached the max num of test, end the test
                    gameComplete()
                }
            }
        }
        timer!!.start()
    }

    // this function handle the short black screen between each test
    private fun testTransition() {
        val transitionTimer: CountDownTimer = object: CountDownTimer(transitionLength, transitionLength) {
            override fun onTick(p0: Long) {
                // change the background to black
                // player shouldn't be able to interact with black screen
                reactionTestView.isClickable = false
                startTime = System.currentTimeMillis()
                reactionTestView.setBackgroundColor(colorBlack.toArgb())
                reactionTestView.text = ""
            }

            override fun onFinish() {
                // black screen is over, change the color to the next one
                changeColor()
                reactionTestView.isClickable = true
            }

        }
        transitionTimer.start()
    }

    // this function checks if the user success or failed the test
    private fun getScore() : GNGResult {
        var time = getElapsedTime(startTime, System.currentTimeMillis())

        if (mode == GNGMode.GO) {
            if (time < maxWaitTime) {
                testStatus = TestStatus.SUCCESS
            } else {
                testStatus = TestStatus.FAILED
                time = 2000
            }
        } else {
            if (time >= maxWaitTime) {
                testStatus = TestStatus.SUCCESS
                time = 2000
            } else {
                testStatus = TestStatus.FAILED
            }
        }

        return GNGResult(time, mode, testStatus)
    }

    private fun gameComplete() {
        Log.i(TAG, "Game complete")
        // reset the num of test counter
        currNumOfTest = 0
        // show player test result
        reactionTestView.setBackgroundColor(colorGray.toArgb())
        startButton.text = "Retry?"
        hasStarted = false
        showResult()
        resultList.clear()
    }


    private fun changeColor() {
        mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)

        // generate random number between 0 and 100
        val rand = (0..100).random()
        val ngBound = 100 * noGoProbability

        if (rand < ngBound) {
            // record the test is currently showing a No-Go
            mode =
                GNGMode.NO_GO
            reactionTestView.setBackgroundColor(colorRed.toArgb())
            reactionTestView.text = "No Go"
        } else {
            // record the test is currently showing a Go
            mode =
                GNGMode.GO
            reactionTestView.setBackgroundColor(colorGreen.toArgb())
            reactionTestView.text = "Go"
        }
    }

    private fun showResult() {
        // make sure to stops any unintended timer before showing the test result
        timer!!.cancel()
        reactionTestView.setBackgroundColor(colorGray.toArgb())
        reactionTestView.text = ""

        // create intent to pass the result list to popup activity
        val intent = Intent(applicationContext, TestResultPopUp::class.java)
        intent.putExtra("result", resultList)
        intent.putExtra("testType", TestType.GNG)
        startActivity(intent)
    }

    // Simple function to reduce clutter in important computations -- probably do not need this anymore
    private fun getElapsedTime(start: Long, end: Long): Long {
        return (end - start)
    }

    private fun startPlayback() {
        mSoundPool?.play(
                mSoundId, 10f, 10f, 1, 0, 1.0f
        )
        Log.i(TAG, "Starting playback in startPlayback()")
    }

    private fun loadSoundPool() {
        // Create a SoundPool
        val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        mSoundPool = SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .build()

        mSoundPool?.apply {
            // Load bubble popping sound into the SoundPool
            mSoundId = load(this@GoNoGoActivity, R.raw.ding_harsh, 1)
        }
    }

    //Sets the colors to light green and red for light mode or unknown mode and dark green and
    //red for dark mode.
    private fun setColors(){
        colorGray = Color.valueOf(ContextCompat.getColor(applicationContext, R.color.gray))
        colorBlack = Color.valueOf(ContextCompat.getColor(applicationContext, R.color.black))

        //Determines if the device is in dark or light mode.
        // Origin code source:
        // https://stackoverflow.com/questions/44170028/android-how-to-detect-if-night-mode-is-on-when-using-appcompatdelegate-mode-ni
        val mode = applicationContext?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
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

enum class GNGMode {
    NONE, GO, NO_GO
}

enum class TestStatus {
    TBD, SUCCESS, FAILED
}