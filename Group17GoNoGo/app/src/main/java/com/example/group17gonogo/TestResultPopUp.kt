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
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.w3c.dom.Text
import java.util.ArrayList

class TestResultPopUp : Activity() {

    private lateinit var resultTextView: TextView
    private lateinit var noticeTextView: TextView
    private var mAuth: FirebaseAuth? = null
    private var totalScore: Long = 0

    private val mRootRef = FirebaseDatabase.getInstance().reference
    private val mScoreRef = mRootRef.child("scores")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_result_pop_up)
        Log.i(TAG, "Entered pop up onCreate")

        noticeTextView = findViewById(R.id.notice_text)
        resultTextView = findViewById(R.id.result_text_view)
        // set the textView to have a vertical scrollbar
        resultTextView.movementMethod = ScrollingMovementMethod()
        mAuth = FirebaseAuth.getInstance()

        window.setLayout(1000, 1500)                                        // set the size of the popup window, might need to adjust later based on the number of test done

        var windowParams = window.attributes
        windowParams.gravity = Gravity.CENTER                                            // set the pop up window to appear in the center of the screen

        window.attributes = windowParams

        showResult(intent.getSerializableExtra("result") as ArrayList<GNGResult>)
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
                totalScore += result.getReactTime()
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
            compareScore()
        } else {
            Log.i(TAG, "No user is logged in")
            setNotice()

        }

        resultTextView.text = resultText
    }

    private fun setNotice() {
        var text = "Login here to compare your score with other players!"

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

    private fun compareScore() {
        noticeTextView.setText("Need to compare score here")
    }

    private fun addScore(score: Long) {
        val uid = mAuth!!.uid as String
        val score = Score(uid, score.toInt())

        mScoreRef.child(uid).setValue(score)
    }

    override fun onResume() {
        super.onResume()
        if (mAuth!!.currentUser != null) {
            addScore(totalScore)                // add the score to the database of the newly logged in user
            compareScore()                      // compare the user score with other users
        }
    }

    companion object {
        const val TAG = "GoNoGo"
    }
}