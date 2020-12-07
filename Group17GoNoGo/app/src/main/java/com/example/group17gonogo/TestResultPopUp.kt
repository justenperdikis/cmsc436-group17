package com.example.group17gonogo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.ArrayList

class TestResultPopUp : Activity() {

    private lateinit var resultTextView: TextView
    private lateinit var noticeTextView: TextView
    private lateinit var showLeaderboardButton: Button
    private var mAuth: FirebaseAuth? = null
    private var totalScore: Long = 0

    private lateinit var testType:TestType

    private val mRootRef = FirebaseDatabase.getInstance().reference
    private val mGNGScoreRef = mRootRef.child("gngScores")
    private val mReactionScoreRef = mRootRef.child("reactionScores")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_result_pop_up)

        Log.i(TAG, "Entered pop up onCreate")

        testType = intent.getSerializableExtra("testType") as TestType

        noticeTextView = findViewById(R.id.notice_text)
        showLeaderboardButton = findViewById(R.id.leaderboard_button)
        resultTextView = findViewById(R.id.result_text_view)
        // set the textView to have a vertical scrollbar
        resultTextView.movementMethod = ScrollingMovementMethod()
        mAuth = FirebaseAuth.getInstance()

        window.setLayout(1000, 1500)                                        // set the size of the popup window, might need to adjust later based on the number of test done

        val windowParams = window.attributes
        windowParams.gravity = Gravity.CENTER                                            // set the pop up window to appear in the center of the screen

        window.attributes = windowParams

        showResult(intent.getSerializableExtra("result") as ArrayList<GNGResult>)


        showLeaderboardButton.setOnClickListener {
            val intent = Intent(applicationContext, LeaderboardActivity::class.java)
            intent.putExtra("testType", testType)
            startActivity(intent)
        }
    }

    private fun showResult(list: ArrayList<GNGResult>) {
        Log.i(TAG, "showResult() called with list size of ${list.size}")

        var resultText = ""
        // reset the score every time a new result need to be displayed
        totalScore = 0

        // loop through the list, create resultString based on the value on each result, append it to create one long string
        when (testType) {
            TestType.GNG -> {
                for (result in list) {
                    val singleResult = "${result.getGNGMode()} - ${result.getReactTime()} ms - ${result.getTestStatus()}\n"
                    resultText += singleResult

                    // the smaller the score, the better the user perform on the test
                    if (result.getGNGMode() == GNGMode.GO && result.getTestStatus() == TestStatus.SUCCESS) {
                        totalScore += result.getReactTime()
                    } else if (result.getGNGMode() == GNGMode.NO_GO && result.getTestStatus() == TestStatus.SUCCESS) {
                        totalScore += 500
                    } else {
                        totalScore += 2000
                    }
                }
            }
            // calculate the score for reaction test
            TestType.React -> {
                for (result in list) {
                    val singleResult = "${result.getReactTime()} ms - ${result.getTestStatus()}\n"
                    resultText += singleResult

                    if (result.getTestStatus() == TestStatus.SUCCESS) {
                        // add points every time user succeed
                        totalScore += result.getReactTime()
                    } else {
                        // subtract points for every time user failed
                        totalScore -= result.getReactTime()
                    }
                }
            }
        }


        resultText += "\nYour test score is: $totalScore\n"

        // check if user is logged in or not
        // if yes, record test score, show how the user percentile
        // else, show text asking for user to login or register for account

        if (mAuth!!.currentUser != null) {
            Log.i(TAG, "A user is logged in")
            addScore(totalScore)
            noticeTextView.visibility = View.GONE
            showLeaderboardButton.visibility = View.VISIBLE
        } else {
            Log.i(TAG, "No user is logged in")
            showLeaderboardButton.visibility = View.GONE
            noticeTextView.visibility = View.VISIBLE
            setNotice()

        }

        resultTextView.text = resultText
    }

    private fun setNotice() {
        val text = "Login here to see the leaderboard!"

        val ss = SpannableString(text)
        val clickable = object: ClickableSpan() {
            override fun onClick(view: View) {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        ss.setSpan(clickable, 6, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        noticeTextView.text = ss
        noticeTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun addScore(score: Long) {
        val uid = mAuth!!.uid as String

        // trying to query the username out of "users" database
        val mUserRef = mRootRef.child("users")
        val currUser = mUserRef.child(uid).child("userId")

        currUser.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.value.toString()

                // record the data inside onDataChange because it took time to access database and query the username
                val score = Score(username, score.toInt())

                when (testType) {
                    TestType.GNG -> {
                        mGNGScoreRef.child(uid).setValue(score)
                    }
                    TestType.React -> {
                        mReactionScoreRef.child(uid).setValue(score)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // do nothing
            }
        })

    }

    override fun onResume() {
        super.onResume()
        if (mAuth!!.currentUser != null) {
            // add the score to the database of the newly logged in user
            addScore(totalScore)
            noticeTextView.visibility = View.GONE
            showLeaderboardButton.visibility = View.VISIBLE
        } else {
            showLeaderboardButton.visibility = View.GONE
            noticeTextView.visibility = View.VISIBLE
            setNotice()
        }
    }

    companion object {
        const val TAG = "GoNoGo"
    }
}