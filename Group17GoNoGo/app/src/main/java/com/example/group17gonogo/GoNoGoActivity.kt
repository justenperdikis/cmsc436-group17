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
    private lateinit var highScoreView: TextView

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
        highScoreView = findViewById(R.id.block_three)

        mAuth = FirebaseAuth.getInstance()
        instructionView.setText(R.string.GoNoGo_instructions)
        displayScore()                                              // display the player latest score in block3

        //set to right depending on light or dark mode
        setColors()

        startButton.setOnClickListener {

            //Plays ding
            Log.i(TAG, "Play start sound")
            startPlayback()

            //counts down from 3
            var count = 3
            var countdownTimer = object: CountDownTimer(3000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    reactionTestView.setText(count.toString())
                    Log.i(TAG, "Count" + count.toString())
                    count--
                }

                override fun onFinish() {                                                           // executed when timer is finished
                    //starts test
                    buttonPressed()
                }
            }
            countdownTimer!!.start()

            startButton.isEnabled = false
        }

        reactionTestView.setOnClickListener {
            Log.i(TAG, "reactionTestView clicked")
            buttonPressed()
        }
        // Disable clicking for reactionTestView until the start button is pressed
        reactionTestView.isClickable = false
    }

    private fun displayScore() {
        if (mAuth!!.currentUser != null) {
            mGNGScoreRef.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.i(ReactionActivity.TAG, "onDataChanged in onStart called")

                    var score: Any? = snapshot.child(mAuth!!.uid.toString()).child("score").getValue()

                    if (score == null) {
                        highScoreView.text = "Recent Score: 0"
                    } else {
                        highScoreView.text = "Recent Score: ${score.toString()}"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // do nothing
                }
            })
        } else {
            var text = "Login here to see your recent score!"

            var ss = SpannableString(text)
            var clickable = object: ClickableSpan() {
                override fun onClick(view: View) {
                    var intent = Intent(applicationContext, LoginActivity::class.java)
                    startActivity(intent)
                }
            }

            ss.setSpan(clickable, 6, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            highScoreView.setText(ss)
            highScoreView.movementMethod = LinkMovementMethod.getInstance()
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
        loadSoundPool()                                             //load sound
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
            goProbability = 0.8f                                    // reset the probability to original
            reactionTestView.isClickable = true                     // make the TextView clickable
            hasStarted = true
            startTime = System.currentTimeMillis()
            startGame()
        } else {
            Log.i(TAG, "Ongoing game, change color")
            if (currNumOfTest < numOfTest) {
                timer!!.cancel()                                   // end current timer
                goProbability -= 0.01f                             // increase the probability of no-go appearing as the test goes

                // get the score value
                var result = getScore()                 // get the score if the user pressed the screen before the timer ends
                resultList.add(result)                            // record the current test result
                //changeColor()                                   // change the color if RNGesus allows
                testTransition()

                currNumOfTest += 1                                // increase the number of test done
                timer!!.start()                                   // start new timer for the next test
            } else {
                reactionTestView.isClickable = false
                gameComplete()
            }
        }
    }

    private fun startGame() {
        changeColor()                                                                           // get the first color

        timer = object: CountDownTimer(maxWaitTime.toLong(), maxWaitTime.toLong()) {          // create timer that run for 2 second
            override fun onTick(millisUntilFinished: Long) {
                // Do nothing
            }

            override fun onFinish() {                                                           // executed when timer is finished
                // get the score value
                var result = getScore()
                resultList.add(result)                                                          // record the current test result

                currNumOfTest += 1                                                              // increase the number of test done
                if (currNumOfTest < numOfTest) {                                                // check if game is complete or not
                    goProbability -= 0.01f                                                      // increase the probability of no-go appearing as the test goes
                    //changeColor()                                                               // change the color if RNGesus allows
                    testTransition()
                    timer!!.start()                                                               // start the timer again if the user did not click at all
                } else {
                    reactionTestView.isClickable = false
                    gameComplete()                                                              // reached the max num of test, end the test
                }
            }
        }
        timer!!.start()
    }

    // this function handle the short black screen between each test
    private fun testTransition() {
        var transitionTimer: CountDownTimer = object: CountDownTimer(transitionLength, transitionLength) {
            override fun onTick(p0: Long) {
                // change the bg to black
                reactionTestView.isClickable = false                            // player shouldn't be able to interact with black screen
                startTime = System.currentTimeMillis()
                reactionTestView.setBackgroundColor(colorBlack.toArgb())
                reactionTestView.text = ""
            }

            override fun onFinish() {
                // call changeColor
                changeColor()                                                   // black screen is over, change the color to the next one
                reactionTestView.isClickable = true
            }

        }
        transitionTimer.start()
    }

    // this function checks if the user success or failed the test
    private fun getScore() : GNGResult {
        var time = getElapsedTime(startTime, System.currentTimeMillis())
        //Log.i(TAG, "starttime: ${startTime}, time: $time , current time: ${System.currentTimeMillis()}")

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

        var result = GNGResult(time, this.mode, testStatus)

        return result
    }

    private fun gameComplete() {
        Log.i(TAG, "Game complete")
        currNumOfTest = 0                                                                   // reset the num of test counter
        // show player test result
        reactionTestView.setBackgroundColor(colorGray.toArgb())
        startButton.text = "Retry?"
        hasStarted = false
        showResult()
        resultList.clear()
    }


    private fun changeColor() {
        mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)

        var rand = (0..100).random()                                    // generate random number between 0 and 100
        var ngBound = 100 * noGoProbability

        if (rand < ngBound) {
            mode =
                GNGMode.NO_GO                                        // record the test is currently showing a No-Go
            reactionTestView.setBackgroundColor(colorRed.toArgb())
            reactionTestView.setText("No Go")
        } else {
            mode =
                GNGMode.GO                                           // record the test is currently showing a Go
            reactionTestView.setBackgroundColor(colorGreen.toArgb())
            reactionTestView.setText("Go")
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