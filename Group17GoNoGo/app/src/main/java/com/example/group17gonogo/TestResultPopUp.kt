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
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.w3c.dom.Text
import java.lang.Exception
import java.util.ArrayList

class TestResultPopUp : Activity() {

    private lateinit var resultTextView: TextView
    private lateinit var noticeTextView: TextView
    private lateinit var showLeaderboardButton: Button
    private var mAuth: FirebaseAuth? = null
    private var totalScore: Long = 0

    private val mRootRef = FirebaseDatabase.getInstance().reference
    private val mScoreRef = mRootRef.child("scores")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_result_pop_up)

        Log.i(TAG, "Entered pop up onCreate")

        noticeTextView = findViewById(R.id.notice_text)
        showLeaderboardButton = findViewById(R.id.leaderboard_button)
        resultTextView = findViewById(R.id.result_text_view)
        // set the textView to have a vertical scrollbar
        resultTextView.movementMethod = ScrollingMovementMethod()
        mAuth = FirebaseAuth.getInstance()

        window.setLayout(1000, 1500)                                        // set the size of the popup window, might need to adjust later based on the number of test done

        var windowParams = window.attributes
        windowParams.gravity = Gravity.CENTER                                            // set the pop up window to appear in the center of the screen

        window.attributes = windowParams

        showResult(intent.getSerializableExtra("result") as ArrayList<GNGResult>)


        showLeaderboardButton.setOnClickListener() {
            var intent = Intent(applicationContext, LeaderboardActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showResult(list: ArrayList<GNGResult>) {
        Log.i(TAG, "showResult() called with list size of ${list.size}")

        var resultText = ""
        totalScore = 0              // reset the score everytime a new result need to be displayed

        // loop through the list, create resultString based on the value on each result, append it to create one long string
        for (result in list) {
            var singleResult = "${result.getGNGMode()} - ${result.getReactTime()} ms - ${result.getTestStatus()}\n"
            resultText += singleResult

            if (result.getGNGMode() == GNGMode.GO && result.getTestStatus() == TestStatus.SUCCESS) {
                totalScore += (2000 - result.getReactTime())
            } else if (result.getGNGMode() == GNGMode.NO_GO && result.getTestStatus() == TestStatus.SUCCESS) {
                totalScore += 1000
            } else {
                totalScore -= 500
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
        var text = "Login here to see the leaderboard!"

        var ss = SpannableString(text)
        var clickable = object: ClickableSpan() {
            override fun onClick(view: View) {
                var intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        ss.setSpan(clickable, 6, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        noticeTextView.setText(ss)
        noticeTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun addScore(score: Long) {
        val uid = mAuth!!.uid as String

        // trying to query the username out of "users" database
        val mUserRef = mRootRef.child("users")
        val currUser = mUserRef.child(uid).child("userId")

        currUser.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var username = snapshot.getValue().toString()

                // record the data inside onDataChange because it took time to access database and query the username
                val score = Score(username, score.toInt())
                mScoreRef.child(uid).setValue(score)
            }

            override fun onCancelled(error: DatabaseError) {
                // do nothing
            }
        })

    }

    override fun onResume() {
        super.onResume()
        if (mAuth!!.currentUser != null) {
            addScore(totalScore)                // add the score to the database of the newly logged in user
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